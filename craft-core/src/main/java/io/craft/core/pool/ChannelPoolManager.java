package io.craft.core.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ChannelPoolManager {
    private static Map<String, AbstractChannelPoolMap<InetSocketAddress, SimpleChannelPool>> app2PoolMap = new HashMap<>();
    private static Bootstrap bootstrap = new Bootstrap();
    static {
        bootstrap.channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY,true).option(ChannelOption.SO_KEEPALIVE,true);
    }

    /**
     * 必须先调用该方法，初始化EventLoopGroup
     * @param group
     */
    public static void initGroup(EventLoopGroup group){
        bootstrap.group(group);
    }

//    private static class Holder{
//        public static ChannelPoolManager instance = new ChannelPoolManager();
//    }
//    private ChannelPoolManager(){}
//
//    public static ChannelPoolManager getInstance(){
//        return Holder.instance;
//    }

    public static void removeChannel(String applicationName,String serviceAddr){
        InetSocketAddress address = getInetSocketAddress(serviceAddr);
        removeChannel(applicationName,address);
    }

    public static void removeChannel(String applicationName,InetSocketAddress service){
        AbstractChannelPoolMap<InetSocketAddress, SimpleChannelPool> poolMap = initAppPool(applicationName);
        poolMap.get(service).close();
        poolMap.remove(service);
    }

    public static SimpleChannelPool getChannel(String applicationName,String serviceAddr){
        InetSocketAddress address = getInetSocketAddress(serviceAddr);
        return getChannel(applicationName,address);
    }

    private static InetSocketAddress getInetSocketAddress(String serviceAddr) {
        String[] si = serviceAddr.split(":");
        return new InetSocketAddress(si[0], Integer.parseInt(si[1]));
    }

    public static SimpleChannelPool getChannel(String applicationName,InetSocketAddress service){
        ChannelPoolMap<InetSocketAddress, SimpleChannelPool> poolMap = initAppPool(applicationName);
        return poolMap.get(service);
    }

    private static AbstractChannelPoolMap<InetSocketAddress, SimpleChannelPool> initAppPool(String applicationName) {
        AbstractChannelPoolMap<InetSocketAddress, SimpleChannelPool> poolMap = app2PoolMap.get(applicationName);
        if(poolMap==null) {
            synchronized (ChannelPoolManager.class) {
                poolMap = app2PoolMap.get(applicationName);
                if(poolMap==null) {
                    poolMap = new AbstractChannelPoolMap<InetSocketAddress, SimpleChannelPool>() {
                        @Override
                        protected SimpleChannelPool newPool(InetSocketAddress key) {
                            return new FixedChannelPool(bootstrap.remoteAddress(key), new CraftChannelPoolHandler(), 2);
                        }
                    };
                    app2PoolMap.put(applicationName, poolMap);
                }
            }
        }
        return poolMap;
    }

    public static void main(String[] args) {
        initGroup(new NioEventLoopGroup());
        SimpleChannelPool pool = getChannel("abc",new InetSocketAddress(1088));
        Future<Channel> f = pool.acquire();
        f.addListener((FutureListener<Channel>) f1 -> {
            System.out.println("abc.................."+f1);
            if (f1.isSuccess()) {
                System.out.println("def..................");
                Channel ch = f1.getNow();
                ch.writeAndFlush("abc");
                // Release back to pool
                pool.release(ch);
                System.out.println("ghi..................");
            }
        });
        pool.close();
    }
}
