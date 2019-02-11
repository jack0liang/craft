package io.craft.core.util;

import io.craft.core.constant.Constants;
import io.craft.core.transport.TByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;

public class CraftExceptionHandler {

    public static void handle(ChannelHandlerContext context, Throwable cause) {
        TApplicationException exception;
        if (!(cause instanceof TApplicationException)) {
            exception = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal Server Error : "+cause.getMessage());
        } else {
            exception = (TApplicationException) cause;
        }

        ByteBuf writeBuffer = context.alloc().directBuffer(Constants.DEFAULT_BYTEBUF_SIZE);

        try {
            TByteBuf tout = new TByteBuf(writeBuffer);
            TProtocol pout = new TBinaryProtocol(tout);

            writeBuffer.writeInt(0);

            pout.writeMessageBegin(new TMessage("exception", TMessageType.EXCEPTION, 0));
            exception.write(pout);
            pout.writeMessageEnd();

            writeBuffer.setInt(0, writeBuffer.readableBytes() - 4);

            context.writeAndFlush(writeBuffer).sync();
        } catch (Exception e) {
            //抛出异常只能处理掉
            e.printStackTrace();
        } finally {
            if (writeBuffer.refCnt() > 0) {
                writeBuffer.release();
            }
        }
    }
}
