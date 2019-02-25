package io.craft;

import io.craft.abc.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hello world!
 */
@Slf4j
public class App {


    public static void main(String[] args) throws Exception {
        //启动一个现成watch etcd
//        new Thread() {
//            @Override
//            public void run() {
//                try {
//                    EtcdClient client = new EtcdClient(Client.builder().endpoints("http://127.0.0.1:2379").build(), true);
//                    client.watch("/root/", event -> {
//                        System.out.println(event);
//                    });
//                } catch (Exception e) {
//                    logger.debug(e.getMessage(), e);
//                }
//            }
//        }.start();

        if (true) {
            return;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for(int i=0; i<10000; i++) {
            executorService.submit(new Runnable() {

                @Override
                public void run() {
                    try {
                        request(1085);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public static void request(Integer port) throws Exception {

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
            logger.debug("request success");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        socket.close();
    }
}
