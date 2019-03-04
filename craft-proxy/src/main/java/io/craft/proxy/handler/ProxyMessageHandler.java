package io.craft.proxy.handler;

import io.craft.core.message.CraftFramedMessage;
import io.craft.core.message.TByteBufProtocol;
import io.craft.proxy.discovery.EtcdServiceDiscovery;
import io.craft.proxy.proxy.ProxyClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

import static io.craft.core.constant.Constants.*;

@Slf4j
public class ProxyMessageHandler extends SimpleChannelInboundHandler<CraftFramedMessage> {
    private EtcdServiceDiscovery discovery;
    //private Map<String,PoolChannelHolder> addr2Channel = new HashMap<>();
    private static Map<String, ProxyClient> addr2serverProxy = new HashMap<>();

    public ProxyMessageHandler(EtcdServiceDiscovery discovery) {
        this.discovery = discovery;
    }

//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        super.channelActive(ctx);
//
//    }
//
//    @Override
//    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        logger.debug("channelInactive:"+ctx);
//        addr2Channel.values().stream().forEach(holder->{
//            if(holder.futureChannel.getNow()!=null)
//                holder.pool.release(holder.futureChannel.getNow());
//        });
//    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CraftFramedMessage message) throws Exception {
        ByteBuf buffer = message.getBuffer();
        buffer.markReaderIndex();
        TByteBufProtocol tin = new TByteBufProtocol(buffer);
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
        String serviceAddr = discovery.findService(serviceName);
        ProxyClient serverProxy = getServerProxy(ctx.channel().eventLoop(), serviceAddr);
        serverProxy.write(ctx.channel(),msg,message);
//        PoolChannelHolder holder = addr2Channel.get(serviceAddr);
//        if(holder==null) {
//            SimpleChannelPool pool = ChannelPoolManager.getChannel(serviceName, serviceAddr);
//            Future<Channel> futureChannel = pool.acquire();
//            holder = new PoolChannelHolder(pool,futureChannel);
//            addr2Channel.put(serviceAddr,holder);
//        }
//        Future<Channel> futureChannel = holder.futureChannel;
//        if (futureChannel.isDone()) {
//            proxySend(futureChannel.getNow(),ctx.channel(),message);
//        } else {
//            futureChannel.addListener(future -> proxySend((Channel) future.getNow(),ctx.channel(),message));
//        }
    }

    private ProxyClient getServerProxy(EventLoopGroup group, String serviceAddr) {
        ProxyClient serverProxy = addr2serverProxy.get(serviceAddr);
        if(serverProxy==null){
            synchronized (serviceAddr) {
                serverProxy = addr2serverProxy.get(serviceAddr);
                if(serverProxy==null) {
                    String[] si = serviceAddr.split(":");
                    serverProxy = new ProxyClient(
                            new Bootstrap()
                                    .remoteAddress(si[0], Integer.parseInt(si[1]))
                                    .channel(NioSocketChannel.class)
                                    .option(ChannelOption.TCP_NODELAY, true)
                                    .option(ChannelOption.SO_KEEPALIVE, true)
                                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000),
                            group);
                    addr2serverProxy.put(serviceAddr, serverProxy);
                }
            }
        }
        return serverProxy;
    }

//    private void proxySend(Channel server, Channel client, CraftFramedMessage message){
//        Attribute<Channel> attrClient = server.attr(io.craft.proxy.constant.Constants.SERVER_ATTRIBUTE_CLIENT);
//        attrClient.set(client);
//        server.writeAndFlush(message);
//    }
//
//    private static class PoolChannelHolder{
//        private SimpleChannelPool pool;
//        private Future<Channel> futureChannel;
//        public PoolChannelHolder(SimpleChannelPool pool,Future<Channel> futureChannel){
//            this.pool = pool;
//            this.futureChannel = futureChannel;
//        }
//    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.writeAndFlush(cause);
    }
}
