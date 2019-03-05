package io.craft.core.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.util.ReferenceCounted;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.TTransport;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

import static io.craft.core.constant.Constants.*;

/**
 * Thread Unsafe
 */
public class CraftFramedMessage extends TBinaryProtocol implements ReferenceCounted {

    protected static final long NO_LENGTH_LIMIT = -1;

    private final long requestTime;

    private final ByteBuf buffer;

    private TMessage messageHeader;

    private ServiceHeader serviceHeader;

    public CraftFramedMessage(ByteBuf buffer) {
        this(buffer, 0, false, true);
    }

    public CraftFramedMessage(Channel channel) {
        this(channel.alloc().directBuffer(DEFAULT_BYTEBUF_SIZE), 0, false, true);
    }

    public CraftFramedMessage(ByteBuf buffer, long requestTime) {
        this(buffer, requestTime, false, true);
    }

    public CraftFramedMessage(ByteBuf buffer, long requestTime, boolean strictRead, boolean strictWrite) {
        this(buffer, requestTime, NO_LENGTH_LIMIT, NO_LENGTH_LIMIT, strictRead, strictWrite);
    }

    public CraftFramedMessage(ByteBuf buffer, long requestTime, long stringLengthLimit, long containerLengthLimit) {
        this(buffer, requestTime, stringLengthLimit, containerLengthLimit, false, true);
    }

    public CraftFramedMessage(ByteBuf buffer, long requestTime, long stringLengthLimit, long containerLengthLimit, boolean strictRead, boolean strictWrite) {
        super(new TByteBufTransport(buffer), stringLengthLimit, containerLengthLimit, strictRead, strictWrite);
        this.requestTime = requestTime;
        this.buffer = buffer;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public TMessage getMessageHeader() throws TException {
        if (messageHeader != null) {
            return messageHeader;
        }
        buffer.markReaderIndex();
        initMessageHeader();
        buffer.resetReaderIndex();

        return messageHeader;
    }

    public String getServiceName() throws TException {
        initServiceHeader();
        return serviceHeader.serviceName;
    }

    public String getTraceId() throws TException {
        initServiceHeader();
        return serviceHeader.traceId;
    }

    public Map<String, String> getServiceHeader() throws TException {
        initServiceHeader();
        return serviceHeader.header;
    }

    private void initMessageHeader() throws TException {
        buffer.readerIndex(0);
        messageHeader = readMessageBegin();
    }

    private void initServiceHeader() throws TException {
        if (serviceHeader != null) {
            return;
        }

        buffer.markReaderIndex();
        //顺便把消息头也读了
        initMessageHeader();

        String serviceName = null, traceId = null;
        Map<String, String> header = null;

        boolean serviceNameIsSet = false, traceIdIsSet = false, serviceHeaderIsSet = false;

        TField field;

        readStructBegin();

        while (true) {
            field = readFieldBegin();

            if (field.type == TType.STOP) {
                break;
            }

            if (serviceNameIsSet && traceIdIsSet && serviceHeaderIsSet) {
                break;
            }

            switch (field.id) {

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

                case HEADER_SEQUENCE:
                    serviceHeaderIsSet = true;
                    if (field.type == TType.MAP) {
                        {
                            TMap map = readMapBegin();
                            header = new HashMap<>(2 * map.size);
                            for (int i = 0; i < map.size; ++i) {
                                header.put(readString(), readString());
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

        buffer.resetReaderIndex();

        Assert.notNull(serviceName, "serviceName is null");
        Assert.notNull(traceId, "traceId is null");
        Assert.notNull(header, "header is null");

        serviceHeader = new ServiceHeader(serviceName, traceId, header);
    }

    public void setMessageSequence(int sequence) throws TException {
        buffer.markReaderIndex();
        //读头
        initMessageHeader();
        //messageId所在位置为message header的尾部4个字节
        buffer.setInt(buffer.readerIndex() - FRAME_SIZE_BYTE_LENGTH, sequence);
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
    public TMessage readMessageBegin() throws TException {
        //跳过帧头
        TProtocolUtil.skip(this, TType.I32);
        return super.readMessageBegin();
    }


    @Override
    public void writeMessageBegin(TMessage message) throws TException {
        writeI32(0);
        super.writeMessageBegin(message);
    }

    @Override
    public void writeMessageEnd() {
        super.writeMessageEnd();
        //将帧大小写入到buffer的头部
        buffer.setInt(0, buffer.readableBytes() - FRAME_SIZE_BYTE_LENGTH);
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

    private static class ServiceHeader {

        private final String serviceName;

        private final String traceId;

        private final Map<String, String> header;

        public ServiceHeader(String serviceName, String traceId, Map<String, String> header) {
            this.serviceName = serviceName;
            this.traceId = traceId;
            this.header = header;
        }
    }
}
