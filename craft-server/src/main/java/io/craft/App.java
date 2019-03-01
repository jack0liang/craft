package io.craft;

import io.craft.abc.UserService;
import io.craft.core.codec.CraftFramedMessageDecoder;
import io.craft.core.codec.CraftFramedMessageEncoder;
import io.craft.core.codec.CraftThrowableEncoder;
import io.craft.core.message.CraftFramedMessage;
import io.craft.core.transport.TByteBuf;
import io.craft.core.util.TraceUtil;
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
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.io.File;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

        App app = new App(1089);

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

        UserService.Client client = new UserService.Client("127.0.0.1", 1088);
        TraceUtil.addHeader("UID", "123456");
        for(int i = 0; i<100; i++) {
            TraceUtil.addHeader("HEADER-" + i, "HEADER-VALUE-" + i);
        }

        try {
            client.ping();
            logger.debug("request success");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
