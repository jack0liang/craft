package io.craft.proxy.server;

import io.craft.core.codec.CraftFramedMessageDecoder;
import io.craft.core.codec.CraftFramedMessageEncoder;
import io.craft.core.codec.CraftThrowableEncoder;
import io.craft.proxy.handler.ClientMessageHander;
import io.craft.proxy.proxy.ProxyClient;
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

public class CraftProxyServer implements Closeable {

    private EventLoopGroup executors = new NioEventLoopGroup(1);

    private ProxyClient proxyClient = new ProxyClient(
            new Bootstrap()
                    .remoteAddress("127.0.0.1", 1088)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)//TODO 此处的连接超时需要设置
            ,
            executors);

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
                                    .addLast(new ClientMessageHander(proxyClient));
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
