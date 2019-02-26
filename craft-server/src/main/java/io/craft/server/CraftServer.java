package io.craft.server;

import io.craft.core.codec.CraftFramedMessageDecoder;
import io.craft.core.codec.CraftFramedMessageEncoder;
import io.craft.core.codec.CraftThrowableEncoder;
import io.craft.core.constant.Constants;
import io.craft.core.registry.ServiceRegistry;
import io.craft.core.spring.PropertyManager;
import io.craft.server.executor.CraftBusinessExecutor;
import io.craft.server.executor.CraftRejectedExecutionHandler;
import io.craft.server.executor.CraftThreadFactory;
import io.craft.server.handler.CraftMessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import sun.misc.Signal;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CraftServer implements Closeable {

    private ConfigurableApplicationContext applicationContext;

    private Channel channel;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private ExecutorService businessExecutor;

    private ServiceRegistry registry;

    private PropertyManager propertyManager;

    private void serve() throws Throwable {

        applicationContext = new ClassPathXmlApplicationContext("service.xml");

        propertyManager = applicationContext.getBean(PropertyManager.class);

        String host = propertyManager.getProperty(Constants.APPLICATION_HOST);

        int port = Integer.valueOf(propertyManager.getProperty(Constants.APPLICATION_PORT));

        TProcessor processor = applicationContext.getBean(TProcessor.class);

        CraftFramedMessageEncoder craftFramedMessageEncoder = new CraftFramedMessageEncoder();

        CraftThrowableEncoder exceptionEncoder = new CraftThrowableEncoder();

        ServerBootstrap bootstrap = new ServerBootstrap();

        businessExecutor = new CraftBusinessExecutor(100, 20000, new CraftRejectedExecutionHandler());

        bossGroup = new NioEventLoopGroup(10, new CraftThreadFactory("craft-boss-"));

        workerGroup = new NioEventLoopGroup(10, new CraftThreadFactory("craft-worker-"));

        CraftMessageHandler craftMessageHandler = new CraftMessageHandler(processor, businessExecutor);

        try {

            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(host, port))
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            logger.debug("channel id = {}", ch.id());
                            ch.pipeline()
                                    .addLast(new CraftFramedMessageDecoder())
                                    .addLast(craftFramedMessageEncoder)
                                    .addLast(exceptionEncoder)
                                    .addLast(craftMessageHandler)
                            ;
                        }
                    });
            ChannelFuture f = bootstrap.bind().sync();
            //端口监听成功
            logger.debug("server start success on {}:{}", host, port);

            registry = applicationContext.getBean(ServiceRegistry.class);
            registry.register();
            logger.debug("service register success");

            CraftTermHandler termHandler = new CraftTermHandler(this);

            Signal.handle(new Signal("TERM"), termHandler);
            Signal.handle(new Signal("INT"), termHandler);
            logger.debug("signal handler succsss");

            channel = f.channel();

            channel.closeFuture().sync();
        } finally {
            close();
        }
    }

    @Override
    public synchronized void close() throws IOException {
        //先反注册服务
        try {
            if (registry != null) {
                registry.close();
                registry = null;
                logger.debug("unregister service successful");
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
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
        //关闭business thread pool
        try {
            if (businessExecutor != null) {
                businessExecutor.shutdown();
                businessExecutor = null;
                logger.debug("business thread pool close successful");
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
        //关闭spring容器
        if (applicationContext != null) {
            applicationContext.close();
            applicationContext = null;
            logger.debug("spring container close successful");
        }
    }

    public static void main(String... args) throws Throwable {
        CraftServer server = new CraftServer();
        server.serve();
    }
}
