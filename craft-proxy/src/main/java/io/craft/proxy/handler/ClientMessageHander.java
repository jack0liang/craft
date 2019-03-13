package io.craft.proxy.handler;

import io.craft.core.message.CraftMessage;
import io.craft.core.thrift.TMessage;
import io.craft.core.thrift.TMessageType;
import io.craft.core.thrift.TService;
import io.craft.proxy.router.Router;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientMessageHander extends SimpleChannelInboundHandler<CraftMessage> {

    private Router router;

    public ClientMessageHander(Router router) {
        super();
        this.router = router;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CraftMessage message) throws Exception {
        TMessage msg = message.getHeader();
        if (msg.type == TMessageType.REGISTER) {
            //如果proxy收到的是客户端的连接消息, 则进行服务的初始化处理
            router.addRoute(msg.name);
            CraftMessage response = new CraftMessage(ctx.channel());
            response.writeMessageBegin(new TMessage(msg.name, TMessageType.REGISTERED, msg.sequence));
            response.writeMessageEnd();
            ctx.writeAndFlush(response);
            return;
        }

        TService service = message.getService();

        TMessage header = message.getHeader();

        logger.debug("name={}, traceId={}, cookie={}", service.name, service.traceId, service.cookie);

        message.retain();
        router.route(message).write(ctx.channel(), header, message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        //客户端请求发生异常的时候,关闭连接
        ctx.close();
    }
}
