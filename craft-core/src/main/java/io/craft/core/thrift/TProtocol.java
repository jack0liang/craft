package io.craft.core.thrift;

import io.craft.core.constant.Constants;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

public class TProtocol {

    protected static final TStruct EMPTY_STRUCT = new TStruct("");

    protected static final int VERSION_MASK = 0xffff0000;
    protected static final int VERSION_1 = 0x80010000;
    protected static final Charset CHARSET = Charset.forName("UTF-8");

    protected final ByteBuf buffer;

    protected TProtocol(ByteBuf buffer) {
        this.buffer = buffer;
    }

    public void writeMessageBegin(TMessage message) {
        //重置读写指针
        buffer.readerIndex(0);
        buffer.writerIndex(0);
        //写入帧大小的占位符
        writeInt(0);
        int version = VERSION_1 | message.type.getValue();
        writeInt(version);
        writeString(message.name);
        writeInt(message.sequence);
    }

    public void writeMessageEnd() {
        //将帧大小写入buffer头
        buffer.setInt(0, buffer.readableBytes() - Constants.INT_BYTE_LENGTH);
    }

    public void writeStructBegin(TStruct struct) {}

    public void writeStructEnd() {}

    public void writeFieldBegin(TField field) {
        writeByte(field.type.getValue());
        writeShort(field.sequence);
    }

    public void writeFieldEnd() {}

    public void writeFieldStop() {
        writeByte(TType.STOP.getValue());
    }

    public void writeMapBegin(TMap map) {
        writeByte(map.keyType.getValue());
        writeByte(map.valueType.getValue());
        writeInt(map.size);
    }

    public void writeMapEnd() {}

    public void writeListBegin(TList list) {
        writeByte(list.type.getValue());
        writeInt(list.size);
    }

    public void writeListEnd() {}

    public void writeSetBegin(TSet set) {
        writeByte(set.type.getValue());
        writeInt(set.size);
    }

    public void writeSetEnd() {}

    public void writeBool(boolean b) {
        writeByte(b ? (byte)1 : (byte)0);
    }

    public void writeByte(byte b) {
        buffer.writeByte(b);
    }

    public void writeShort(short s) {
        buffer.writeShort(s);
    }

    public void writeInt(int i) {
        buffer.writeInt(i);
    }

    public void writeLong(long l) {
        buffer.writeLong(l);
    }

    public void writeDouble(double d) {
        buffer.writeDouble(d);
    }

    public void writeString(String str) {
        int writerIndex = buffer.writerIndex();
        buffer.writeInt(0);
        int written = buffer.writeCharSequence(str, CHARSET);
        buffer.setInt(writerIndex, written);
    }

    public void writeBinary(ByteBuf src) {
        buffer.writeInt(src.readableBytes());
        buffer.writeBytes(src);
    }

    /**
     * Reading methods.
     */

    public TMessage readMessageBegin() throws TException {
        TProtocolUtil.skip(this, TType.INT);
        int size = readInt();
        if (size < 0) {
            int version = size & VERSION_MASK;
            if (version != VERSION_1) {
                throw new TException("error message header");
            }
            return new TMessage(readString(), TMessageType.findByValue((byte)(size & 0x000000ff)), readInt());
        } else {
            throw new TException("error message header");
        }
    }

    public void readMessageEnd() {}

    public TStruct readStructBegin() {
        return EMPTY_STRUCT;
    }

    public void readStructEnd() {}

    public TField readFieldBegin() {
        byte b = readByte();
        TType type = TType.findByValue(b);
        short sequence = type == TType.STOP ? 0 : readShort();
        return new TField("", type, sequence);
    }

    public void readFieldEnd() {}

    public TMap readMapBegin() {
        TMap map = new TMap(TType.findByValue(readByte()), TType.findByValue(readByte()), readInt());
        return map;
    }

    public void readMapEnd() {}

    public TList readListBegin() {
        TList list = new TList(TType.findByValue(readByte()), readInt());
        return list;
    }

    public void readListEnd() {}

    public TSet readSetBegin() {
        TSet set = new TSet(TType.findByValue(readByte()), readInt());
        return set;
    }

    public void readSetEnd() {}

    public boolean readBool() {
        return (readByte() == 1);
    }

    public byte readByte() {
        return buffer.readByte();
    }

    public short readShort() {
        return buffer.readShort();
    }

    public int readInt() {
        return buffer.readInt();
    }

    public long readLong() {
        return buffer.readLong();
    }

    public double readDouble() {
        return buffer.readDouble();
    }

    public String readString() {
        int size = readInt();
        return readStringBody(size);
    }

    public String readStringBody(int size) {
        return (String) buffer.readCharSequence(size, CHARSET);
    }

    public ByteBuf readBinary() {
        int size = readInt();
        return buffer.readSlice(size);
    }

}
