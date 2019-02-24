package io.craft.core.registry;

import io.craft.core.config.EtcdClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.LifecycleProcessor;

public class EtcdServiceRegistry implements ServiceRegistry, LifecycleProcessor {

    private static final Logger logger = LoggerFactory.getLogger(EtcdServiceRegistry.class);

    private String root;

    private String applicationName;

    private String host;

    private Integer port;

    private String endpoints;

    private EtcdClient client;

    private String path;

    public void setRoot(String root) {
        this.root = root;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setEndpoints(String endpoints) {
        this.endpoints = endpoints;
    }

    @Override
    public void onRefresh() {
        logger.debug("onRefresh");
    }

    @Override
    public void onClose() {
        logger.debug("onClose");
    }

    @Override
    public void start() {
        logger.debug("start");
    }

    @Override
    public void stop() {
        logger.debug("stop");
    }

    @Override
    public boolean isRunning() {
        logger.debug("isRunning");
        return false;
    }

    @Override
    public synchronized void register() throws Exception {
        path = root + applicationName + "/" + host + ":" + port;
        client = new EtcdClient();
        client.setEndpoints(endpoints.split(","));
        client.setKeepAlive(true);
        client.init();
        client.put(path, host + ":" + port);
        logger.debug("register success, path={}", path);
    }

    @Override
    public synchronized void close() throws Exception {
        try {
            try {
                client.delete(path);
            } finally {
                client.close();
            }
            logger.debug("unregister success, path={}", path);
        } catch (Exception e) {
            logger.debug("unregister failed, path={}, error={}", path, e.getMessage(), e);
            throw e;
        }
    }
}
