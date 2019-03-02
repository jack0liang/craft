package io.craft.proxy.proxy;

import io.craft.core.client.BaseCraftClient;
import io.craft.core.constant.Constants;
import io.craft.core.message.CraftFramedMessage;
import io.craft.core.transport.TByteBuf;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;

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
            channel.writeAndFlush(future.cause());
        } else {
            CraftFramedMessage message = future.getNow();

            message.getBuffer().markReaderIndex();

            ByteBuf buffer = channel.alloc().directBuffer(Constants.DEFAULT_BYTEBUF_SIZE);
            TTransport tin = new TByteBuf(message.getBuffer());
            TProtocol pin = new TBinaryProtocol(tin);

            TTransport tout = new TByteBuf(buffer);
            TProtocol pout = new TBinaryProtocol(tout);

            //替换服务端发过来的messageId, 使用客户端传过来的messageId, 替换messageId不改变消息头长度,是安全的
            TMessage serverRespHead = pin.readMessageBegin();
            TMessage head = new TMessage(serverRespHead.name, serverRespHead.type, header.seqid);
            pout.writeMessageBegin(head);

            message.getBuffer().setBytes(0, buffer);

            message.getBuffer().resetReaderIndex();

            channel.writeAndFlush(message);

            logger.debug("client seq={}, server seq={}, request success", header.seqid, serverRespHead.seqid);
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
            ByteBuf buffer = channel.alloc().directBuffer(Constants.DEFAULT_BYTEBUF_SIZE);
            TTransport tin = new TByteBuf(buffer);
            TProtocol pin = new TBinaryProtocol(tin);
            //替换客户端发过来的messageId, 使用自己的messageId, 替换messageId不改变消息头长度,是安全的
            TMessage msg = new TMessage(header.name, header.type, messageId);
            pin.writeMessageBegin(msg);

            message.getBuffer().setBytes(0, buffer);

            return message;
        }
    }
}
