package io.craft;

import io.craft.abc.UserService;
import io.craft.core.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hello world!
 */
@Slf4j
public class App {

    static {
        System.setProperty("proxy.host", "127.0.0.1");
        System.setProperty("proxy.port", "1089");
        System.setProperty("proxy.connect.timeout", "5000");
    }

    private UserService.Client client;

    App() {
        this.client = new UserService.Client();
    }

    public static void main(String[] args) throws Exception {

//        TimeUnit.SECONDS.sleep(10);

        App app = new App();
        //app.request();

        ExecutorService executorService = Executors.newFixedThreadPool(5);

        for(int i=0; i<100000; i++) {
            executorService.submit(new Runnable() {

                @Override
                public void run() {
                    try {
                        app.request();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }


    }

    public void request() throws Exception {

        TraceUtil.addHeader("UID", "123456");
        for(int i = 0; i<100; i++) {
            TraceUtil.addHeader("HEADER-" + i, "HEADER-VALUE-" + i);
        }

        try {
            long beginTime = System.currentTimeMillis();
            client.ping();
            long endTime = System.currentTimeMillis();
            logger.debug("request success, latency={}ms", (endTime - beginTime));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
