package io.craft.proxy.handler;

import io.craft.core.message.CraftFramedMessage;
import io.craft.proxy.proxy.ProxyClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Deprecated
@Slf4j
public class ClientMessageHander extends SimpleChannelInboundHandler<CraftFramedMessage> {

    private ProxyClient proxyClient;

    public ClientMessageHander(ProxyClient proxyClient) {
        super();
        this.proxyClient = proxyClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CraftFramedMessage message) throws Exception {
        /*
        ByteBuf buffer = message.getBuffer();
        buffer.markReaderIndex();
        TByteBuf tin = new TByteBuf(buffer);
        TProtocol pin = new TBinaryProtocol(tin);
        //读消息开始
        TMessage msg = pin.readMessageBegin();
        //方法名
        String function = msg.name;
        //读取入参结构体

        String serviceName = null, traceId = null;
        Map<String, String> header;

        boolean serviceNameIsSet = false, traceIdIsSet = false, headerIsSet = false;

        org.apache.thrift.protocol.TField schemeField;
        pin.readStructBegin();
        while (true) {
            schemeField = pin.readFieldBegin();
            if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                break;
            }
            if (serviceNameIsSet && traceIdIsSet && headerIsSet) {
                //如果所有的必要头都读完了, 则不用再读取其他字段了
                break;
            }
            switch (schemeField.id) {
                case SERVICE_NAME_SEQUENCE: // serviceName STRING
                    serviceNameIsSet = true;
                    if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                        serviceName = pin.readString();
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(pin, schemeField.type);
                    }
                    break;
                case TRACE_ID_SEQUENCE: // traceId STRING
                    traceIdIsSet = true;
                    if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                        traceId = pin.readString();
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(pin, schemeField.type);
                    }
                    break;
                case HEADER_SEQUENCE: // header MAP
                    headerIsSet = true;
                    if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
                        {
                            org.apache.thrift.protocol.TMap _map = pin.readMapBegin();
                            header = new java.util.HashMap<>(2 * _map.size);
                            java.lang.String _key;
                            java.lang.String _val;
                            for (int _i = 0; _i < _map.size; ++_i)
                            {
                                _key = pin.readString();
                                _val = pin.readString();
                                header.put(_key, _val);
                            }
                            pin.readMapEnd();
                        }
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(pin, schemeField.type);
                    }
                    break;
                default:
                    org.apache.thrift.protocol.TProtocolUtil.skip(pin, schemeField.type);
            }
            pin.readFieldEnd();
        }
        pin.readStructEnd();

        Assert.isTrue(!StringUtils.isEmpty(serviceName), "serviceName must not empty");
        Assert.isTrue(!StringUtils.isEmpty(traceId), "traceId must not empty");

        logger.debug("serviceName={}, traceId={}", serviceName, traceId);

        buffer.resetReaderIndex();

        message.retain();

        proxyClient.write(ctx.channel(), msg, message);
        */
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.writeAndFlush(cause);
    }
}
