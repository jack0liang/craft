package io.craft.core.config;

public class EtcdEvent implements ConfigClient.NamespaceEvent {

    private final String key;

    private final String value;

    private final String oldValue;

    public EtcdEvent(String key, String value, String oldValue) {
        this.key = key;
        this.value = value;
        this.oldValue = oldValue;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getOldValue() {
        return oldValue;
    }

    @Override
    public String toString() {
        return "EtcdEvent{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", oldValue='" + oldValue + '\'' +
                '}';
    }
}
