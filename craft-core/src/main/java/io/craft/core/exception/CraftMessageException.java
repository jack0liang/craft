package io.craft.core.exception;

import org.apache.thrift.TApplicationException;

public class CraftMessageException extends TApplicationException {

    private int messageId;

    public CraftMessageException(int messageId) {
        this(messageId, TApplicationException.INTERNAL_ERROR, null);
    }

    public CraftMessageException(int messageId, int type) {
        this(messageId, type, null);
    }

    public CraftMessageException(int messageId, int type, String message) {
        super(type, message);
        this.messageId = messageId;
    }

    public int getMessageId() {
        return messageId;
    }
}
