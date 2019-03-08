package io.craft.server.handler;

import io.craft.core.constant.Constants;
import io.craft.core.message.CraftFramedMessage;
import io.craft.server.executor.CraftBusinessExecutor;
import io.craft.server.executor.CraftBusinessTask;
import io.craft.server.executor.CraftRejectedExecutionHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TMessage;

import java.util.concurrent.ExecutorService;

@ChannelHandler.Sharable
public class CraftMessageHandler extends SimpleChannelInboundHandler<CraftFramedMessage> {

    private ExecutorService executor;

    private TProcessor processor;

    public CraftMessageHandler(TProcessor processor, ExecutorService executor) {
        this.processor = processor;
        this.executor = executor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CraftFramedMessage message) throws Exception {
        TMessage msg = message.getMessageHeader();
        if (msg.type == Constants.MESSAGE_TYPE_INIT) {
            //服务端直接忽略客户端发过来的初始化连接消息
            CraftFramedMessage response = new CraftFramedMessage(ctx.channel());
            response.writeMessageBegin(new TMessage(msg.name, Constants.MESSAGE_TYPE_INIT, msg.seqid));
            response.writeMessageEnd();
            ctx.writeAndFlush(response);
            return;
        }
        //防止ref被释放，先占用一次，后续会释放
        message.retain();
        executor.submit(new CraftBusinessTask(ctx, processor, message));
    }

}
