package io.craft.proxy.proxy;

import io.craft.core.client.BaseCraftClient;
import io.craft.core.message.CraftMessage;
import io.craft.core.thrift.TException;
import io.craft.core.thrift.TMessage;
import io.craft.core.thrift.TMessageType;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProxyClient extends BaseCraftClient {

    public ProxyClient(Bootstrap bootstrap, EventLoopGroup executors) {
        super(bootstrap, executors);
    }

    public void write(Channel channel, TMessage header, CraftMessage message) throws TException {
        MessageProducer producer = new ProxyMessageProducer(header, message);
        Future<CraftMessage> future = write(producer);
        if (future.isDone()) {
            response(channel, header, future);
        } else {
            future.addListener(f -> {
                response(channel, header, (Future<CraftMessage>) f);
            });
        }
    }

    public void response(Channel channel, TMessage header, Future<CraftMessage> future) throws TException {
        if (!future.isSuccess()) {
            CraftMessage error = new CraftMessage(channel);
            TException ex = new TException(future.cause().getMessage(), future.cause());
            error.writeMessageBegin(new TMessage("exception", TMessageType.EXCEPTION, header.sequence));
            ex.write(error);
            error.writeMessageEnd();
            channel.writeAndFlush(error);
        } else {
            //服务端返回的消息
            CraftMessage response = future.getNow();
            //替换服务端发过来的messageId, 使用客户端传过来的messageId
            response.setMessageSequence(header.sequence);
            channel.writeAndFlush(response);
            logger.debug("client seq={}, request success", header.sequence);
        }
    }

    private static class ProxyMessageProducer implements MessageProducer {

        private TMessage header;

        private CraftMessage message;

        private int messageId;

        public ProxyMessageProducer(TMessage header, CraftMessage message) {
            this.header = header;
            this.message = message;
        }

        @Override
        public void setMessageId(int messageId) {
            this.messageId = messageId;
        }

        @Override
        public int getMessageId() {
            return messageId;
        }

        @Override
        public CraftMessage produce(Channel channel) throws TException {
            message.setMessageSequence(messageId);
            return message;
        }
    }
}
