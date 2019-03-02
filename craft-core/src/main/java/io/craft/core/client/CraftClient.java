package io.craft.core.client;

import io.craft.core.codec.CraftFramedMessageDecoder;
import io.craft.core.codec.CraftFramedMessageEncoder;
import io.craft.core.constant.Constants;
import io.craft.core.message.CraftFramedMessage;
import io.craft.core.transport.TByteBuf;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
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

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CraftClient {

    private static final Logger logger = LoggerFactory.getLogger(CraftClient.class);

    private String proxyHost;

    private int proxyPort;

    private EventLoopGroup executors;

    private AtomicInteger sequence;

    private Bootstrap bootstrap;

    private AtomicBoolean isConnecting;

    private AtomicBoolean isConnected;

    private Queue<WriteEntity> queue;

    private Channel channel;

    private Map<Integer, MessageEntity> messageMap;

    public CraftClient() {
        //获取
        this.executors = new NioEventLoopGroup();
        this.sequence = new AtomicInteger(0);
        this.bootstrap = new Bootstrap();
        this.bootstrap
                .group(this.executors)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_TIMEOUT, 5000)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new CraftFramedMessageDecoder())
                                .addLast(new CraftFramedMessageEncoder())
                                .addLast(new ClientChannelHandler());
                    }
                });

        this.isConnecting = new AtomicBoolean(false);
        this.isConnected = new AtomicBoolean(false);
        this.queue = new ConcurrentLinkedQueue<>();
        this.messageMap = new ConcurrentHashMap<>();
    }

    public CraftClient(String proxyHost, int proxyPort) {
        this();
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    private int write(String methodName, TBase<?, ?> args, byte type) throws TException {
        int messageId = sequence.getAndIncrement();
        MessageEntity messageEntity = new MessageEntity();

        messageMap.put(messageId, messageEntity);

        logger.debug("seq={}, send data", messageId);

        try {
            synchronized (messageEntity) {

                WriteEntity entity = new WriteEntity(methodName, args, type, messageId);

                if (isConnected.get()) {
                    //已连接的,直接写到channel
                    doWrite(entity);
                } else {
                    //没连接上, 统一先放入队列
                    queue.offer(entity);
                    //开始连接, 这里只需要一个线程去连接, 其他的只需要往队列丢就够了
                    if (isConnecting.compareAndSet(false, true)) {
                        this.bootstrap.remoteAddress(this.proxyHost, this.proxyPort);
                        ChannelFuture future = this.bootstrap.connect();
                        this.channel = future.channel();
                        if (future.isDone()) {
                            processConnected(future);
                        } else {
                            future.addListener(f -> {
                                processConnected((ChannelFuture) f);
                            });
                        }
                    }
                }

                logger.debug("seq={}, wait response", messageId);

                try {
                    messageEntity.wait();
                } catch (InterruptedException e) {
                    throw new TApplicationException(TApplicationException.INTERNAL_ERROR, "wait error, error=" + e.getMessage());
                }

            }
        } catch (Throwable t) {
            //发生任何异常, 都要把messageMap里面对应的messageId去掉
            logger.debug("remove messageMap, messageId={}", messageId);
            messageMap.remove(messageId);
            throw new TApplicationException(TApplicationException.INTERNAL_ERROR, "system error, error=" + t.getMessage());
        }

        return messageId;
    }

    private void processConnected(ChannelFuture future) throws TException {
        isConnecting.set(false);

        WriteEntity message;

        if (!future.isSuccess()) {
            logger.error("socket connect error, error={}", future.cause().getMessage(), future.cause());
            //逐个通知
            while((message = queue.poll()) != null) {
                processReceived(message.getMessageId(), null);
            }
            throw new TApplicationException(TApplicationException.INTERNAL_ERROR, "socket connect error, error=" + future.cause().getMessage());
        }
        logger.debug("connected channel={}", channel);
        isConnected.set(true);


        while((message = queue.poll()) != null) {
            doWrite(message);
        }
    }

    private void processWrited(ChannelFuture future, int messageId) throws TException {
        if (future.isSuccess()) {
            //写入成功,不用处理
            return;
        }
        //发生异常
        logger.error("write failed, error={}", future.cause().getMessage(), future.cause());
        isConnected.set(false);
        processReceived(messageId, null);
    }

    private void processReceived(int messageId, CraftFramedMessage message) {
        MessageEntity messageEntity = messageMap.get(messageId);
        Throwable t = null;
        if (message == null) {
            t = new Throwable("error");
        }
        if (t == null) {
            logger.debug("process received, messageId={}", messageId);
        } else {
            logger.error("process received error, messageId={}", messageId, t);
        }
        synchronized (messageEntity) {
            messageEntity.setResponse(message);
            messageEntity.notify();
        }
    }

    private void doWrite(WriteEntity entity) throws TException {

        ByteBuf buffer = channel.alloc().directBuffer(Constants.DEFAULT_BYTEBUF_SIZE);
        TByteBuf tout = new TByteBuf(buffer);
        TProtocol pout = new TBinaryProtocol(tout);

        pout.writeMessageBegin(new TMessage(entity.getMethodName(), entity.getType(), entity.getMessageId()));
        entity.getArgs().write(pout);
        pout.writeMessageEnd();

        CraftFramedMessage message = new CraftFramedMessage(buffer, 0, 0);
        logger.debug("do write message messageId={}", entity.getMessageId());
        messageMap.get(entity.getMessageId()).setRequest(message);
        ChannelFuture future = channel.writeAndFlush(message);
        if (future.isDone()) {
            //写完了,但是没有成功
            processWrited(future, entity.getMessageId());
        } else {
            future.addListener(f -> {
                if (f.isDone()) {
                    //写完了,但是没有成功
                    processWrited((ChannelFuture) f, entity.getMessageId());
                }
            });
        }
    }

    protected int sendBase(String methodName, TBase<?,?> args) throws TException {
        return sendBase(methodName, args, TMessageType.CALL);
    }


    protected int sendBase(String methodName, TBase<?,?> args, byte type) throws TException {
        return write(methodName, args, type);
    }

    protected void receiveBase(int messageId, TBase<?,?> result, String methodName) throws TException {
        try {
            MessageEntity messageEntity = messageMap.get(messageId);

            logger.debug("seq={}, response received", messageId);

            CraftFramedMessage response = messageEntity.getResponse();

            if (response == null) {
                throw new TApplicationException(TApplicationException.INTERNAL_ERROR, "response is null");
            }

            TTransport tin = new TByteBuf(response.getBuffer());
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
        } finally {
            logger.debug("remove messageMap, messageId={}", messageId);
            messageMap.remove(messageId);
        }
    }

    public void close() {
        if (isConnected.get()) {
            channel.close();
        }
    }


    private static class WriteEntity {

        private String methodName;
        private TBase<?, ?> args;
        private byte type;
        private int messageId;

        public WriteEntity(String methodName, TBase<?, ?> args, byte type, int messageId) {
            this.methodName = methodName;
            this.args = args;
            this.type = type;
            this.messageId = messageId;
        }

        public String getMethodName() {
            return methodName;
        }

        public TBase<?, ?> getArgs() {
            return args;
        }

        public byte getType() {
            return type;
        }

        public int getMessageId() {
            return messageId;
        }
    }

    private static class MessageEntity {

        private CraftFramedMessage request;

        private CraftFramedMessage response;

        public CraftFramedMessage getRequest() {
            return request;
        }

        public void setRequest(CraftFramedMessage request) {
            this.request = request;
        }

        public CraftFramedMessage getResponse() {
            return response;
        }

        public void setResponse(CraftFramedMessage response) {
            this.response = response;
        }
    }

    @ChannelHandler.Sharable
    private class ClientChannelHandler extends SimpleChannelInboundHandler<CraftFramedMessage> {

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            //读取发生异常, 关闭连接, 等待下一次重连
            isConnected.set(false);
            logger.error("socket read error, error={}", cause.getMessage(), cause);
            //通知所有等待的线程
            for(Integer messageId : messageMap.keySet()) {
                processReceived(messageId, null);
            }
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, CraftFramedMessage message) throws Exception {
            message.retain();

            ByteBuf buffer = message.getBuffer();

            buffer.markReaderIndex();

            TTransport tin = new TByteBuf(buffer);
            TProtocol pin = new TBinaryProtocol(tin);

            TMessage msg = pin.readMessageBegin();

            logger.debug("seq={}, notify response", msg.seqid);

            buffer.resetReaderIndex();

            processReceived(msg.seqid, message);
        }
    }
}
