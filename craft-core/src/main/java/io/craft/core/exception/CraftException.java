package io.craft.core.exception;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;

public class CraftException extends TException {

    private static final TStruct STRUCT = new TStruct("CraftException");

    private static final TField FIELD_CODE = new TField("code", TType.I32, (short) 0);

    private static final TField FIELD_MESSAGE = new TField("message", TType.STRING, (short) 1);

    private Integer code;

    private String message;

    public CraftException() {
        super();
    }

    public CraftException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public void read(TProtocol iprot) throws TException {
        TField field;
        iprot.readStructBegin();
        field = iprot.readFieldBegin();
        assertSeq(FIELD_CODE.id, field.id);
        this.code = iprot.readI32();
        iprot.readFieldEnd();

        field = iprot.readFieldBegin();
        assertSeq(FIELD_MESSAGE.id, field.id);
        this.message = iprot.readString();
        iprot.readFieldEnd();

        iprot.readStructEnd();
    }

    public void write(TProtocol oprot) throws TException {
        oprot.writeStructBegin(STRUCT);
        oprot.writeFieldBegin(FIELD_CODE);
        oprot.writeI32(code);
        oprot.writeFieldEnd();
        oprot.writeFieldBegin(FIELD_MESSAGE);
        oprot.writeString(message);
        oprot.writeFieldEnd();
        oprot.writeFieldStop();
        oprot.writeStructEnd();
    }

    private static void assertSeq(short expect, short actual) throws TException {
        if (actual != expect) {
            throw new TProtocolException(TProtocolException.INVALID_DATA);
        }
    }
}

