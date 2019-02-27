package io.craft.proxy.discovery;

import io.craft.core.config.EtcdClient;
import io.craft.core.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

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

    public synchronized String findService(String applicationName) throws Exception {
        String path = getPath(applicationName);
        if(app2ServiceList.get(applicationName)==null){
            final ServiceHolder serviceHolder = new ServiceHolder();
            Properties props = etcdClient.watch(path, event->{
                String value = event.getValue();
                serviceHolder.addService(value);
            });
            logger.info(props.toString());
            props.values().forEach(item->serviceHolder.addService((String)item));
            app2ServiceList.put(applicationName,serviceHolder);
        }
        String value = app2ServiceList.get(applicationName).next();
        logger.debug("find service success, path={}, value={}", path,value);
        return value;
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

    private static class ServiceHolder{
        private int pos = 0;
        private List<String> serviceList = new ArrayList<>();

        public ServiceHolder(List<String> serviceList){
            this.serviceList = serviceList;
        }

        public ServiceHolder(){}

        public synchronized void addService(String service){
            if(!serviceList.contains(service))
                serviceList.add(service);
        }

        public synchronized String next(){
            if(pos>=serviceList.size())
                pos = 0;
            return serviceList.get(pos++);
        }
    }
}
