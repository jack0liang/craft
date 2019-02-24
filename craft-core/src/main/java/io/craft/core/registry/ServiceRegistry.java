package io.craft.core.registry;

public interface ServiceRegistry {

    void register() throws Exception;

    void close() throws Exception;
}
