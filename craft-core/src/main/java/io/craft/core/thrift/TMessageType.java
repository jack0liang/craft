package io.craft.core.thrift;

public enum TMessageType {

    CALL((byte) 1),
    REPLY((byte) 2),
    EXCEPTION((byte) 3),
    REGISTER((byte) 126),
    REGISTERED((byte) 127),
    ;

    private byte value;

    TMessageType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static TMessageType findByValue(byte value) {
        switch (value) {

            case 1 :
                return CALL;

            case 2 :
                return REPLY;

            case 3 :
                return EXCEPTION;

            case 126 :
                return REGISTER;

            case 127 :
                return REGISTERED;

            default :
                return null;

        }
    }
}
