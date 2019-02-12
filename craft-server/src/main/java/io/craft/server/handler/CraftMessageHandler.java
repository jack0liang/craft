package io.craft.server.handler;

import io.craft.core.message.CraftFramedMessage;
import io.craft.server.executor.CraftBusinessExecutor;
import io.craft.server.executor.CraftBusinessTask;
import io.craft.server.executor.CraftRejectedExecutionHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.thrift.TProcessor;

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
        ctx.write(cause);
    }
}
