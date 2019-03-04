package io.craft.proxy.handler;

import io.craft.core.message.CraftFramedMessage;
import io.craft.core.message.TByteBufProtocol;
import io.craft.proxy.proxy.ProxyClient;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.springframework.util.Assert;

import java.util.Map;

import static io.craft.core.constant.Constants.*;

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

        logger.debug("serviceName={}, traceId={}", serviceName, traceId);

        message.retain();

        proxyClient.write(ctx.channel(), messageHeader, message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.writeAndFlush(cause);
    }
}
