package io.craft.abc.constant;

public enum UserType implements io.craft.core.thrift.TEnum {

    A(0),

    B(1),

    ;

    private final int value;

    UserType(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    public static UserType findByValue(int value) {
        switch (value) {
            case 0:
                return A;
            case 1:
                return B;
            default:
                return null;
        }
    }
}