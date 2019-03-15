package io.craft.core.lbschedule;

public class HashSchedule<E> implements LBSchedule<E, Hashing> {

    private volatile E[] nodes;

    private RoundRobinSchedule<E> optional;

    public HashSchedule(E[] nodes) {
        set(nodes);
        this.optional = new RoundRobinSchedule<>(nodes);
    }

    @Override
    public void set(E[] nodes) {
        this.nodes = nodes;
        this.optional.set(nodes);
    }

    @Override
    public E get() {
        return optional.get();
    }

    @Override
    public E get(Hashing request) {
        E[] vnodes = nodes;
        if (vnodes.length == 0) {
            return null;
        }
        int pos = request.hash() % vnodes.length;
        return vnodes[pos];
    }
}
