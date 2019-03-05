package io.craft.core.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class TByteBufTransport extends TTransport {

    private final ByteBuf buffer;

    public TByteBufTransport(ByteBuf buffer) {
        this.buffer = buffer;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void open() throws TTransportException {
        throw new TTransportException("not support");
    }

    @Override
    public void close() {
        buffer.release();
    }

    @Override
    public int read(byte[] buf, int off, int len) throws TTransportException {
        int readableBytes = Math.min(buffer.readableBytes(), len);
        if (readableBytes > 0) {
            try {
                buffer.readBytes(buf, off, len);
            } catch (Exception e) {
                throw new TTransportException(e.getMessage(), e);
            }
        }
        return readableBytes;
    }

    @Override
    public void write(byte[] buf, int off, int len) throws TTransportException {
        try {
            buffer.writeBytes(buf, off, len);
        } catch (Exception e) {
            throw new TTransportException(e.getMessage(), e);
        }
    }
}
