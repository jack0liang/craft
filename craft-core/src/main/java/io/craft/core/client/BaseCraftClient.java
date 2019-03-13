package io.craft.core.client;

import io.craft.core.codec.CraftFramedMessageDecoder;
import io.craft.core.codec.CraftFramedMessageEncoder;
import io.craft.core.message.CraftMessage;
import io.craft.core.thrift.TException;
import io.craft.core.thrift.TMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class BaseCraftClient {

    private static final Logger logger = LoggerFactory.getLogger(BaseCraftClient.class);

    private Bootstrap bootstrap;

    private EventLoopGroup executors;

    private AtomicInteger sequence;

    private AtomicBoolean isConnecting;

    private AtomicBoolean isConnected;

    private Queue<MessageProducer> queue;

    private Channel channel;

    private Map<Integer, DefaultPromise<CraftMessage>> promiseMap;


    public BaseCraftClient(Bootstrap bootstrap, EventLoopGroup executors) {
        this.bootstrap = bootstrap;
        this.executors = executors;
        this.sequence = new AtomicInteger(0);
        this.isConnecting = new AtomicBoolean(false);
        this.isConnected = new AtomicBoolean(false);
        this.queue = new ConcurrentLinkedQueue<>();
        this.promiseMap = new ConcurrentHashMap<>();
        //设置io线程
        this.bootstrap.group(executors);
        //设置handler
        this.bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline()
                        .addLast(new CraftFramedMessageDecoder())
                        .addLast(new CraftFramedMessageEncoder())
                        .addLast(new ClientChannelHandler());
            }
        });
    }

    protected Future<CraftMessage> write(MessageProducer producer) throws TException {

        //生成消息序号
        int messageId = sequence.getAndIncrement();
        //生成future
        DefaultPromise<CraftMessage> promise = new DefaultPromise<>(this.executors.next());
        promise.setUncancellable();
        //设置Producer的消息ID
        producer.setMessageId(messageId);
        //将消息ID跟future绑定起来
        //在处理连接错误时,write不允许写入,否则会发生错误
        synchronized (promiseMap) {
            promiseMap.put(messageId, promise);
        }

        if (isConnected.get()) {
            //已连接的,直接写到channel
            logger.debug("seq={}, direct write", messageId);
            doWrite(producer);
        } else {
            //没连接上, 统一先放入队列
            logger.debug("seq={}, queue offer", messageId);
            queue.offer(producer);
            //开始连接, 这里只需要一个线程去连接, 其他的只需要往队列丢就够了
            if (isConnecting.compareAndSet(false, true)) {
                ChannelFuture cf = this.bootstrap.connect();
                //当连接失败时,触发,如果连接成功,则在ClientChannelHandler#channelActive中执行
                if (cf.isDone() && !cf.isSuccess()) {
                    processConnectFailed(cf);
                } else {
                    cf.addListener(f -> {
                        if (!f.isSuccess()) {
                            processConnectFailed((ChannelFuture) f);
                        }
                    });
                }
            }
        }

        return promise;
    }

    private void processConnectFailed(ChannelFuture future) throws TException {
        isConnecting.set(false);
        MessageProducer producer;
        logger.error("socket connect error, error={}", future.cause().getMessage(), future.cause());
        //逐个通知
        while((producer = queue.poll()) != null) {
            processReceived(producer.getMessageId(), null, future.cause());
        }
    }

    private void processConnected(Channel channel) throws TException {
        this.channel = channel;
        isConnecting.set(false);
        isConnected.set(true);
        logger.debug("connected channel={}", channel);

        MessageProducer producer;
        while((producer = queue.poll()) != null) {
            doWrite(producer);
        }
    }

    private void processWrited(ChannelFuture channelFuture, int messageId) throws TException {
        if (!channelFuture.isSuccess()) {
            logger.error("write failed, error={}", channelFuture.cause().getMessage(), channelFuture.cause());
            close();
            processReceived(messageId, null, channelFuture.cause());
        }
    }

    private void processReceived(int messageId, CraftMessage message, Throwable cause) throws TException {
        logger.debug("promise get, messageId={}", messageId);
        DefaultPromise<CraftMessage> promise = promiseMap.get(messageId);
        promiseMap.remove(messageId);
        logger.debug("promise removed, messageId={}, promise={}", messageId, promise);
        if (cause != null) {
            logger.error("process received error, messageId={}, error={}", messageId, cause.getMessage(), cause);
            promise.setFailure(cause);
        } else {
            logger.debug("process received success, messageId={}", messageId);
            promise.setSuccess(message);
        }
    }

    private void processReceiveFailed(Throwable cause) throws TException {
        //通知所有等待的线程
        Integer[] messageIds;
        //此处需要加一小段锁,不允许write再写入内容,避免发出错误的通知
        synchronized (promiseMap) {
            messageIds = new Integer[promiseMap.size()];
            messageIds = promiseMap.keySet().toArray(messageIds);
        }

        if (messageIds.length > 0) {
            logger.error("connection error={}, cause {} prmoise not done", cause.getMessage(), messageIds.length, cause);
        }

        for(Integer messageId : messageIds) {
            processReceived(messageId, null, cause);
        }
    }

    private void doWrite(MessageProducer producer) throws TException {
        CraftMessage message = producer.produce(channel);
        logger.debug("do write message messageId={}, readerIndex={}, readableBytes={}", producer.getMessageId(), message.readerIndex(), message.readableBytes());
        ChannelFuture future = channel.writeAndFlush(message);
        if (future.isDone()) {
            processWrited(future, producer.getMessageId());
        } else {
            future.addListener(f -> {
                processWrited((ChannelFuture) f, producer.getMessageId());
            });
        }
    }

    public void close() {
        if (isConnected.get()) {
            isConnected.set(false);
            channel.close();
        }
    }

    public interface MessageProducer {

        void setMessageId(int messageId);

        int getMessageId();

        CraftMessage produce(Channel channel) throws TException;

    }


    @ChannelHandler.Sharable
    private class ClientChannelHandler extends SimpleChannelInboundHandler<CraftMessage> {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            processConnected(ctx.channel());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            processReceiveFailed(new Exception("connection closed"));
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            //读取发生异常, 关闭连接, 等待下一次重连
            close();
            processReceiveFailed(cause);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, CraftMessage message) throws Exception {
            message.retain();
            TMessage msg = message.getHeader();
            logger.debug("seq={}, received response, refCnt={}", msg.sequence, message.refCnt());
            processReceived(msg.sequence, message, null);
        }
    }
}
