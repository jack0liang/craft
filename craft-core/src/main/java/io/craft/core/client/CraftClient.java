package io.craft.core.client;

import io.craft.core.message.CraftMessage;
import io.craft.core.thrift.*;
import io.craft.core.util.PropertyUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
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

    public CraftClient(String serviceName) {
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
        //发送服务监听消息(proxy专用,直接连接服务端的话,服务端会忽略此消息)
        MessageProducer producer = new ClientInitProducer(serviceName);
        CraftMessage response = null;
        try {
            //同步等待注册返回
            response = write(producer).get();
            TMessage msg = response.readMessageBegin();
            if (msg.type != TMessageType.REGISTERED) {
                throw new RuntimeException("proxy init failed");
            }
            logger.info("proxy init [{}] done", serviceName);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (response != null) {
                response.release();
            }
        }
    }

    protected Future<CraftMessage> sendBase(String methodName, TArgs args) throws io.craft.core.thrift.TException {
        return sendBase(methodName, args, TMessageType.CALL);
    }

    protected Future<CraftMessage> sendBase(String methodName, TArgs args, TMessageType type) throws io.craft.core.thrift.TException {
        MessageProducer producer = new ClientMessageProducer(methodName, args, type);
        return write(producer);
    }

    protected void receiveBase(Future<CraftMessage> future, TSerializable result, String methodName) throws io.craft.core.thrift.TException {
        CraftMessage message = null;
        try {
            try {
                message = future.get();
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
                throw new TException(t.getMessage(), t);
            }

            if (message == null) {
                throw new TException("response is null");
            }

            TMessage msg = message.readMessageBegin();
            if (msg.type == TMessageType.EXCEPTION) {
                TException x = new TException();
                x.read(message);
                message.readMessageEnd();
                throw x;
            }

            result.read(message);
            message.readMessageEnd();
        } finally {
            if (message != null) {
                message.release();
            }
        }
    }

    private static class ClientInitProducer implements MessageProducer {

        private String serviceName;

        private int messageId;

        public ClientInitProducer(String serviceName) {
            this.serviceName = serviceName;
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
        public CraftMessage produce(Channel channel) throws io.craft.core.thrift.TException {
            CraftMessage message = new CraftMessage(channel);
            message.writeMessageBegin(new TMessage(serviceName, TMessageType.REGISTER, messageId));
            message.writeMessageEnd();
            return message;
        }
    }

    private static class ClientMessageProducer implements MessageProducer {

        private String methodName;

        private TArgs args;

        private TMessageType type;

        private int messageId;

        public ClientMessageProducer(String methodName, TArgs args, TMessageType type) {
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
        public CraftMessage produce(Channel channel) throws io.craft.core.thrift.TException {
            CraftMessage message = new CraftMessage(channel);
            message.writeMessageBegin(new TMessage(methodName, type, messageId));
            args.write(message);
            message.writeMessageEnd();
            return message;
        }
    }
}
