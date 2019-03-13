package io.craft.core.thrift;

import io.craft.core.util.TraceUtil;

public abstract class TFunction<I, P extends TArgs, R extends TSerializable> {

    protected I iface;

    protected String methodName;

    public TFunction(I iface, String methodName) {
        this.iface = iface;
        this.methodName = methodName;
    }

    public final R process(TProtocol in) throws TException {
        P args = getEmptyArgsInstance();
        try {
            args.read(in);
        } catch (Throwable t) {
            in.readMessageEnd();
            TException ce;
            if (!(t instanceof TException)) {
                ce = new TException(t.getMessage(), t);
            } else {
                ce = (TException) t;
            }
            throw ce;
        }
        TraceUtil.setTraceId(args.getTraceId());
        TraceUtil.setCookie(args.getCookie());
        R result = getResult(args);
        TraceUtil.clear();
        return result;
    }

    public abstract P getEmptyArgsInstance();

    public abstract R getResult(P args) throws TException;
}
