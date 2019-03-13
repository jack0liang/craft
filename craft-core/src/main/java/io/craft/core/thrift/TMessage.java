package io.craft.core.thrift;

import java.util.Objects;

public class TMessage {

    public TMessage(String name, TMessageType type, int sequence) {
        this.name = name;
        this.type = type;
        this.sequence = sequence;
    }

    public final String name;
    public final TMessageType type;
    public final int sequence;

    @Override
    public String toString() {
        return "<TMessage name:'" + name + "' type: " + type + " sequence:" + sequence + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TMessage message = (TMessage) o;
        return sequence == message.sequence &&
                Objects.equals(name, message.name) &&
                type == message.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, sequence);
    }
}
