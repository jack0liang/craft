package io.craft.proxy.handler;

import io.craft.core.message.CraftFramedMessage;
import io.craft.proxy.proxy.ProxyClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.protocol.TMessage;

import java.util.Map;

@Slf4j
public class ClientMessageHander extends SimpleChannelInboundHandler<CraftFramedMessage> {

    private ProxyClient proxyClient;

    public ClientMessageHander(ProxyClient proxyClient) {
        super();
        this.proxyClient = proxyClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CraftFramedMessage message) throws Exception {
        String serviceName = message.getServiceName();
        String traceId = message.getTraceId();
        Map<String, String> header = message.getServiceHeader();

        TMessage messageHeader = message.getMessageHeader();

        logger.debug("serviceName={}, traceId={}, header={}", serviceName, traceId, header);

        message.retain();
        proxyClient.write(ctx.channel(), messageHeader, message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        //客户端请求发生异常的时候,关闭连接
        ctx.close();
    }
}
