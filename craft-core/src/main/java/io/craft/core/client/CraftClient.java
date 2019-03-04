package io.craft.core.client;

import io.craft.core.constant.Constants;
import io.craft.core.message.CraftFramedMessage;
import io.craft.core.message.TByteBufProtocol;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CraftClient extends BaseCraftClient {

    private static final Logger logger = LoggerFactory.getLogger(CraftClient.class);

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
            logger.error(t.getMessage(), t);
            throw new TApplicationException(TApplicationException.INTERNAL_ERROR, "future get error, error=" + t.getMessage());
        }

        if (message == null) {
            throw new TApplicationException(TApplicationException.INTERNAL_ERROR, "response is null");
        }

        TMessage msg = message.readMessageBegin();
        if (msg.type == TMessageType.EXCEPTION) {
            TApplicationException x = new TApplicationException();
            x.read(message);
            message.readMessageEnd();
            throw x;
        }

        result.read(message);
        message.readMessageEnd();
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
            CraftFramedMessage message = new CraftFramedMessage(channel);
            message.writeMessageBegin(new TMessage(methodName, type, messageId));
            args.write(message);
            message.writeMessageEnd();
            return message;
        }
    }
}
