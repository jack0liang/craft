package io.craft.core.exception;

import org.apache.thrift.TApplicationException;

public class CraftException extends TApplicationException {

    private int messageId;

    public CraftException(int messageId) {
        super(TApplicationException.INTERNAL_ERROR);
        this.messageId = messageId;
    }

    public CraftException(int messageId, int type) {
        super(type);
        this.messageId = messageId;
    }

    public CraftException(int messageId, int type, String message) {
        super(type, message);
        this.messageId = messageId;
    }

    public int getMessageId() {
        return messageId;
    }
}
