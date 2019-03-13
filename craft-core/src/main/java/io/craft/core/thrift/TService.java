package io.craft.core.thrift;

import java.util.Map;

public class TService {

    public final String name;

    public final String traceId;

    public final Map<String, String> cookie;

    public TService(String name, String traceId, Map<String, String> cookie) {
        this.name = name;
        this.traceId = traceId;
        this.cookie = cookie;
    }

}
