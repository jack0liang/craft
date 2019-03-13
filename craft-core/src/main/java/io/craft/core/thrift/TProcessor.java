package io.craft.core.thrift;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public abstract class TProcessor<I> {

    private static final Logger logger = LoggerFactory.getLogger(TProcessor.class);

    private Map<String, TFunction<I, ? extends TArgs, ? extends TSerializable>> functionMap;

    protected TProcessor(Map<String, TFunction<I, ? extends TArgs, ? extends TSerializable>> functionMap) {
        this.functionMap = functionMap;
    }

    public final void process(TProtocol in, TProtocol out) {
        TMessage message = null;
        try {
            message = in.readMessageBegin();
            TFunction function = functionMap.get(message.name);
            if (function == null) {
                TProtocolUtil.skip(in, TType.STRUCT);
                in.readMessageEnd();
                throw new TException("Invalid method name: '" + message.name + "'");
            }
            TSerializable result = function.process(in);
            out.writeMessageBegin(new TMessage(message.name, TMessageType.REPLY, message.sequence));
            result.write(out);
            out.writeMessageEnd();
        } catch (Throwable t) {
            logger.error("process error, function={}, error={}", message.name, t.getMessage(), t);
            if (message == null) {
                return;
            }
            TException ce;
            if (!(t instanceof TException)) {
                ce = new TException(t.getMessage(), t);
            } else {
                ce = (TException) t;
            }
            out.writeMessageBegin(new TMessage(message.name, TMessageType.EXCEPTION, message.sequence));
            ce.write(out);
            out.writeMessageEnd();
        }
    }

}
