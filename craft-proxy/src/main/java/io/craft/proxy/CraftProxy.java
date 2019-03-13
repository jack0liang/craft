package io.craft.proxy;


import io.craft.core.codec.CraftFramedMessageDecoder;
import io.craft.core.codec.CraftFramedMessageEncoder;
import io.craft.core.config.EtcdClient;
import io.craft.core.constant.Constants;
import io.craft.proxy.discovery.EtcdServiceDiscovery;
import io.craft.proxy.handler.ProxyMessageHandler;
import io.craft.proxy.pool.ChannelPoolManager;
import io.craft.proxy.util.PropertyUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CraftProxy implements Closeable {
//    private ConfigurableApplicationContext applicationContext;
//    private PropertyManager propertyManager;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;

    public void proxy() throws InterruptedException, IOException, ExecutionException {
//        applicationContext = new ClassPathXmlApplicationContext("proxy.xml");
//        propertyManager = applicationContext.getBean(PropertyManager.class);
        EtcdServiceDiscovery discovery = initServiceDiscovery();

        int port = Integer.valueOf(PropertyUtil.getProperty(Constants.APPLICATION_PORT));

        ServerBootstrap bootstrap = new ServerBootstrap();

        bossGroup = new NioEventLoopGroup();

        workerGroup = new NioEventLoopGroup();
        try {
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(port)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            logger.info("channel id = {}", ch.id());
                            ch.pipeline()
                                    .addLast(new CraftFramedMessageDecoder())
                                    .addLast(new CraftFramedMessageEncoder())
                                    .addLast(new ProxyMessageHandler(discovery));
                        }
                    });
            ChannelFuture f = bootstrap.bind().sync();
            //端口监听成功
            logger.debug("proxy start success on port:{}", port);
            ChannelPoolManager.initGroup(workerGroup);
            channel = f.channel();

            channel.closeFuture().sync();
        } finally {
            close();
        }
    }

    private EtcdServiceDiscovery initServiceDiscovery() throws ExecutionException, InterruptedException {
        EtcdClient etcdClient = new EtcdClient();
        etcdClient.setKeepAlive(true);
        String endpoints = PropertyUtil.getProperty("etcd.endpoints");
        etcdClient.setEndpoints(endpoints.split(","));
        etcdClient.init();
        EtcdServiceDiscovery discovery = new EtcdServiceDiscovery();
        discovery.setApplicationNamespace(PropertyUtil.getProperty(Constants.APPLICATION_NAMESPACE));
        discovery.setEtcdClient(etcdClient);
        return discovery;
    }

    @Override
    public synchronized void close() throws IOException {
        //等待1s, 避免有些还没感知到服务下线
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new IOException(e.getMessage(), e);
        }
        //关闭端口监听
        try {
            if (channel != null) {
                channel.close().sync();
                channel = null;
                logger.debug("channel close successful");
            }
        } catch (InterruptedException e) {
            throw new IOException(e.getMessage(), e);
        }
        //关闭boss event loop
        try {
            if (bossGroup != null) {
                bossGroup.shutdownGracefully().sync();
                bossGroup = null;
                logger.debug("boss group close successful");
            }
        } catch (InterruptedException e) {
            throw new IOException(e.getMessage(), e);
        }
        //关闭worker event loop
        try {
            if (workerGroup != null) {
                workerGroup.shutdownGracefully().sync();
                workerGroup = null;
                logger.debug("worker group close successful");
            }
        } catch (InterruptedException e) {
            throw new IOException(e.getMessage(), e);
        }
        //关闭spring容器
//        if (applicationContext != null) {
//            applicationContext.close();
//            applicationContext = null;
//            logger.debug("spring container close successful");
//        }
    }

    public static void main(String... args) throws Exception {
        new CraftProxy().proxy();
    }
}
