package io.craft.server.register;

import com.alibaba.fastjson.JSON;
import io.craft.core.etcd.EtcdClient;
import io.craft.core.util.IPUtil;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.kv.PutResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class EtcdServiceRegister implements ServiceRegister {

    private static final int TTL = 5;

    private final Properties properties;

    private final String root;

    private final ScheduledExecutorService heartbeat = Executors.newScheduledThreadPool(1);

    private final String application;

    private final String ip;

    private final Integer port;

    private final URI[] servers;

    private final EtcdClient client;

    private final String path;


    public EtcdServiceRegister(String root, Properties properties) throws Exception {
        this.root = root;
        this.properties = properties;

        this.application = properties.getProperty("application");
        String network = properties.getProperty("network");
        String[] register = properties.getProperty("register").split(";");
        this.port = Integer.valueOf(properties.getProperty("port"));
        this.ip = IPUtil.getIPV4(network);

        if (StringUtils.isEmpty(this.ip)) {
            throw new IllegalArgumentException("无法根据网卡名获取到IP");
        }

        List<URI> etcdServerList = new ArrayList<>(register.length);
        for(String server : register) {
            etcdServerList.add(URI.create(server));
        }

        this.servers = etcdServerList.toArray(new URI[]{});

        this.client = new EtcdClient(Client.builder().endpoints(servers).build(), true);

        this.path = root + "/" + application+"/"+ip+":"+port;
    }

    public void register0() throws Exception {
        PutResponse response = client.put(path, ip + ":" + port);
        logger.debug("register success response={}", JSON.toJSONString(response));
    }

    @Override
    public void register() throws Throwable {
        //注册节点，ttl=TTL
        register0();
        //每隔1秒执行一次心跳去刷新
        heartbeat.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    register0();
                } catch (Throwable t) {
                    logger.error("register refresh error:{}", t.getMessage(), t);
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
        logger.debug("register success");
    }

    @Override
    public void close() throws IOException {
        heartbeat.shutdown();
        try {
            client.delete(path);
        } catch (Exception e) {
            logger.error("delete directory error:{}", e.getMessage(), e);
        }
        client.close();
    }
}
