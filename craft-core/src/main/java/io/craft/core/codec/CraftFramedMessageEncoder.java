package io.craft.core.codec;

import io.craft.core.message.CraftFramedMessage;
import io.craft.core.transport.TByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMap;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TType;

import java.util.Map;

@ChannelHandler.Sharable
public class CraftFramedMessageEncoder extends MessageToByteEncoder<CraftFramedMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, CraftFramedMessage message, ByteBuf out) throws Exception {
        ByteBuf buffer = message.getBuffer();
        out.writeInt(buffer.readableBytes());
        out.writeBytes(buffer);
    }
}
