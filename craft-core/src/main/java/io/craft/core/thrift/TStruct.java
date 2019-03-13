package io.craft.core.thrift;

public class TStruct {

    public TStruct() {
        this("");
    }

    public TStruct(String n) {
        name = n;
    }

    public final String name;

}
