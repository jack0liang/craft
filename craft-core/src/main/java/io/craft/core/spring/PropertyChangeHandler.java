package io.craft.core.spring;

public interface PropertyChangeHandler {

    void accept(String key, String value, String oldValue);

}
