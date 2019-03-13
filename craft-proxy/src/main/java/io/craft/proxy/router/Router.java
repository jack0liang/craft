package io.craft.proxy.router;

import io.craft.core.lbschedule.LBSchedule;
import io.craft.core.message.CraftMessage;
import io.craft.proxy.proxy.ProxyClient;

public interface Router {

    void addRoute(String serviceName) throws Exception;

    void addRoute(String serviceName, LBSchedule<ServiceNode, ?> loadBalance) throws Exception;

    ProxyClient route(CraftMessage request) throws Exception;

}
