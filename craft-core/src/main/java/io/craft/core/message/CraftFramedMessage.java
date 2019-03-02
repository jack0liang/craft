package io.craft.core.message;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCounted;

public class CraftFramedMessage implements ReferenceCounted {


    private final long requestTime;

    private final int timeout;

    private final ByteBuf buffer;

    public CraftFramedMessage(ByteBuf buffer, long requestTime, int timeout) {
        this.buffer = buffer;
        this.requestTime = requestTime;
        this.timeout = timeout;
    }

    public CraftFramedMessage(ByteBuf buffer) {
        this(buffer, 0, 0);
    }

    public ByteBuf getBuffer() {
        return buffer;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public int getTimeout() {
        return timeout;
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
