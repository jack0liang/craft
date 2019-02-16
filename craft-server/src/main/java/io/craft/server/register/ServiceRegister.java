package io.craft.server.register;

import java.io.Closeable;

public interface ServiceRegister extends Closeable {

    void register() throws Throwable;
}
