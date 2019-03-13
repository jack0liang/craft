package io.craft.idl.constant;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unchecked"})
public enum ClassType {

    VOID("VOID"),
    BOOL("BOOL"),
    BYTE("BYTE"),
    DOUBLE("DOUBLE"),
    I16("SHORT"),
    I32("INT"),
    I64("LONG"),
    STRING("STRING"),
    STRUCT("STRUCT"),
    MAP("MAP"),
    SET("SET"),
    LIST("LIST"),
    ENUM("ENUM"),
    DATE("LONG"),

    ;

    private String name;

    ClassType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return this.name();
    }

    public static ClassType findByClass(Class clazz) {
        if (Boolean.class.equals(clazz)) {
            return BOOL;
        } else if (Byte.class.equals(clazz)) {
            return BYTE;
        } else if (Double.class.equals(clazz) || Float.class.equals(clazz)) {
            return DOUBLE;
        } else if (Short.class.equals(clazz)) {
            return I16;
        } else if (Integer.class.equals(clazz)) {
            return I32;
        } else if (Long.class.equals(clazz)) {
            return I64;
        } else if (String.class.equals(clazz)) {
            return STRING;
        }  else if (Date.class.equals(clazz)) {
            return DATE;
        } else if (clazz.isAssignableFrom(Set.class)) {
            return SET;
        } else if (clazz.isAssignableFrom(List.class)) {
            return LIST;
        } else if (clazz.isAssignableFrom(Map.class)) {
            return MAP;
        } else if (clazz.isEnum()) {
            return ENUM;
        } else if (clazz.getSimpleName().equals("void")) {
            return VOID;
        } else {
            return STRUCT;
        }
    }

}
