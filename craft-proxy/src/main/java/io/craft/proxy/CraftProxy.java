package io.craft.proxy;


import io.craft.core.config.EtcdClient;
import io.craft.proxy.util.PropertyUtil;

public class CraftProxy {

    private String host;

    private int port;

    private String namespace;

    CraftProxy() {

    }

    public static void main(String... args) throws Exception {
        EtcdClient client = new EtcdClient();

//        String host = PropertyUtil.getProperty("proxy.host");
//        Integer port = Integer.valueOf(PropertyUtil.getProperty("proxy.port"));


        client.setEndpoints(PropertyUtil.getProperty("registry").split(","));
        client.setKeepAlive(true);
        client.init();

        client.watch(PropertyUtil.getProperty("namespace"), event -> {
            System.out.println(event);
        });
    }
}
