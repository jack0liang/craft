package io.craft;

import io.craft.abc.UserService;
import io.craft.core.codec.CraftFramedMessageDecoder;
import io.craft.core.codec.CraftFramedMessageEncoder;
import io.craft.core.codec.CraftThrowableEncoder;
import io.craft.core.message.CraftFramedMessage;
import io.craft.core.transport.TByteBuf;
import io.craft.server.ChannelTransport;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMap;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TType;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hello world!
 */
@Slf4j
public class App {

    private int port;

    App(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {

        App app = new App(1087);

        app.request();

//        ExecutorService executorService = Executors.newFixedThreadPool(10);
//
//        for(int i=0; i<10000; i++) {
//            executorService.submit(new Runnable() {
//
//                @Override
//                public void run() {
//                    try {
//                        app.request();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//        }


    }

    public void request() throws Exception {

        Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup eventExecutors = new NioEventLoopGroup(10);
        bootstrap
                .group(eventExecutors)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        logger.debug("channel id = {}", ch.id());
                        ch.pipeline()
                                .addLast(new CraftFramedMessageDecoder())
                                .addLast(new CraftFramedMessageEncoder())
                                .addLast(new CraftThrowableEncoder())
                                .addLast(new SimpleChannelInboundHandler<CraftFramedMessage>() {

                                    @Override
                                    protected void channelRead0(ChannelHandlerContext ctx, CraftFramedMessage msg) throws Exception {
                                        ChannelTransport.channels.get(ctx.channel()).writeInt(msg.getBuffer().readableBytes());
                                        ChannelTransport.channels.get(ctx.channel()).writeBytes(msg.getBuffer());
                                        synchronized (ctx.channel()) {
                                            ctx.channel().notify();
                                        }
                                    }
                                })
                        ;
                    }
                });

        ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", port).syncUninterruptibly();

        Channel channel = channelFuture.channel();

        ByteBuf inputBuffer = channel.alloc().directBuffer(1024);

        TTransport outputTransport = new ChannelTransport(channel, inputBuffer);

        TTransport tin = new TFramedTransport(new TByteBuf(inputBuffer));

        TTransport tout = new TFramedTransport(outputTransport);


        TProtocol pin = new TBinaryProtocol(tin);

        TProtocol pout = new TBinaryProtocol(tout);

        //写入messageId
        pout.writeString(UUID.randomUUID().toString().replace("-", ""));
        //写入map
        pout.writeMapBegin(new TMap(TType.STRING, TType.STRING, 1));
        pout.writeString("UID");
        pout.writeString(UUID.randomUUID().toString());
        pout.writeMapEnd();
        UserService.Client client = new UserService.Client(pin, pout);

        List<Long> ids = new ArrayList<>();
        for(int i = 0; i<1000; i++) {
            ids.add(Long.valueOf(i));
        }

        try {
            client.gets(ids);
            logger.debug("request success");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
