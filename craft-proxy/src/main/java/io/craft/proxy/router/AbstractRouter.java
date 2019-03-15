package io.craft.proxy.router;

import io.craft.core.lbschedule.LBSchedule;
import io.craft.core.lbschedule.WeightedRoundRobinSchedule;
import io.craft.core.message.CraftMessage;
import io.craft.core.thrift.TService;
import io.craft.proxy.proxy.ProxyClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractRouter implements Router {

    private ServiceDiscovery discovery;

    private Map<String, LBSchedule<ServiceNode, ?>> routerMap;

    private Map<ServiceNode, ProxyClient> proxies;

    private EventLoopGroup executors;

    protected AbstractRouter(ServiceDiscovery discovery) {
        this.discovery = discovery;
        this.routerMap = new ConcurrentHashMap<>();
        this.proxies = new ConcurrentHashMap<>();
        this.executors = new NioEventLoopGroup(2);
    }

    @Override
    public void addRoute(String serviceName, LBSchedule<ServiceNode, ?> loadBalance) throws Exception {
        synchronized (serviceName) {
            if (routerMap.containsKey(serviceName)) {
                return;
            }
            discovery.addWatcher(serviceName, loadBalance);
            routerMap.put(serviceName, loadBalance);
        }
    }

    @Override
    public void addRoute(String serviceName) throws Exception {
        addRoute(serviceName, new WeightedRoundRobinSchedule<>(new ServiceNode[]{}));
    }

    @Override
    public ProxyClient route(CraftMessage request) throws Exception {
        TService service = request.getService();
        if (!routerMap.containsKey(service.name)) {
            throw new Exception("proxy not init");
        }
        ServiceNode node = routerMap.get(service.name).get();
        if (node == null) {
            throw new Exception("can not find any service node");
        }
        ProxyClient proxy;
        if (proxies.containsKey(node)) {
            //如果proxy已经正常建立了的话,省去了初始化的过程
            proxy = proxies.get(node);
        } else {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.remoteAddress(node.getHost(), node.getPort())
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);//TODO 此处的连接超时需要设置
            proxy = new ProxyClient(bootstrap, executors);
            ProxyClient old = proxies.putIfAbsent(node, proxy);
            if (old != null) {
                proxy = old;
            }
        }

        return proxy;
    }
}
