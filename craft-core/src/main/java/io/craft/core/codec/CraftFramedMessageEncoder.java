package io.craft.core.codec;

import io.craft.core.message.CraftMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

@ChannelHandler.Sharable
public class CraftFramedMessageEncoder extends MessageToByteEncoder<CraftMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, CraftMessage message, ByteBuf out) throws Exception {
        out.capacity(message.readableBytes());
        message.readBytes(out);
    }
}
