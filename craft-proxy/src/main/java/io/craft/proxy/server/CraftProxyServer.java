package io.craft.proxy.server;

import io.craft.core.codec.CraftFramedMessageDecoder;
import io.craft.core.codec.CraftFramedMessageEncoder;
import io.craft.core.codec.CraftThrowableEncoder;
import io.craft.core.config.EtcdClient;
import io.craft.core.util.PropertyUtil;
import io.craft.proxy.handler.ClientMessageHander;
import io.craft.proxy.proxy.ProxyClient;
import io.craft.proxy.router.AbstractRouter;
import io.craft.proxy.router.Router;
import io.craft.proxy.router.ServiceDiscovery;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class CraftProxyServer implements Closeable {

    private EventLoopGroup executors = new NioEventLoopGroup(1);

    private Router router;

    public CraftProxyServer() {
        EtcdClient client = new EtcdClient();
        client.setEndpoints(PropertyUtil.getProperty("registry").split(","));
        client.setKeepAlive(false);
        try {
            client.init();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        ServiceDiscovery discovery = new ServiceDiscovery(client, PropertyUtil.getProperty("namespace"));
        router = new AbstractRouter(discovery) {};
    }

    private void serve(int port) throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            bootstrap.group(executors)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(port)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new CraftFramedMessageDecoder())
                                    .addLast(new CraftFramedMessageEncoder())
                                    .addLast(new CraftThrowableEncoder())
                                    .addLast(new ClientMessageHander(router));
                        }
                    });

            bootstrap
                    .bind()
                    .sync()
                    .channel()
                    .closeFuture()
                    .sync();

        } finally {
            close();
        }
    }


    @Override
    public void close() throws IOException {

    }

    public static void main(String... args) throws Exception {
        System.setProperty("registry", "http://127.0.0.1:2379");
        System.setProperty("namespace", "/dev/");
        CraftProxyServer proxyServer = new CraftProxyServer();
        proxyServer.serve(1089);
    }
}
