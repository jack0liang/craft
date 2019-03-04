package io.craft.server.executor;

import io.craft.core.constant.Constants;
import io.craft.core.exception.CraftException;
import io.craft.core.message.CraftFramedMessage;
import io.craft.core.message.TByteBufProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;

@Slf4j
public class CraftBusinessTask implements Runnable {

    private ChannelHandlerContext context;

    private TProcessor processor;

    private CraftFramedMessage request;

    public CraftBusinessTask(ChannelHandlerContext context, TProcessor processor, CraftFramedMessage request) {
        this.context = context;
        this.processor = processor;
        this.request = request;
    }

    @Override
    public void run() {

//        long latency = System.currentTimeMillis() - message.getRequestTime();
//
//        if (latency > message.getTimeout()) {
//            //大于500ms的请求，直接丢弃
//            try {
//                logger.debug("discard request latency = {}, message = {}", latency, message);
//                if (!context.isRemoved()) {
//                    context.writeAndFlush(new TApplicationException(TApplicationException.INTERNAL_ERROR, "request discarded"));
//                }
//            } finally {
//                message.release();
//            }
//            return;
//        }


        CraftFramedMessage response = new CraftFramedMessage(context.channel());

        try {
            logger.debug("request={}, refCnt={} process", request, request.refCnt());

            processor.process(request, response);

            Future future = context.writeAndFlush(response);
            if (future.isDone()) {
                if (!future.isSuccess()) {
                    logger.error("write response failed, error={}", future.cause().getMessage(), future.cause());
                }
            } else {
                future.addListener(f -> {
                    logger.error("write response failed, error={}", f.cause().getMessage(), f.cause());
                });
            }

        } catch (Exception e) {
            response.release();
            TMessage header;
            try {
                logger.error("process error={}", e.getMessage(), e);
                header = request.getMessageHeader();
                context.writeAndFlush(new CraftException(header.seqid));
            } catch (TException te) {
                //获取不到messageId, 则强制将连接关闭
                logger.error("get messageId error, error={}", te.getMessage(), te);
                context.close();
            }
        } finally {
            request.release();
        }
    }
}
