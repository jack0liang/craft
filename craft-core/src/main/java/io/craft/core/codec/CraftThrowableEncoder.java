package io.craft.core.codec;

import io.craft.core.constant.Constants;
import io.craft.core.exception.CraftException;
import io.craft.core.message.CraftFramedMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;

import java.util.List;

@ChannelHandler.Sharable
public class CraftThrowableEncoder extends MessageToMessageEncoder<CraftException> {

    @Override
    protected void encode(ChannelHandlerContext ctx, CraftException cause, List<Object> out) throws Exception {
        CraftFramedMessage message = new CraftFramedMessage(ctx.channel());

        message.writeMessageBegin(new TMessage("exception", TMessageType.EXCEPTION, cause.getMessageId()));
        cause.write(message);
        message.writeMessageEnd();

        out.add(message);
    }

}
