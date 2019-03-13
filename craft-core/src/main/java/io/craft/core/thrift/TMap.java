package io.craft.core.thrift;

public class TMap {

    public TMap() {
        this(TType.STOP, TType.STOP, 0);
    }

    public TMap(TType keyType, TType valueType, int size) {
        this.keyType = keyType;
        this.valueType = valueType;
        this.size = size;
    }

    public final TType keyType;
    public final TType valueType;
    public final int   size;
}
