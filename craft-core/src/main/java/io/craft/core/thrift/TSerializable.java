package io.craft.core.thrift;

public interface TSerializable {

    void read(TProtocol protocol) throws TException;

    void write(TProtocol protocol) throws TException;

}
