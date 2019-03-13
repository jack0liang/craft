package io.craft.server;

import io.craft.core.codec.CraftFramedMessageDecoder;
import io.craft.core.codec.CraftFramedMessageEncoder;
import io.craft.core.constant.Constants;
import io.craft.core.registry.ServiceRegistry;
import io.craft.core.spring.PropertyManager;
import io.craft.core.thrift.TProcessor;
import io.craft.core.util.IPUtil;
import io.craft.server.executor.CraftBusinessExecutor;
import io.craft.server.executor.CraftRejectedExecutionHandler;
import io.craft.server.executor.CraftThreadFactory;
import io.craft.server.handler.CraftMessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.concurrent.ExecutorService;

@Slf4j
public class CraftServer {

    private static final String SERVICE_AUTOCONFIG_TEST = "service-test.xml";

    private static final String SERVICE_AUTOCONFIG = "service.xml";

    private volatile ConfigurableApplicationContext applicationContext;

    private String host;

    private Integer port;

    private volatile Channel channel;

    private volatile EventLoopGroup bossGroup;

    private volatile EventLoopGroup workerGroup;

    private volatile ExecutorService businessExecutor;

    private volatile ServiceRegistry registry;

    private PropertyManager propertyManager;

    private URL findURLOfDefaultConfigurationFile() {
        URL url;
        url = getResource(SERVICE_AUTOCONFIG_TEST);
        if (url != null) {
            return url;
        }

        return getResource(SERVICE_AUTOCONFIG);
    }

    private URL getResource(String filename) {
        ClassLoader loader = this.getClass().getClassLoader();
        return loader.getResource(filename);
    }

    /**
     * 创建spring context
     */
    private void createApplicationContext() throws Exception {
        URL configLocation = findURLOfDefaultConfigurationFile();
        if (configLocation == null) {
            throw new Exception("xml config not found");
        }
        applicationContext = new ClassPathXmlApplicationContext("service.xml");
        propertyManager = applicationContext.getBean(PropertyManager.class);
        if (propertyManager == null) {
            throw new Exception("property manager init failed");
        }
        logger.info("start 1/3 create spring container done");
    }

    /**
     * 监听端口
     */
    private void listen() throws Exception {
        host = IPUtil.getLocalIPV4();
        port = Integer.valueOf(propertyManager.getProperty(Constants.APPLICATION_PORT));
        TProcessor processor = applicationContext.getBean(TProcessor.class);
        CraftFramedMessageEncoder craftFramedMessageEncoder = new CraftFramedMessageEncoder();
        ServerBootstrap bootstrap = new ServerBootstrap();
        businessExecutor = new CraftBusinessExecutor(100, 20000, new CraftRejectedExecutionHandler());
        bossGroup = new NioEventLoopGroup(10, new CraftThreadFactory("craft-boss-"));
        workerGroup = new NioEventLoopGroup(10, new CraftThreadFactory("craft-worker-"));
        CraftMessageHandler craftMessageHandler = new CraftMessageHandler(processor, businessExecutor);

        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(host, port))
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new CraftFramedMessageDecoder())
                                .addLast(craftFramedMessageEncoder)
                                .addLast(craftMessageHandler)
                        ;
                    }
                });

        channel = bootstrap.bind().sync().channel();
        logger.info("start 2/3 listen port done");
    }

    /**
     * 服务注册到注册中心
     */
    private void register() throws Exception {
        registry = applicationContext.getBean(ServiceRegistry.class);
        registry.register();
        logger.info("start 3/3 register service done");
        channel.closeFuture().sync();
    }

    private void serve() throws Throwable {
        //注册钩子,在服务关闭的时候关闭容器
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                close();
            } catch (Exception e) {
                logger.error("server close error={}", e.getMessage(), e);
            }
        }));
        createApplicationContext();
        listen();
        register();
    }

    public synchronized void close() throws Exception {
        //先反注册服务
        try {
            if (registry != null) {
                registry.close();
                registry = null;
            }
            logger.info("close 1/7 unregister service done");
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
//        //等待1s, 避免有些还没感知到服务下线
//        try {
//            TimeUnit.SECONDS.sleep(10);
//            logger.info("close 2/7 wait unregister sync done");
//        } catch (InterruptedException e) {
//            throw new IOException(e.getMessage(), e);
//        }
        //关闭端口监听
        try {
            if (channel != null) {
                channel.close().sync();
                channel = null;
            }
            logger.info("close 3/7 close port done");
        } catch (InterruptedException e) {
            throw new IOException(e.getMessage(), e);
        }
        //关闭boss event loop
        try {
            if (bossGroup != null) {
                bossGroup.shutdownGracefully().sync();
                bossGroup = null;
            }
            logger.info("close 4/7 boss shutdown done");
        } catch (InterruptedException e) {
            throw new IOException(e.getMessage(), e);
        }
        //关闭worker event loop
        try {
            if (workerGroup != null) {
                workerGroup.shutdownGracefully().sync();
                workerGroup = null;
            }
            logger.info("close 5/7 worker shutdown done");
        } catch (InterruptedException e) {
            throw new IOException(e.getMessage(), e);
        }
        //关闭business thread pool
        try {
            if (businessExecutor != null) {
                businessExecutor.shutdown();
                businessExecutor = null;
            }
            logger.info("close 6/7 business shutdown done");
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
        //关闭spring容器
        if (applicationContext != null) {
            applicationContext.close();
            applicationContext = null;
        }
        logger.info("close 7/7 spring container close done");
    }

    public static void main(String... args) throws Throwable {
        CraftServer server = new CraftServer();
        server.serve();
    }
}
