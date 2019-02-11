package io.craft.server.executor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class CraftThreadFactory implements ThreadFactory {

    private final AtomicInteger count;

    private String prefix;

    public CraftThreadFactory(String prefix) {
        count = new AtomicInteger(0);
        this.prefix = prefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, prefix + count.getAndIncrement());
    }
}
