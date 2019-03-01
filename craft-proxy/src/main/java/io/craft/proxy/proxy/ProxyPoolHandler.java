package io.craft.proxy.proxy;

import io.craft.core.codec.CraftFramedMessageEncoder;
import io.craft.core.codec.CraftThrowableEncoder;
import io.craft.proxy.constant.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.channel.pool.ChannelPool;
import io.netty.util.Attribute;

public class ProxyPoolHandler extends AbstractChannelPoolHandler {

    private ChannelPool pool;

    private ChannelInboundHandler handler = new ProxyToServerHandler();

    private CraftFramedMessageEncoder messageEncoder = new CraftFramedMessageEncoder();

    private CraftThrowableEncoder throwableEncoder = new CraftThrowableEncoder();

    public void setPool(ChannelPool pool) {
        this.pool = pool;
    }

    @Override
    public void channelCreated(Channel ch) throws Exception {
        ch.pipeline()
                .addLast(messageEncoder)
                .addLast(throwableEncoder)
                .addLast(handler);
    }

    @Override
    public void channelReleased(Channel ch) throws Exception {
        //移除服务连接上的client
        Attribute<Channel> attrClient = ch.attr(Constants.SERVER_ATTRIBUTE_CLIENT);
        attrClient.set(null);
//        ch.pipeline()
//                .remove(handler)
//                .remove(throwableEncoder)
//                .remove(messageEncoder);
    }

//    @Override
//    public void channelAcquired(Channel ch) throws Exception {
//        ch.pipeline()
//                .addLast(messageEncoder)
//                .addLast(throwableEncoder)
//                .addLast(handler);
//    }

    @ChannelHandler.Sharable
    private class ProxyToServerHandler extends SimpleChannelInboundHandler<ByteBuf> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
            buffer.retain();
            Attribute<Channel> attrClient = ctx.channel().attr(Constants.SERVER_ATTRIBUTE_CLIENT);
            attrClient.get().writeAndFlush(buffer);
        }
    }
}
