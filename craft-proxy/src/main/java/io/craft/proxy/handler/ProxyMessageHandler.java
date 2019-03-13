package io.craft.proxy.handler;

import io.craft.core.constant.Constants;
import io.craft.core.message.CraftFramedMessage;
import io.craft.proxy.discovery.EtcdServiceDiscovery;
import io.craft.proxy.proxy.ProxyClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.protocol.TMessage;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ProxyMessageHandler extends SimpleChannelInboundHandler<CraftMessage> {
    private FindService findService;
    //private Map<String,PoolChannelHolder> addr2Channel = new HashMap<>();
    private static Map<String, ProxyClient> addr2serverProxy = new HashMap<>();

    public ProxyMessageHandler(FindService findService) {
        this.findService = findService;
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
    protected void channelRead0(ChannelHandlerContext ctx, CraftMessage message) throws Exception {

        TService service = message.getService();
        //Map<String, String> header;

        Assert.isTrue(!StringUtils.isEmpty(service.name), "name must not empty");
        Assert.isTrue(!StringUtils.isEmpty(service.traceId), "traceId must not empty");

        logger.debug("name={}, traceId={}", service.name, service.traceId);

        message.retain();
        String serviceAddr = findService.find(serviceName);
        ProxyClient serverProxy = getServerProxy(ctx.channel().eventLoop(), serviceAddr);
        serverProxy.write(ctx.channel(),message.getHeader(),message);
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
