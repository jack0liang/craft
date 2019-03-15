package io.craft.core.lbschedule;

import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinSchedule<E> implements LBSchedule<E, Object> {

    private volatile E[] nodes;

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
        E[] vnodes = nodes;
        if (vnodes.length == 0) {
            return null;
        }
        int pos = current.getAndUpdate(prev -> {
            int next = prev++;
            if (next >= vnodes.length) {
                return 0;
            } else {
                return next;
            }
        });
        return vnodes[pos];
    }


}
