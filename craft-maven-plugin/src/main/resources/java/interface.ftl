<#import "./module/class.ftl" as clazz>
package ${packageName};

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Craft IDL Compiler (${version})", date = "${date?datetime}")
public interface ${className} {

    <#list methods as method>
    <#if method.deprecated>
    @java.lang.Deprecated
    </#if>
    ${method.returnValue.fullClassName} ${method.name}(<#list method.parameters as parameter>${parameter.fullClassName} ${parameter.name}<#sep>, </#sep></#list>) throws org.apache.thrift.TException;

    </#list>
    <@format blank=4>
    <#list fields as class>
    <@clazz.class className=class.className fields=class.fields static=true />

    </#list>
    </@format>

    public static class Processor<I extends ${className}> extends org.apache.thrift.TBaseProcessor<I> implements org.apache.thrift.TProcessor {

        private static final org.slf4j.Logger _LOGGER = org.slf4j.LoggerFactory.getLogger(Processor.class.getName());

        public Processor(I iface) {
            super(iface, getProcessMap(new java.util.HashMap<java.lang.String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>>()));
        }

        protected Processor(I iface, java.util.Map<java.lang.String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>> processMap) {
            super(iface, getProcessMap(processMap));
        }

        private static <I extends ${className}> java.util.Map<java.lang.String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>> getProcessMap(java.util.Map<java.lang.String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>> processMap) {
            <#list methods as method>
            processMap.put("${method.name}", new ${method.name}());
            </#list>
            return processMap;
        }

        <#list methods as method>
        public static class ${method.name}<I extends ${className}> extends org.apache.thrift.ProcessFunction<I, ${method.name}_args> {

            public ${method.name}() {
                super("${method.name}");
            }

            public ${method.name}_args getEmptyArgsInstance() {
                return new ${method.name}_args();
            }

            protected boolean isOneway() {
                return false;
            }

            @Override
            protected boolean rethrowUnhandledExceptions() {
                return false;
            }

            public ${method.name}_result getResult(I iface, ${method.name}_args args) throws org.apache.thrift.TException {
                ${method.name}_result result = new ${method.name}_result();
                io.craft.core.util.TraceUtil.setTraceId(args.getTraceId());
                io.craft.core.util.TraceUtil.setHeader(args.getHeader());
                <#if method.returnValue.className != "void">
                result.${method.returnValue.name} = iface.${method.name}(<#list method.parameters as parameter>args.${parameter.name}<#sep>, </#sep></#list>);
                <#else>
                iface.${method.name}(<#list method.parameters as parameter>args.${parameter.name}<#sep>, </#sep></#list>);
                </#if>
                //请求完成之后就清空trace信息
                io.craft.core.util.TraceUtil.clear();
                return result;
            }
        }

        </#list>
    }

    public static class Client extends io.craft.core.client.CraftClient implements ${className} {

        public static final String SERVICE_NAME = "${packageName}";

        public ${className}() {
            super(SERVICE_NAME);
        }

        <#list methods as method>
        public ${method.returnValue.fullClassName} ${method.name}(<#list method.parameters as parameter>${parameter.fullClassName} ${parameter.name}<#sep>, </#sep></#list>) throws org.apache.thrift.TException
        {
            io.netty.util.concurrent.Future<io.craft.core.message.CraftFramedMessage> future = send_${method.name}(<#list method.parameters as parameter>${parameter.name}<#sep>, </#sep></#list>);
            <#if method.returnValue.className == "void">
            recv_${method.name}(future);
            <#else>
            return recv_${method.name}(future);
            </#if>
        }

        private io.netty.util.concurrent.Future<io.craft.core.message.CraftFramedMessage> send_${method.name}(<#list method.parameters as parameter>${parameter.fullClassName} ${parameter.name}<#sep>, </#sep></#list>) throws org.apache.thrift.TException
        {
            ${method.name}_args args = new ${method.name}_args();
            args.setServiceName(SERVICE_NAME);
            String traceId = io.craft.core.util.TraceUtil.getTraceId();
            if (traceId != null) {
                args.setTraceId(traceId);
            } else {
                args.setTraceId(io.craft.core.util.TraceUtil.generateTraceId());
            }
            args.setHeader(io.craft.core.util.TraceUtil.getHeader());
            <#list method.parameters as parameter>
            args.set${parameter.name?cap_first}(${parameter.name});
            </#list>
            return sendBase("${method.name}", args);
        }

        private ${method.returnValue.fullClassName} recv_${method.name}(io.netty.util.concurrent.Future<io.craft.core.message.CraftFramedMessage> future) throws org.apache.thrift.TException
        {
            ${method.name}_result result = new ${method.name}_result();
            receiveBase(future, result, "${method.name}");
            <#if method.returnValue.className != "void">
            if (result.${method.returnValue.name} != null) {
                return result.${method.returnValue.name};
            }
            <#if method.returnValue.required>
            throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "${method.name} failed: unknown result");
            <#else>
            return null;
            </#if>
            </#if>
        }

        </#list>
    }
}