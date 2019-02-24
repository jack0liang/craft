package io.craft.core.spring;

public interface PropertyManager {

    String getProperty(String name);

    String getProperty(String name, String defaultValue);

}
