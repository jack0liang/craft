package io.craft.proxy;

import io.craft.core.codec.CraftFramedMessageDecoder;
import io.craft.core.codec.CraftFramedMessageEncoder;
import io.craft.core.config.EtcdClient;
import io.craft.core.util.PropertyUtil;
import io.craft.proxy.handler.ClientMessageHander;
import io.craft.proxy.router.AbstractRouter;
import io.craft.proxy.router.Router;
import io.craft.proxy.router.ServiceDiscovery;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;

@Slf4j
public class CraftProxy {

    private EventLoopGroup bossGroup = new NioEventLoopGroup(1);

    private EventLoopGroup workerGroup = new NioEventLoopGroup(2);

    private Router router;

    private Channel channel;

    public CraftProxy() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            close();
        }));
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
        bootstrap.group(bossGroup, workerGroup)
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
                                .addLast(new ClientMessageHander(router));
                    }
                });

        channel = bootstrap
                .bind()
                .sync()
                .channel();

        logger.info("proxy start, listen port {}", port);
        channel.closeFuture().sync();
    }


    public void close() {
        logger.info("proxy shutdown...");
        channel.close();
    }

    public static void main(String... args) throws Exception {
        System.setProperty("registry", "http://127.0.0.1:2379");
        System.setProperty("namespace", "/dev/");
        CraftProxy proxyServer = new CraftProxy();
        proxyServer.serve(1089);
    }
}
