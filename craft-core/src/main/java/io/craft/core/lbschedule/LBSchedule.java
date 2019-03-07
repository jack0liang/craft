package io.craft.core.lbschedule;

public interface LBSchedule<E, R> {

    void set(E[] nodes);

    default E get(R request) {
        return get();
    }

    E get();

}
