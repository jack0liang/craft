package io.craft.proxy.handler;

import io.craft.core.constant.Constants;
import io.craft.core.message.CraftFramedMessage;
import io.craft.proxy.proxy.ProxyClient;
import io.craft.proxy.router.Router;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.protocol.TMessage;

import java.util.Map;

@Slf4j
public class ClientMessageHander extends SimpleChannelInboundHandler<CraftFramedMessage> {

    private Router router;

    public ClientMessageHander(Router router) {
        super();
        this.router = router;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CraftFramedMessage message) throws Exception {
        TMessage msg = message.getMessageHeader();
        if (msg.type == Constants.MESSAGE_TYPE_INIT) {
            //如果proxy收到的是客户端的连接消息, 则进行服务的初始化处理
            router.addRoute(msg.name);
            CraftFramedMessage response = new CraftFramedMessage(ctx.channel());
            response.writeMessageBegin(new TMessage(msg.name, Constants.MESSAGE_TYPE_INIT, msg.seqid));
            response.writeMessageEnd();
            ctx.writeAndFlush(response);
            return;
        }

        String serviceName = message.getServiceName();
        String traceId = message.getTraceId();
        Map<String, String> header = message.getServiceHeader();

        TMessage messageHeader = message.getMessageHeader();

        logger.debug("serviceName={}, traceId={}, header={}", serviceName, traceId, header);

        message.retain();
        router.route(message).write(ctx.channel(), messageHeader, message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        //客户端请求发生异常的时候,关闭连接
        ctx.close();
    }
}
