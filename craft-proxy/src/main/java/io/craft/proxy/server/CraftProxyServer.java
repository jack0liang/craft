package io.craft.proxy.server;

import io.craft.core.codec.CraftFramedMessageDecoder;
import io.craft.proxy.handler.ClientMessageHander;
import io.craft.proxy.proxy.Proxy;
import io.craft.proxy.proxy.ProxyImpl;
import io.craft.proxy.proxy.ProxyPoolHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;

public class CraftProxyServer implements Closeable {

    private EventLoopGroup executors;

    private Proxy proxy = new ProxyImpl(new InetSocketAddress("127.0.0.1", 1088), new NioEventLoopGroup(), new ProxyPoolHandler());

    private void serve(int port) throws Exception {
        executors = new NioEventLoopGroup(2);
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
                                    .addLast(new ClientMessageHander(proxy));
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
        CraftProxyServer proxyServer = new CraftProxyServer();
        proxyServer.serve(1089);
    }
}
