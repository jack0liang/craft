package io.craft.server;

import io.craft.core.codec.CraftFramedMessageDecoder;
import io.craft.core.codec.CraftFramedMessageEncoder;
import io.craft.core.codec.CraftThrowableEncoder;
import io.craft.server.executor.CraftBusinessExecutor;
import io.craft.server.executor.CraftRejectedExecutionHandler;
import io.craft.server.executor.CraftThreadFactory;
import io.craft.server.handler.CraftMessageHandler;
import io.craft.server.register.EtcdServiceRegister;
import io.craft.server.register.ServiceRegister;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import sun.misc.Signal;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CraftServer implements Closeable {

    private ConfigurableApplicationContext applicationContext;

    private Channel channel;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private ExecutorService businessThreadPool;

    private ServiceRegister serviceRegister;

    private void serve(Properties properties) throws Throwable {

        Integer port = Integer.valueOf(properties.getProperty("port"));//服务端口

        applicationContext = new ClassPathXmlApplicationContext("service.xml");
        TProcessor processor = applicationContext.getBean(TProcessor.class);

        CraftFramedMessageEncoder craftFramedMessageEncoder = new CraftFramedMessageEncoder();

        CraftThrowableEncoder exceptionEncoder = new CraftThrowableEncoder();

        ServerBootstrap bootstrap = new ServerBootstrap();

        businessThreadPool = new CraftBusinessExecutor(100, 20000, new CraftRejectedExecutionHandler());

        bossGroup = new NioEventLoopGroup(10, new CraftThreadFactory("craft-boss-"));

        workerGroup = new NioEventLoopGroup(10, new CraftThreadFactory("craft-worker-"));

        CraftMessageHandler craftMessageHandler = new CraftMessageHandler(processor, businessThreadPool);

        try {

            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
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
            logger.debug("listen port success");

            serviceRegister = new EtcdServiceRegister("root", properties);
            serviceRegister.register();

            Signal.handle(new Signal("TERM"), new CraftSignalHandler(this));
            Signal.handle(new Signal("INT"), new CraftSignalHandler(this));

            channel = f.channel();

            channel.closeFuture().sync();
        } finally {
            close();
        }
    }

    @Override
    public synchronized void close() throws IOException {
        //先反注册服务
        if (serviceRegister != null) {
            serviceRegister.close();
            serviceRegister = null;
            logger.debug("unregister service successful");
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
            if (businessThreadPool != null) {
                businessThreadPool.shutdown();
                businessThreadPool = null;
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
        Properties properties = new Properties(System.getProperties());
        for(String arg : args) {
            if (StringUtils.isBlank(arg)) {
                continue;
            }
            arg = StringUtils.trim(arg);
            int idx = arg.indexOf("=");
            if (idx == -1) {
                properties.setProperty(arg, null);
            } else {
                properties.setProperty(arg.substring(0, idx), arg.substring(idx+1));
            }
        }

        CraftServer server = new CraftServer();
        server.serve(properties);
    }
}
