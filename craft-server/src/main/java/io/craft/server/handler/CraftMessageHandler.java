package io.craft.server.handler;

import io.craft.core.constant.Constants;
import io.craft.core.message.CraftFramedMessage;
import io.craft.core.transport.TByteBuf;
import io.craft.core.util.CraftExceptionHandler;
import io.craft.server.executor.CraftBusinessExecutor;
import io.craft.server.executor.CraftBusinessTask;
import io.craft.server.executor.CraftRejectedExecutionHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;

import java.util.concurrent.ExecutorService;

@ChannelHandler.Sharable
public class CraftMessageHandler extends ChannelInboundHandlerAdapter {

    private final static ExecutorService executorService = new CraftBusinessExecutor(2,20000, new CraftRejectedExecutionHandler());

    private TProcessor processor;

    public CraftMessageHandler(TProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //super.channelRead(ctx, msg);
        if (!(msg instanceof CraftFramedMessage)) {
            super.channelRead(ctx, msg);
            return;
        }

        CraftFramedMessage message = (CraftFramedMessage) msg;

        executorService.submit(new CraftBusinessTask(ctx, processor, message));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        CraftExceptionHandler.handle(ctx, cause);
//
//        TApplicationException exception;
//        if (!(cause instanceof TApplicationException)) {
//            exception = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal Server Error : "+cause.getMessage());
//        } else {
//            exception = (TApplicationException) cause;
//        }
//
//        ByteBuf writeBuffer = ctx.alloc().directBuffer(Constants.DEFAULT_BYTEBUF_SIZE);
//        TByteBuf tout = new TByteBuf(writeBuffer);
//        TProtocol pout = new TBinaryProtocol(tout);
//        pout.writeMessageBegin(new TMessage("exception", TMessageType.EXCEPTION, 0));
//        exception.write(pout);
//        pout.writeMessageEnd();
//
//        CraftFramedMessage message = new CraftFramedMessage(writeBuffer, null);
//
//        ctx.writeAndFlush(message).sync();
    }
}
