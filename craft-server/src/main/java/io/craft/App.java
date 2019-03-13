package io.craft;

import io.craft.abc.UserService;
import io.craft.abc.model.UserModel;
import io.craft.core.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hello world!
 */
@Slf4j
public class App {

    static {
        System.setProperty("proxy.host", "192.168.11.217");
        System.setProperty("proxy.port", "1088");
        System.setProperty("proxy.connect.timeout", "5000");
    }

    private UserService.Client client;

    App() {
        this.client = new UserService.Client();
    }

    public static void main(String[] args) throws Exception {

//        TimeUnit.SECONDS.sleep(10);

        App app = new App();
//        //app.request();

        int count = 1;

        int threads = 1;
//
        ExecutorService executorService = Executors.newFixedThreadPool(threads);

        CountDownLatch cdl = new CountDownLatch(count);

        long beginTime = System.currentTimeMillis();

        for(int i=0; i<count; i++) {
            executorService.submit(new Runnable() {

                @Override
                public void run() {
                    try {
                        app.request();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        cdl.countDown();
                    }
                }
            });
        }

        cdl.await();

        logger.info("{} threads process {} request take {}ms", threads, count, (System.currentTimeMillis() - beginTime));
    }

    public void request() throws Exception {

        TraceUtil.addCookie("UID", "123456");
        for(int i = 0; i<100; i++) {
            TraceUtil.addCookie("HEADER-" + i, "HEADER-VALUE-" + i);
        }

        try {
            long beginTime = System.currentTimeMillis();
            UserModel model = client.get(System.currentTimeMillis());
            long endTime = System.currentTimeMillis();
            logger.debug("request success, latency={}ms, response={}", (endTime - beginTime), model);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
