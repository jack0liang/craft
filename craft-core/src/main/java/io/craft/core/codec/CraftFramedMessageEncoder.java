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
        out.writeInt(0);
        TProtocol protocol = new TBinaryProtocol(new TByteBuf(out));
        //写入messageId
        protocol.writeString(message.getTraceId());
        //写入header
        TMap map = new TMap(TType.STRING, TType.STRING, message.getHeader().size());
        protocol.writeMapBegin(map);
        for(Map.Entry<String, String> entry : message.getHeader().entrySet()) {
            protocol.writeString(entry.getKey());
            protocol.writeString(entry.getValue());
        }
        protocol.writeMapEnd();
        out.writeBytes(buffer);
        out.setInt(0, out.readableBytes() - 4);
    }
}
