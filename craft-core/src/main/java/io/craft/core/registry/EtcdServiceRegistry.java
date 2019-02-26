package io.craft.core.registry;

import io.craft.core.config.ConfigClient;
import io.craft.core.config.EtcdClient;
import io.craft.core.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EtcdServiceRegistry implements ServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(EtcdServiceRegistry.class);

    private ConfigClient configClient;

    private String applicationNamespace;

    private String applicationName;

    private String host;

    private Integer port;

    private String path;

    public void setConfigClient(ConfigClient configClient) {
        this.configClient = configClient;
    }

    public void setApplicationNamespace(String applicationNamespace) {
        this.applicationNamespace = applicationNamespace;
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

    public void init() {
        this.path = applicationNamespace + applicationName + Constants.HOSTS_PATH + host + ":" + port;
    }

    @Override
    public synchronized void register() throws Exception {
        configClient.put(path, host + ":" + port);
        logger.debug("register success, path={}", path);
    }

    @Override
    public synchronized void close() throws Exception {
        try {
            try {
                configClient.delete(path);
            } finally {
                configClient.close();
            }
            logger.debug("unregister success, path={}", path);
        } catch (Exception e) {
            logger.debug("unregister failed, path={}, error={}", path, e.getMessage(), e);
            throw e;
        }
    }
}
