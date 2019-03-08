package io.craft.proxy.router;

import io.craft.core.lbschedule.LBSchedule;
import io.craft.core.message.CraftFramedMessage;
import io.craft.proxy.proxy.ProxyClient;

import java.net.InetSocketAddress;

public interface Router {

    void addRoute(String serviceName) throws Exception;

    void addRoute(String serviceName, LBSchedule<ServiceNode, ?> loadBalance) throws Exception;

    ProxyClient route(CraftFramedMessage request) throws Exception;

}
