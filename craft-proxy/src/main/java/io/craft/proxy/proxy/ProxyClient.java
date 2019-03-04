package io.craft.proxy.proxy;

import io.craft.core.client.BaseCraftClient;
import io.craft.core.exception.CraftException;
import io.craft.core.message.CraftFramedMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMessage;

@Slf4j
public class ProxyClient extends BaseCraftClient {

    public ProxyClient(Bootstrap bootstrap, EventLoopGroup executors) {
        super(bootstrap, executors);
    }

    public void write(Channel channel, TMessage header, CraftFramedMessage message) throws TException {
        MessageProducer producer = new ProxyMessageProducer(header, message);
        Future<CraftFramedMessage> future = write(producer);
        if (future.isDone()) {
            response(channel, header, future);
        } else {
            future.addListener(f -> {
                response(channel, header, (Future<CraftFramedMessage>) f);
            });
        }
    }

    public void response(Channel channel, TMessage header, Future<CraftFramedMessage> future) throws TException {
        if (!future.isSuccess()) {
            channel.writeAndFlush(new CraftException(header.seqid));
        } else {
            //服务端返回的消息
            CraftFramedMessage response = future.getNow();
            //替换服务端发过来的messageId, 使用客户端传过来的messageId
            response.setMessageSequence(header.seqid);
            channel.writeAndFlush(response);
            logger.debug("client seq={}, request success", header.seqid);
        }
    }

    private static class ProxyMessageProducer implements MessageProducer {

        private TMessage header;

        private CraftFramedMessage message;

        private int messageId;

        public ProxyMessageProducer(TMessage header, CraftFramedMessage message) {
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
        public CraftFramedMessage produce(Channel channel) throws TException {
            message.setMessageSequence(header.seqid);
            return message;
        }
    }
}
