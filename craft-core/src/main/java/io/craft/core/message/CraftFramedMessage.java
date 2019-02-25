package io.craft.core.message;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCounted;

import java.util.HashMap;
import java.util.Map;

public class CraftFramedMessage implements ReferenceCounted {

    private final String traceId;

    private final long requestTime;

    private final int timeout;

    private final Map<String, String> header;

    private final ByteBuf buffer;

    CraftFramedMessage(String traceId, long requestTime, int timeout, Map<String, String> header, ByteBuf buffer) {
        this.traceId = traceId;
        this.requestTime = requestTime;
        this.timeout = timeout;
        this.header = header;
        this.buffer = buffer;
    }

    public String getTraceId() {
        return traceId;
    }

    public Map<String, String> getHeader() {
        return header;
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

    @Override
    public String toString() {
        return "CraftFramedMessage{" +
                "traceId='" + traceId + '\'' +
                ", requestTime=" + requestTime +
                ", timeout=" + timeout +
                ", header=" + header +
                ", buffer=" + buffer +
                '}';
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private String traceId;

        private Map<String, String> header;

        private ByteBuf buffer;

        private Long requestTime;

        private Integer timeout;

        Builder() {
            this.traceId = null;
            this.header = new HashMap<>(0);
            this.buffer = null;
            this.requestTime = null;
            this.timeout = null;
        }

        public Builder setTraceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder addHeader(String name, String value) {
            header.put(name, value);
            return this;
        }

        public Builder setBuffer(ByteBuf buffer) {
            this.buffer = buffer;
            return this;
        }

        public Builder setRequestTime(long requestTime) {
            this.requestTime = requestTime;
            return this;
        }

        public Builder setTimeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public CraftFramedMessage build() {
            //这里可以做一些必要的检查
            return new CraftFramedMessage(traceId, requestTime, timeout, header, buffer);
        }
    }
}
