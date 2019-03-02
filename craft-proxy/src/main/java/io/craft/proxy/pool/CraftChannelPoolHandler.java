package io.craft.proxy.pool;

import io.craft.core.codec.CraftFramedMessageEncoder;
import io.craft.proxy.constant.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CraftChannelPoolHandler implements ChannelPoolHandler {
    private static final Logger logger = LoggerFactory.getLogger(CraftChannelPoolHandler.class);
    @Override
    public void channelReleased(Channel ch) throws Exception {
        logger.debug("channelReleased. Channel ID: " + ch.id());
    }

    @Override
    public void channelAcquired(Channel ch) throws Exception {
        logger.debug("channelAcquired. Channel ID: " + ch.id());
    }

    @Override
    public void channelCreated(Channel ch) throws Exception {
        logger.debug("channelCreated. Channel ID: " + ch.id());
        SocketChannel channel = (SocketChannel) ch;
        channel.config().setKeepAlive(true);
        channel.config().setTcpNoDelay(true);
        logger.debug("channel is active:"+ch.isActive());
        channel.pipeline().addLast(new CraftFramedMessageEncoder()).addLast(new SimpleChannelInboundHandler<ByteBuf>() {
            //内层建立的连接，从这里接收内层的应答，在这里是服务端的应答
            @Override
            protected void channelRead0(
                    ChannelHandlerContext ctx2, ByteBuf in)
                    throws Exception {
                logger.debug("inner received bytes = {}", in.readableBytes());
                ByteBuf readBuffer = ctx2.alloc().directBuffer(1024);
                readBuffer.writeBytes(in);
                ctx2.channel().attr(Constants.SERVER_ATTRIBUTE_CLIENT).get().writeAndFlush(readBuffer);
            }

        });
    }

}
