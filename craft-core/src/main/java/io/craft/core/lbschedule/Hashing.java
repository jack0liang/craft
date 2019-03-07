package io.craft.core.lbschedule;

public interface Hashing {

    default int hash() {
        return this.hashCode();
    }
}
