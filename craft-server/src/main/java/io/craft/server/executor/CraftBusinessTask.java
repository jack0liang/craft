package io.craft.server.executor;

import io.craft.core.constant.Constants;
import io.craft.core.message.CraftFramedMessage;
import io.craft.core.transport.TByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;

@Slf4j
public class CraftBusinessTask implements Runnable {

    private ChannelHandlerContext context;

    private TProcessor processor;

    private CraftFramedMessage message;

    public CraftBusinessTask(ChannelHandlerContext context, TProcessor processor, CraftFramedMessage message) {
        this.context = context;
        this.processor = processor;
        this.message = message;
    }

    @Override
    public void run() {

        long latency = System.currentTimeMillis() - message.getRequestTime();

        if (latency > message.getTimeout()) {
            //大于500ms的请求，直接丢弃
            try {
                logger.debug("discard request latency = {}, message = {}", latency, message);
                if (!context.isRemoved()) {
                    context.writeAndFlush(new TApplicationException(TApplicationException.INTERNAL_ERROR, "request discarded"));
                }
            } finally {
                message.release();
            }
            return;
        }

        ByteBuf writeBuffer = context.alloc().directBuffer(Constants.DEFAULT_BYTEBUF_SIZE);

        try {
            logger.debug("message {}, refCnt = {} process", message, message.refCnt());
            TTransport tin = new TByteBuf(message.getBuffer());
            TProtocol pin = new TBinaryProtocol(tin);

            TTransport tout = new TByteBuf(writeBuffer);
            TProtocol pout = new TBinaryProtocol(tout);

            CraftFramedMessage returnMessage = new CraftFramedMessage(writeBuffer, 0, 0);

            logger.debug("message {} process", message);
            processor.process(pin, pout);

            try {
                context.writeAndFlush(returnMessage);
            } catch (Exception e) {
                //发生异常时，关闭异常的连接
                context.close();
                logger.error("channel write error {}", e.getMessage(), e);
            }

        } catch (Exception e) {
            writeBuffer.release();
            context.writeAndFlush(e);
            logger.error("process error {}", e.getMessage(), e);
        } finally {
            logger.debug("message {} release", message);
            message.release();
        }
    }
}
