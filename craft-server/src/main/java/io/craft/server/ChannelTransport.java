package io.craft.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelTransport extends TTransport {

    public static final Map<Channel, ByteBuf> channels = new ConcurrentHashMap<>();

    private Channel channel;

    private ByteBuf buffer;

    public ChannelTransport(Channel channel, ByteBuf buffer) {
        this.channel = channel;
        this.buffer = buffer;
        channels.put(channel, buffer);
    }

    @Override
    public boolean isOpen() {
        return channel.isOpen();
    }

    @Override
    public void open() throws TTransportException {
        //do nth
    }

    @Override
    public void close() {
        buffer.release();
        channel.close().syncUninterruptibly();
    }

    @Override
    public int read(byte[] buf, int off, int len) throws TTransportException {
        return 0;
    }

    @Override
    public void write(byte[] buf, int off, int len) throws TTransportException {
        ByteBuf buffer = channel.alloc().directBuffer(buf.length);
        buffer.writeBytes(buf, off, len);
        channel.write(buffer);
    }

    @Override
    public void flush() throws TTransportException {
        channel.flush();
        synchronized (channel) {
            try {
                channel.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
