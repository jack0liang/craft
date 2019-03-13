package io.craft.proxy.discovery;

import io.craft.core.config.EtcdClient;
import io.craft.core.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

@Component
public class EtcdServiceDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(EtcdServiceDiscovery.class);

    @Autowired
    private EtcdClient etcdClient;

    @Value("${application.namespace}")
    private String applicationNamespace;

    private Map<String, ServiceHolder> app2ServiceList = new HashMap<>();

    public void setEtcdClient(EtcdClient etcdClient) {
        this.etcdClient = etcdClient;
    }

    public void setApplicationNamespace(String applicationNamespace) {
        this.applicationNamespace = applicationNamespace;
    }

    private String getPath(String applicationName) {
        return applicationNamespace + applicationName + Constants.HOSTS_PATH ;
    }

    public ServiceHolder getServiceHolder(String applicationName) throws ExecutionException, InterruptedException {
        initServiceList(applicationName);
        return app2ServiceList.get(applicationName);
    }

    public String findService(String applicationName) throws Exception {
        String path = initServiceList(applicationName);
        String value = app2ServiceList.get(applicationName).next();
        logger.debug("find service success, path={}, value={}", path,value);
        return value;
    }

    private String initServiceList(String applicationName) throws ExecutionException, InterruptedException {
        String path = getPath(applicationName);
        if(app2ServiceList.get(applicationName)==null){
            synchronized (app2ServiceList) {
                if(app2ServiceList.get(applicationName)==null) {
                    final ServiceHolder serviceHolder = new ServiceHolder(applicationName);
                    Properties props = etcdClient.watch(path, event -> {
                        String value = event.getValue();
                        String oldValue = event.getOldValue();
                        serviceHolder.addService(value, oldValue);
                    });
                    logger.info(props.toString());
                    props.values().forEach(item->serviceHolder.addService((String)item,null));
                    app2ServiceList.put(applicationName, serviceHolder);
                }
            }
        }
        return path;
    }

    public synchronized void close() throws Exception {
        try {
            etcdClient.close();
            logger.debug("close proxy");
        } catch (Exception e) {
            logger.debug("close failed, error={}", e.getMessage(), e);
            throw e;
        }
    }

}
