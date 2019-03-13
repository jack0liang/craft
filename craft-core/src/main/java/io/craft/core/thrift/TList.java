package io.craft.core.thrift;

public class TList {

    public TList() {
        this(TType.STOP, 0);
    }

    public TList(TType type, int size) {
        this.type = type;
        this.size = size;
    }

    public final TType type;
    public final int  size;
}
