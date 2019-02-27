package io.craft.proxy.handler;

import io.craft.core.constant.Constants;
import io.craft.core.spring.PropertyManager;
import io.craft.proxy.discovery.EtcdServiceDiscovery;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class ProxyMessageHandler extends ByteToMessageDecoder {
    private EtcdServiceDiscovery discovery;
    private PropertyManager propertyManager;
    private ByteBuf readBuffer;
    private ChannelHandlerContext innerCtx;
    ChannelFuture connectFuture;

    public ProxyMessageHandler(BeanFactory beanFactory) {
        this.discovery = beanFactory.getBean(EtcdServiceDiscovery.class);
        this.propertyManager = beanFactory.getBean(PropertyManager.class);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.readBuffer = ctx.alloc().directBuffer(Constants.DEFAULT_BYTEBUF_SIZE);

        String applicationName = propertyManager.getProperty(Constants.APPLICATION_NAME);
        String serviceAddr = discovery.findService(applicationName);
        String[] si = serviceAddr.split(":");
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class).handler(
                new SimpleChannelInboundHandler<ByteBuf>() {
                    //内层建立的连接，从这里接收内层的应答，在这里是服务端的应答
                    @Override
                    protected void channelRead0(
                            ChannelHandlerContext ctx2, ByteBuf in)
                            throws Exception {
                        logger.debug("inner received bytes = {}", in.readableBytes());
                        ByteBuf readBuffer = ctx2.alloc().directBuffer(Constants.DEFAULT_BYTEBUF_SIZE);
                        readBuffer.writeBytes(in);

                        ctx.writeAndFlush(readBuffer);
                    }

                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        logger.debug("channelActive链接服务"+ctx.channel().toString());
                        innerCtx = ctx;
                        if (readBuffer.readableBytes() > 0)
                            ctx.writeAndFlush(readBuffer);
                    }

                } );
        bootstrap.group(ctx.channel().eventLoop());//关键在这里。把外层channel的eventLoop挂接在内层上;ByteBuf创建和释放需是同一个线程
        connectFuture = bootstrap.connect(new InetSocketAddress(si[0], Integer.parseInt(si[1])));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("channelInactive:"+ctx);
        super.channelInactive(ctx);
        if (readBuffer.refCnt() > 0) {
            readBuffer.release(readBuffer.refCnt());
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        logger.debug("received bytes = {}", in.readableBytes());
        if(readBuffer.refCnt()==0)
            readBuffer=ctx.alloc().directBuffer(Constants.DEFAULT_BYTEBUF_SIZE);
        readBuffer.writeBytes(in);
        if (connectFuture.isDone()) {
            if (innerCtx != null && innerCtx.channel().isActive()&&readBuffer.readableBytes() > 0) {
                innerCtx.writeAndFlush(readBuffer);
            }
        }
    }
}
