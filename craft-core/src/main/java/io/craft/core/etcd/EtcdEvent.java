package io.craft.core.etcd;

public class EtcdEvent {

    private final String key;

    private final String value;

    public EtcdEvent(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
