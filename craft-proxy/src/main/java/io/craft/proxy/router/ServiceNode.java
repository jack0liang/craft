package io.craft.proxy.router;

import io.craft.core.lbschedule.WeightedNode;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = {"host", "port"})
public class ServiceNode implements WeightedNode {

    private final String host;

    private final int port;

    private final int weight;

    public ServiceNode(String host, int port, int weight) {
        this.host = host;
        this.port = port;
        this.weight = weight;
    }

    public ServiceNode(String host, int port) {
        this(host, port, 1);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public int weight() {
        return this.weight;
    }

}
