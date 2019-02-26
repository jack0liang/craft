package io.craft.core.config;

import java.io.Closeable;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public interface ConfigClient<T extends ConfigClient.NamespaceEvent> extends Closeable {

    /**
     *
     * @param key
     * @param value
     * @return 对应key的旧值，如果之前没有则返回null
     * @throws Exception
     */
    String put(String key, String value) throws Exception;

    String put(String key, String value, boolean keepAlive) throws Exception;

    boolean delete(String key) throws Exception;

    Properties watch(String namespace, Consumer<T> consumer) throws Exception;

    interface NamespaceEvent {

        String getKey();

        String getValue();

        String getOldValue();

    }
}
