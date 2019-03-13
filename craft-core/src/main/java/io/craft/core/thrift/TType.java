package io.craft.core.thrift;

public enum TType {

    STOP((byte) 0),
    VOID((byte) 1),
    BOOL((byte) 2),
    BYTE((byte) 3),
    DOUBLE((byte) 4),
    SHORT((byte) 6),
    INT((byte) 8),
    LONG((byte) 10),
    STRING((byte) 11),
    STRUCT((byte) 12),
    MAP((byte) 13),
    SET((byte) 14),
    LIST((byte) 15),
    ENUM((byte) 16)
    ;

    private byte value;

    TType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static TType findByValue(byte value) {
        switch (value) {
            case 0 :
                return STOP;

            case 1 :
                return VOID;

            case 2 :
                return BOOL;

            case 3 :
                return BYTE;

            case 4 :
                return DOUBLE;

            case 6 :
                return SHORT;

            case 8 :
                return INT;

            case 10 :
                return LONG;

            case 11 :
                return STRING;

            case 12 :
                return STRUCT;

            case 13 :
                return MAP;

            case 14 :
                return SET;

            case 15 :
                return LIST;

            case 16 :
                return ENUM;

            default :
                return null;
        }
    }
}
