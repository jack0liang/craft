package io.craft.core.lbschedule;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinSchedule<E> implements LBSchedule<E, Object> {

    private E[] nodes;

    private AtomicInteger current;

    public RoundRobinSchedule(E[] nodes) {
        this.current = new AtomicInteger(0);
        set(nodes);
    }

    @Override
    public void set(E[] nodes) {
        this.nodes = nodes;
    }

    @Override
    public E get() {
        int pos = current.getAndIncrement() % nodes.length;
        return nodes[pos];
    }
}
