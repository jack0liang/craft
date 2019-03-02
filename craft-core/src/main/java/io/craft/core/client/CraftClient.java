package io.craft.core.client;

import io.craft.core.constant.Constants;
import io.craft.core.message.CraftFramedMessage;
import io.craft.core.transport.TByteBuf;
import io.craft.core.util.PropertyUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;


public class CraftClient extends BaseCraftClient {

    private static String PROXY_HOST;

    private static int PROXY_PORT;

    private static int PROXY_CONNECT_TIMEOUT;

    static {
        PROXY_HOST = PropertyUtil.getProperty("proxy.host");
        PROXY_PORT = Integer.valueOf(PropertyUtil.getProperty("proxy.port"));
        PROXY_CONNECT_TIMEOUT = Integer.valueOf(PropertyUtil.getProperty("proxy.connect.timeout"));
    }

    public CraftClient() {
        super(
                new Bootstrap()
                        .remoteAddress(PROXY_HOST, PROXY_PORT)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, PROXY_CONNECT_TIMEOUT)//TODO 此处的连接超时需要设置
                ,
                new NioEventLoopGroup(2)
        );
    }

    protected Future<CraftFramedMessage> sendBase(String methodName, TBase<?,?> args) throws TException {
        return sendBase(methodName, args, TMessageType.CALL);
    }

    protected Future<CraftFramedMessage> sendBase(String methodName, TBase<?,?> args, byte type) throws TException {
        MessageProducer producer = new ClientMessageProducer(methodName, args, type);
        return write(producer);
    }

    protected void receiveBase(Future<CraftFramedMessage> future, TBase<?,?> result, String methodName) throws TException {
        CraftFramedMessage message;
        try {
            message = future.get();
        } catch (Throwable t) {
            throw new TApplicationException(TApplicationException.INTERNAL_ERROR, "future get error, error=" + t.getMessage());
        }

        if (message == null) {
            throw new TApplicationException(TApplicationException.INTERNAL_ERROR, "response is null");
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

        result.read(pin);
        pin.readMessageEnd();
    }

    private static class ClientMessageProducer implements MessageProducer {

        private String methodName;

        private TBase<?, ?> args;

        private byte type;

        private int messageId;

        public ClientMessageProducer(String methodName, TBase<?, ?> args, byte type) {
            this.methodName = methodName;
            this.args = args;
            this.type = type;
        }

        @Override
        public void setMessageId(int messageId) {
            this.messageId = messageId;
        }

        @Override
        public int getMessageId() {
            return this.messageId;
        }

        @Override
        public CraftFramedMessage produce(Channel channel) throws TException {
            ByteBuf buffer = channel.alloc().directBuffer(Constants.DEFAULT_BYTEBUF_SIZE);
            TByteBuf tout = new TByteBuf(buffer);
            TProtocol pout = new TBinaryProtocol(tout);

            pout.writeMessageBegin(new TMessage(methodName, type, messageId));
            args.write(pout);
            pout.writeMessageEnd();

            return new CraftFramedMessage(buffer);
        }
    }
}
