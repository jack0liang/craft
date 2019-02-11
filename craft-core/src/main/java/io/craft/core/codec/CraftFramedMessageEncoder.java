package io.craft.core.codec;

import io.craft.core.message.CraftFramedMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.thrift.transport.TTransportException;

import java.util.List;

@ChannelHandler.Sharable
public class CraftFramedMessageEncoder extends MessageToByteEncoder<CraftFramedMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, CraftFramedMessage message, ByteBuf out) throws Exception {
        ByteBuf buffer = message.getBuffer();
        out.writeInt(buffer.readableBytes());
        out.writeBytes(buffer);
    }
}
