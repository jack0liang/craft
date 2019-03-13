package io.craft.core.message;

import io.craft.core.thrift.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.ReferenceCounted;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

import static io.craft.core.constant.Constants.*;

/**
 * Thread Unsafe
 */
public class CraftMessage extends TProtocol implements ReferenceCounted {

    private final long requestTime;

    private final ByteBuf buffer;

    private TMessage header;

    private TService service;

    public CraftMessage(ByteBuf buffer) {
        this(buffer, 0);
    }

    public CraftMessage(Channel channel) {
        this(channel.alloc().directBuffer(DEFAULT_BYTEBUF_SIZE), 0);
    }

    public CraftMessage(ByteBuf buffer, long requestTime) {
        super(buffer);
        this.requestTime = requestTime;
        this.buffer = buffer;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public TMessage getHeader() throws TException {
        if (header != null) {
            return header;
        }
        buffer.markReaderIndex();
        header = getHeader0();
        buffer.resetReaderIndex();

        return header;
    }

    /**
     * 此方法会重置readerIndex
     * @return
     * @throws TException
     */
    private TMessage getHeader0() throws TException {
        buffer.readerIndex(0);
        return readMessageBegin();
    }

    public TService getService() throws TException {
        if (service != null) {
            return service;
        }
        buffer.markReaderIndex();
        service = getService0();
        buffer.resetReaderIndex();
        return service;
    }

    private TService getService0() throws TException {
        //先读消息头
        header = getHeader0();

        String serviceName = null, traceId = null;
        Map<String, String> cookie = null;

        boolean serviceNameIsSet = false, traceIdIsSet = false;

        readStructBegin();

        while(true) {
            TField field = readFieldBegin();
            if (field.type == TType.STOP) {
                break;
            }
            if (serviceNameIsSet && traceIdIsSet) {
                break;
            }
            switch (field.sequence) {

                case SERVICE_NAME_SEQUENCE:
                    serviceNameIsSet = true;
                    if (field.type == TType.STRING) {
                        serviceName = readString();
                    } else {
                        TProtocolUtil.skip(this, field.type);
                    }
                    break;

                case TRACE_ID_SEQUENCE:
                    traceIdIsSet = true;
                    if (field.type == TType.STRING) {
                        traceId = readString();
                    } else {
                        TProtocolUtil.skip(this, field.type);
                    }
                    break;

                case COOKIE_SEQUENCE:
                    if (field.type == TType.MAP) {
                        {
                            TMap map = readMapBegin();
                            cookie = new HashMap<>(2 * map.size);
                            for (int i = 0; i < map.size; ++i) {
                                cookie.put(readString(), readString());
                            }
                            readMapEnd();
                        }
                    } else {
                        TProtocolUtil.skip(this, field.type);
                    }
                    break;

                default :
                    TProtocolUtil.skip(this, field.type);
            }
            readFieldEnd();
        }

        Assert.notNull(serviceName, "name is null");
        Assert.notNull(traceId, "traceId is null");

        return new TService(serviceName, traceId, cookie);
    }

    public void setMessageSequence(int sequence) throws TException {
        buffer.markReaderIndex();
        //读头
        getHeader0();
        //messageId所在位置为message header的尾部4个字节
        buffer.setInt(buffer.readerIndex() - INT_BYTE_LENGTH, sequence);
        buffer.resetReaderIndex();
    }

    public void markReaderIndex() {
        buffer.markReaderIndex();
    }

    public void resetReaderIndex() {
        buffer.resetReaderIndex();
    }

    public void markWriterIndex() {
        buffer.markWriterIndex();
    }

    public void resetWriterIndex() {
        buffer.resetWriterIndex();
    }

    public int readerIndex() {
        return buffer.readerIndex();
    }

    public void readerIndex(int readerIndex) {
        buffer.readerIndex(readerIndex);
    }

    public int writerIndex() {
        return buffer.writerIndex();
    }

    public void writerIndex(int readerIndex) {
        buffer.writerIndex(readerIndex);
    }

    public void readBytes(ByteBuf dst) {
        buffer.readBytes(dst);
    }

    public int readableBytes() {
        return buffer.readableBytes();
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
