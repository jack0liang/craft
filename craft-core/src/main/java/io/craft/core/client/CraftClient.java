package io.craft.core.client;

import io.craft.core.codec.CraftFramedMessageDecoder;
import io.craft.core.codec.CraftFramedMessageEncoder;
import io.craft.core.codec.CraftThrowableEncoder;
import io.craft.core.constant.Constants;
import io.craft.core.message.CraftFramedMessage;
import io.craft.core.transport.TByteBuf;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.Attribute;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;

import java.util.concurrent.atomic.AtomicInteger;

public class CraftClient {

    private String proxyHost;

    private int proxyPort;

    private EventLoopGroup executors;

    private AtomicInteger sequence;

    private Bootstrap bootstrap;

    private ChannelPool pool;

    private ClientPoolHandler handler;

    public CraftClient() {
        //获取
        this.executors = new NioEventLoopGroup();
        this.sequence = new AtomicInteger(0);
        this.bootstrap = new Bootstrap();
        this.bootstrap
                .group(this.executors)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true);

        this.handler = new ClientPoolHandler();

    }

    public CraftClient(String proxyHost, int proxyPort) {
        this();
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        init();
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void init() {
        this.bootstrap.remoteAddress(this.proxyHost, this.proxyPort);
        //@TODO 此处的maxConnections需要调整成跟业务线程一样的大小
        this.pool = new FixedChannelPool(this.bootstrap, this.handler, 1000);
    }

    protected Channel sendBase(String methodName, TBase<?,?> args) throws TException {
        return sendBase(methodName, args, TMessageType.CALL);
    }


    protected Channel sendBase(String methodName, TBase<?,?> args, byte type) throws TException {
        Channel channel;
        try {
            channel = pool.acquire().get();
        } catch (Exception e) {
            throw new TApplicationException(TApplicationException.INTERNAL_ERROR, "acquire pool failed, error=" + e.getMessage());
        }

        int messageId = sequence.getAndIncrement();

        ByteBuf buffer = channel.alloc().directBuffer(Constants.DEFAULT_BYTEBUF_SIZE);
        TByteBuf tout = new TByteBuf(buffer);
        TProtocol pout = new TBinaryProtocol(tout);

        pout.writeMessageBegin(new TMessage(methodName, type, messageId));
        args.write(pout);
        pout.writeMessageEnd();

        channel.writeAndFlush(new CraftFramedMessage(buffer, 0, 0));
        setChannelMessageId(channel, messageId);
        return channel;
    }

    protected void receiveBase(Channel channel, TBase<?,?> result, String methodName) throws TException {
        CraftFramedMessage message;
        int messageId;
        synchronized (channel) {
            try {
                channel.wait();
            } catch (InterruptedException e) {
                throw new TApplicationException(TApplicationException.INTERNAL_ERROR, "receive wait error=" + e.getMessage());
            }
            messageId = getChannelMessageId(channel);
            message = getChannelMessage(channel);
            pool.release(channel);
        }

        if (message == null) {
            throw new TApplicationException(TApplicationException.MISSING_RESULT, "result not found");
        }

        TTransport tin = new TByteBuf(message.getBuffer());
        TProtocol pin = new TBinaryProtocol(tin);

        TMessage msg = pin.readMessageBegin();
        if (msg.type == TMessageType.EXCEPTION) {
            TApplicationException x = new TApplicationException();
            x.read(pin);
            pin.readMessageEnd();
            throw x;
        }
        if (msg.seqid != messageId) {
            throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID,
                    String.format("%s failed: out of sequence response: expected %d but got %d", methodName, messageId, msg.seqid));
        }
        result.read(pin);
        pin.readMessageEnd();
    }

    public void close() {
        executors.shutdownGracefully().syncUninterruptibly();
    }

    public static void setChannelMessage(Channel channel, CraftFramedMessage message) {
        Attribute<CraftFramedMessage> attrMessage = channel.attr(Constants.CHANNEL_MESSAGE);
        attrMessage.set(message);
    }

    public static CraftFramedMessage getChannelMessage(Channel channel) {
        Attribute<CraftFramedMessage> attrMessage = channel.attr(Constants.CHANNEL_MESSAGE);
        return attrMessage.get();
    }

    public static void setChannelMessageId(Channel channel, Integer id) {
        Attribute<Integer> attrMessageId = channel.attr(Constants.CHANNEL_MESSAGE_ID);
        attrMessageId.set(id);
    }

    public static Integer getChannelMessageId(Channel channel) {
        Attribute<Integer> attrMessageId = channel.attr(Constants.CHANNEL_MESSAGE_ID);
        return attrMessageId.get();
    }

    public class ClientPoolHandler extends AbstractChannelPoolHandler {

        private CraftFramedMessageEncoder messageEncoder = new CraftFramedMessageEncoder();

        private CraftThrowableEncoder throwableEncoder = new CraftThrowableEncoder();

        private ClientChannelHandler clientChannelHandler = new ClientChannelHandler();

        @Override
        public void channelCreated(Channel ch) throws Exception {
            ch.pipeline()
                    .addLast(new CraftFramedMessageDecoder())
                    .addLast(messageEncoder)
                    .addLast(throwableEncoder)
                    .addLast(clientChannelHandler);
        }

        @Override
        public void channelReleased(Channel ch) throws Exception {
            setChannelMessage(ch, null);
            setChannelMessageId(ch, null);
        }
    }

    @ChannelHandler.Sharable
    public class ClientChannelHandler extends SimpleChannelInboundHandler<CraftFramedMessage> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, CraftFramedMessage message) throws Exception {
            message.retain();
            Channel channel = ctx.channel();
            setChannelMessage(channel, message);
            synchronized (channel) {
                channel.notify();
            }
        }
    }
}
