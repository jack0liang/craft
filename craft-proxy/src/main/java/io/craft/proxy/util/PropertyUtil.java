package io.craft.proxy.util;

import java.io.IOException;
import java.util.Properties;

public class PropertyUtil {
    private static Properties props = new Properties();
    static {
        try {
            props.load(PropertyUtil.class.getResourceAsStream("/application.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getProperty(String name) {
        String value = System.getProperty(name);
        if (value == null) {
            value = System.getenv(name);
            if(value==null)
                value = props.getProperty(name);
        }
        return value;
    }

    public static void main(String[] args) {
        System.out.println(getProperty("application.namespace"));
    }
}
