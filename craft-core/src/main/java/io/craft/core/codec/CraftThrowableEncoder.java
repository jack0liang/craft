package io.craft.core.codec;

import io.craft.core.constant.Constants;
import io.craft.core.message.CraftFramedMessage;
import io.craft.core.transport.TByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;

import java.util.List;

@ChannelHandler.Sharable
public class CraftThrowableEncoder extends MessageToMessageEncoder<Throwable> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Throwable cause, List<Object> out) throws Exception {
        TApplicationException exception;
        if (!(cause instanceof TApplicationException)) {
            exception = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal Server Error : "+cause.getMessage());
        } else {
            exception = (TApplicationException) cause;
        }

        ByteBuf writeBuffer = ctx.alloc().directBuffer(Constants.DEFAULT_BYTEBUF_SIZE);

        TByteBuf tout = new TByteBuf(writeBuffer);
        TProtocol pout = new TBinaryProtocol(tout);

        pout.writeMessageBegin(new TMessage("exception", TMessageType.EXCEPTION, 0));
        exception.write(pout);
        pout.writeMessageEnd();

        CraftFramedMessage message = CraftFramedMessage.newBuilder().setBuffer(writeBuffer).build();

        out.add(message);
    }

}
