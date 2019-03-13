package io.craft.core.thrift;

public class TSet {

    public TSet() {
        this(TType.STOP, 0);
    }

    public TSet(TType type, int size) {
        this.type = type;
        this.size = size;
    }

    public final TType type;
    public final int  size;

}
