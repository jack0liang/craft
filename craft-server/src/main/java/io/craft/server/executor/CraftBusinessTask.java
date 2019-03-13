package io.craft.server.executor;

import io.craft.core.message.CraftMessage;
import io.craft.core.thrift.TException;
import io.craft.core.thrift.TMessage;
import io.craft.core.thrift.TMessageType;
import io.craft.core.thrift.TProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CraftBusinessTask implements Runnable {

    private ChannelHandlerContext context;

    private TProcessor processor;

    private CraftMessage request;

    public CraftBusinessTask(ChannelHandlerContext context, TProcessor processor, CraftMessage request) {
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


        CraftMessage response = new CraftMessage(context.channel());

        try {
            //logger.debug("request={}, refCnt={} process", request, request.refCnt());
            processor.process(request, response);

            Future future = context.writeAndFlush(response);
            if (future.isDone()) {
                if (!future.isSuccess()) {
                    logger.error("write response failed, error={}", future.cause().getMessage(), future.cause());
                }
            } else {
                future.addListener(f -> {
                    if (!f.isSuccess()) {
                        logger.error("write response failed, error={}", f.cause().getMessage(), f.cause());
                    }
                });
            }

        } catch (Exception e) {
            response.release();
            TMessage header;
            TException ce;
            if (e instanceof TException) {
                ce = (TException) e;
            } else {
                ce = new TException(e.getMessage(), e.getCause());
            }
            CraftMessage error = new CraftMessage(context.channel());
            try {
                header = request.getHeader();
                error.writeMessageBegin(new TMessage("exception", TMessageType.EXCEPTION, header.sequence));
                ce.write(error);
                error.writeMessageEnd();
                logger.error("business task execute error={}", e.getMessage(), e);
                context.writeAndFlush(error);
            } catch (Exception te) {
                //获取不到messageId, 则强制将连接关闭
                logger.error("write error, error={}", te.getMessage(), te);
                error.release();
                context.close();
            }
        } finally {
            request.release();
        }
    }
}
