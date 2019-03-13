package io.craft.server.handler;

import io.craft.core.message.CraftMessage;
import io.craft.core.thrift.TMessage;
import io.craft.core.thrift.TMessageType;
import io.craft.core.thrift.TProcessor;
import io.craft.server.executor.CraftBusinessTask;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.ExecutorService;

@ChannelHandler.Sharable
public class CraftMessageHandler extends SimpleChannelInboundHandler<CraftMessage> {

    private ExecutorService executor;

    private TProcessor processor;

    public CraftMessageHandler(TProcessor processor, ExecutorService executor) {
        this.processor = processor;
        this.executor = executor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CraftMessage message) throws Exception {
        TMessage msg = message.getHeader();
        if (msg.type == TMessageType.REGISTER) {
            //服务端直接忽略客户端发过来的初始化连接消息
            CraftMessage response = new CraftMessage(ctx.channel());
            response.writeMessageBegin(new TMessage(msg.name, TMessageType.REGISTERED, msg.sequence));
            response.writeMessageEnd();
            ctx.writeAndFlush(response);
            return;
        }
        //防止ref被释放，先占用一次，后续会释放
        message.retain();
        executor.submit(new CraftBusinessTask(ctx, processor, message));
    }

}
