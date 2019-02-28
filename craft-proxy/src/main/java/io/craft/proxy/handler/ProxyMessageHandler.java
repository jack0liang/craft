package io.craft.proxy.handler;

import io.craft.core.constant.Constants;
import io.craft.core.pool.ChannelPoolManager;
import io.craft.core.spring.PropertyManager;
import io.craft.proxy.discovery.EtcdServiceDiscovery;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;

import java.net.InetSocketAddress;

@Slf4j
public class ProxyMessageHandler extends ChannelInboundHandlerAdapter {
    private static final String RETURN_HANDLER_NAME = "RETURN_HANDLER_NAME";
    private EtcdServiceDiscovery discovery;
    private PropertyManager propertyManager;
    private SimpleChannelPool pool;
    private Channel innerChannel;

    public ProxyMessageHandler(BeanFactory beanFactory) {
        this.discovery = beanFactory.getBean(EtcdServiceDiscovery.class);
        this.propertyManager = beanFactory.getBean(PropertyManager.class);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        String applicationName = propertyManager.getProperty(Constants.APPLICATION_NAME);
        String serviceAddr = discovery.findService(applicationName);
        String[] si = serviceAddr.split(":");
        InetSocketAddress address = new InetSocketAddress(si[0], Integer.parseInt(si[1]));
        pool = ChannelPoolManager.getChannel(applicationName,address);
        Future<Channel> futureChannel = pool.acquire();
        innerChannel = futureChannel.get();
        ChannelPipeline pipeline = innerChannel.pipeline();
        if(pipeline.get(RETURN_HANDLER_NAME)!=null)
            pipeline.remove(RETURN_HANDLER_NAME);
        pipeline.addLast(RETURN_HANDLER_NAME,new SimpleChannelInboundHandler<ByteBuf>() {
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

        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("channelInactive:"+ctx);
        if(pool!=null&&innerChannel!=null)
            pool.release(innerChannel);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof ByteBuf)) {
            super.channelRead(ctx, msg);
            return;
        }
        ByteBuf in = (ByteBuf) msg;
        logger.debug("received bytes = {}", in.readableBytes());
        if(innerChannel!=null&&innerChannel.isActive()){
            innerChannel.writeAndFlush(in);
        }
    }
}
