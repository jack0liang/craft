package io.craft;

import io.craft.abc.UserService;
import io.craft.core.codec.CraftThrowableEncoder;
import io.craft.core.message.CraftFramedMessage;
import io.craft.server.handler.CraftMessageHandler;
import io.craft.core.codec.CraftFramedMessageDecoder;
import io.craft.core.codec.CraftFramedMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hello world!
 */
@Slf4j
public class App {

    private static App app;

    public static void main(String[] args) throws Exception {

        new Thread() {

            @Override
            public void run() {
                try {
                    app = new App();
                    app.serve(1085);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

        TimeUnit.SECONDS.sleep(2);

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for(int i=0; i<10000; i++) {
            executorService.submit(new Runnable() {

                @Override
                public void run() {
                    try {
                        app.request(1085);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void request(Integer port) throws Exception {

        Socket socket = new Socket("127.0.0.1", port);
        socket.setReuseAddress(true);

        TSocket transport = new TSocket(socket);

        transport.setTimeout(5000);

        // 协议要和服务端一致

        //transport.open();

        TTransport tin = new TFramedTransport(new TIOStreamTransport(socket.getInputStream()));

        TTransport tout = new TFramedTransport(new TIOStreamTransport(socket.getOutputStream()));

        TProtocol pin = new TBinaryProtocol(tin);

        TProtocol pout = new TBinaryProtocol(tout);
        UserService.Client client = new UserService.Client(pin, pout);

        List<Long> ids = new ArrayList<>();
        for(int i = 0; i<1000; i++) {
            ids.add(Long.valueOf(i));
        }

        try {
            client.gets(ids);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        socket.close();
    }

    public void serve(Integer port) throws InterruptedException {

        UserService service = new UserServiceImpl();

        TProcessor processor = new UserService.Processor<>(service);

        EventLoopGroup bossGroup = new NioEventLoopGroup(10, new ThreadFactory() {

            private AtomicInteger count = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "craft-boss-" + count.getAndIncrement());
            }
        });

        EventLoopGroup workerGroup = new NioEventLoopGroup(10, new ThreadFactory() {

            private AtomicInteger count = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "craft-worker-" + count.getAndIncrement());
            }
        });

        CraftFramedMessageEncoder craftFramedMessageEncoder = new CraftFramedMessageEncoder();

        CraftMessageHandler craftMessageHandler = new CraftMessageHandler(processor);

        CraftThrowableEncoder exceptionEncoder = new CraftThrowableEncoder();

        try {
            ServerBootstrap b = new ServerBootstrap();        //1

            b.group(bossGroup, workerGroup)                                    //2
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            logger.debug("channel id = {}", ch.id());
                            ch.pipeline()
                                    .addLast(new CraftFramedMessageDecoder())
                                    .addLast(new MessageToMessageDecoder<CraftFramedMessage>() {
                                        @Override
                                        protected void decode(ChannelHandlerContext ctx, CraftFramedMessage msg, List<Object> out) throws Exception {
                                            out.add(msg);
                                        }
                                    })
                                    .addLast(craftFramedMessageEncoder)
                                    .addLast(exceptionEncoder)
                                    .addLast(craftMessageHandler)
                                    ;
                        }
                    });
            ChannelFuture f = b.bind().sync();  //6
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully().sync();
            bossGroup.shutdownGracefully().sync();        //7
        }
    }
}
