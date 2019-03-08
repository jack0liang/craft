package io.craft.proxy.router;

import com.alibaba.fastjson.JSON;
import io.craft.core.config.ConfigClient;
import io.craft.core.config.EtcdClient;
import io.craft.core.constant.Constants;
import io.craft.core.lbschedule.LBSchedule;
import io.craft.proxy.discovery.EtcdServiceDiscovery;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscovery.class);

    private ConfigClient<ConfigClient.NamespaceEvent> client;

    private String namespace;

    private Map<String, Set<ServiceNode>> serviceMap;

    public ServiceDiscovery(ConfigClient client, String namespace) {
        this.client = client;
        this.namespace = namespace;
        this.serviceMap = new ConcurrentHashMap<>();
    }

    public void addWatcher(String serviceName, LBSchedule<ServiceNode, ?> lbSchedule) throws Exception {
        if (serviceMap.containsKey(serviceName)) {
            //如果已经添加过了,就不需要再次添加
            return;
        }
        Set<ServiceNode> nodes = new HashSet<>();
        this.serviceMap.put(serviceName, nodes);
        String prefix = namespace + serviceName + Constants.HOSTS_PATH;
        Properties properties = client.watch(prefix, event -> {
            String key = event.getKey();
            String[] arr = key.split(":");
            String value = event.getValue();
            ServiceNode node = new ServiceNode(arr[0], Integer.valueOf(arr[1]));
            if (StringUtils.isEmpty(value)) {
                //移除
                nodes.remove(node);
            } else {
                //新增
                nodes.add(node);
            }
            lbSchedule.set(nodes.toArray(new ServiceNode[]{}));
            logger.info("nodes={}", JSON.toJSONString(nodes));
        });
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = entry.getKey().toString();
            String[] arr = key.split(":");
            nodes.add(new ServiceNode(arr[0], Integer.valueOf(arr[1])));
        }
        lbSchedule.set(nodes.toArray(new ServiceNode[]{}));
        logger.info("nodes={}", JSON.toJSONString(nodes));
    }

}
