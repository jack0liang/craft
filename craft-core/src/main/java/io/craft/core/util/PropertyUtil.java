package io.craft.core.util;

public class PropertyUtil {


    public static String getProperty(String name) {
        String value = System.getProperty(name);
        if (value == null) {
            value = System.getenv(name);
        }
        return value;
    }

}
