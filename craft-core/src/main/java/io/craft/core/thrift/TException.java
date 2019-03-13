package io.craft.core.thrift;

public class TException extends Exception implements TSerializable {

    private static final TStruct CRAFT_EXCEPTION_STRUCT = new TStruct("TException");
    private static final TField MESSAGE_FIELD = new TField("message", TType.STRING, (short)1);
    private static final TField TYPE_FIELD = new TField("type", TType.INT, (short)2);

    private static final int DEFAULT_TYPE = 500;

    private String message;
    private int type;

    public TException() {
        this(null, DEFAULT_TYPE);
    }

    public TException(String message) {
        this(message, DEFAULT_TYPE);
    }

    public TException(String message, int type) {
        super(message);
        this.message = message;
        this.type = type;
    }

    public TException(String message, Throwable cause) {
        this(message, DEFAULT_TYPE, cause);
    }

    public TException(String message, int type, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.type = type;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }

    @Override
    public void read(TProtocol protocol) throws TException {
        TField field;
        protocol.readStructBegin();

        while (true) {
            field = protocol.readFieldBegin();
            if (field.type == TType.STOP) {
                break;
            }
            switch (field.sequence) {
                case 1:
                    if (field.type == TType.STRING) {
                        this.message = protocol.readString();
                    } else {
                        TProtocolUtil.skip(protocol, field.type);
                    }
                    break;
                case 2:
                    if (field.type == TType.INT) {
                        this.type = protocol.readInt();
                    } else {
                        TProtocolUtil.skip(protocol, field.type);
                    }
                    break;
                default:
                    TProtocolUtil.skip(protocol, field.type);
                    break;
            }
            protocol.readFieldEnd();
        }
        protocol.readStructEnd();
    }

    @Override
    public void write(TProtocol protocol) {
        protocol.writeStructBegin(CRAFT_EXCEPTION_STRUCT);
        if (getMessage() != null) {
            protocol.writeFieldBegin(MESSAGE_FIELD);
            protocol.writeString(getMessage());
            protocol.writeFieldEnd();
        }
        protocol.writeFieldBegin(TYPE_FIELD);
        protocol.writeInt(getType());
        protocol.writeFieldEnd();
        protocol.writeFieldStop();
        protocol.writeStructEnd();
    }
}
