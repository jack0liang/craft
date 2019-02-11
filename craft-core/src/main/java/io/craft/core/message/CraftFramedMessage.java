package io.craft.core.message;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCounted;

public class CraftFramedMessage implements ReferenceCounted {

    private final ByteBuf buffer;

    private final Long requestTime;

    public CraftFramedMessage(ByteBuf buffer, Long requestTime) {
        this.buffer = buffer;
        this.requestTime = requestTime;
    }

    public ByteBuf getBuffer() {
        return buffer;
    }

    public Long getRequestTime() {
        return requestTime;
    }

    @Override
    public int refCnt() {
        return buffer.refCnt();
    }

    @Override
    public ReferenceCounted retain() {
        buffer.retain();
        return this;
    }

    @Override
    public ReferenceCounted retain(int increment) {
        buffer.retain(increment);
        return this;
    }

    @Override
    public ReferenceCounted touch() {
        buffer.touch();
        return this;
    }

    @Override
    public ReferenceCounted touch(Object hint) {
        buffer.touch(hint);
        return this;
    }

    @Override
    public boolean release() {
        return buffer.release();
    }

    @Override
    public boolean release(int decrement) {
        return buffer.release(decrement);
    }
}
