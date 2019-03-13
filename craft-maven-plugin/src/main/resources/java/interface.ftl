<#import "./common.ftl" as common>
package ${packageName};

public interface ${className} {

    <#list methods as method>
    <#if method.deprecated>
    @java.lang.Deprecated
    </#if>
    ${method.returnValue.fullClassName} ${method.name}(<#list method.parameters as parameter>${parameter.fullClassName} ${parameter.name}<#sep>, </#sep></#list>) throws io.craft.core.thrift.TException;

    </#list>

    <#list methods as method>
    <@format blank=4>
    <@common.args method.name+"_args" method.parameters />
    <#if method.returnValue.type.getName() == "VOID">
        <#assign returnFields=[] />
    <#else>
        <#assign returnFields=[method.returnValue] />
    </#if>

    <@common.struct method.name+"_result" returnFields />
    </@format>
    </#list>

    class Processor<I extends ${className}> extends io.craft.core.thrift.TProcessor<I> {

        private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Processor.class);

        public Processor(I iface) {
            super(getProcessMap(iface, new java.util.HashMap<java.lang.String, io.craft.core.thrift.TFunction<I, ? extends io.craft.core.thrift.TArgs, ? extends io.craft.core.thrift.TSerializable>>()));
        }

        private static <I extends ${className}> java.util.Map<java.lang.String, io.craft.core.thrift.TFunction<I, ? extends io.craft.core.thrift.TArgs, ? extends io.craft.core.thrift.TSerializable>> getProcessMap(I iface, java.util.Map<java.lang.String, io.craft.core.thrift.TFunction<I, ? extends io.craft.core.thrift.TArgs, ? extends io.craft.core.thrift.TSerializable>> processMap) {
            <#list methods as method>
            processMap.put("${method.name}", new ${method.name}(iface));
            </#list>
            return processMap;
        }

        <#list methods as method>
        public static class ${method.name}<I extends ${className}> extends io.craft.core.thrift.TFunction<I, ${method.name}_args, ${method.name}_result> {

            public ${method.name}(I iface) {
                super(iface, "${method.name}");
            }

            public ${method.name}_args getEmptyArgsInstance() {
                return new ${method.name}_args();
            }

            public ${method.name}_result getResult(${method.name}_args args) throws io.craft.core.thrift.TException {
                ${method.name}_result result = new ${method.name}_result();
                <#if method.returnValue.className != "void">
                result.${method.returnValue.name} = iface.${method.name}(<#list method.parameters as parameter>args.${parameter.name}<#sep>, </#sep></#list>);
                <#else>
                iface.${method.name}(<#list method.parameters as parameter>args.${parameter.name}<#sep>, </#sep></#list>);
                </#if>
                return result;
            }
        }

        </#list>
    }

    class Client extends io.craft.core.client.CraftClient implements ${className} {

        public static final String SERVICE_NAME = "${packageName}";

        public Client() {
            super(SERVICE_NAME);
        }

        <#list methods as method>
        public ${method.returnValue.fullClassName} ${method.name}(<#list method.parameters as parameter>${parameter.fullClassName} ${parameter.name}<#sep>, </#sep></#list>) throws io.craft.core.thrift.TException {
            io.netty.util.concurrent.Future<io.craft.core.message.CraftMessage> future = send_${method.name}(<#list method.parameters as parameter>${parameter.name}<#sep>, </#sep></#list>);
            <#if method.returnValue.className == "void">
            recv_${method.name}(future);
            <#else>
            return recv_${method.name}(future);
            </#if>
        }

        private io.netty.util.concurrent.Future<io.craft.core.message.CraftMessage> send_${method.name}(<#list method.parameters as parameter>${parameter.fullClassName} ${parameter.name}<#sep>, </#sep></#list>) throws io.craft.core.thrift.TException  {
            ${method.name}_args args = new ${method.name}_args();
            args.setServiceName(SERVICE_NAME);
            String traceId = io.craft.core.util.TraceUtil.getTraceId();
            if (traceId != null) {
                args.setTraceId(traceId);
            } else {
                args.setTraceId(io.craft.core.util.TraceUtil.generateTraceId());
            }
            args.setCookie(io.craft.core.util.TraceUtil.getCookie());
            <#list method.parameters as parameter>
            args.set${parameter.name?cap_first}(${parameter.name});
            </#list>
            return sendBase("${method.name}", args);
        }

        private ${method.returnValue.fullClassName} recv_${method.name}(io.netty.util.concurrent.Future<io.craft.core.message.CraftMessage> future) throws io.craft.core.thrift.TException  {
            ${method.name}_result result = new ${method.name}_result();
            receiveBase(future, result, "${method.name}");
            <#if method.returnValue.type.getName() != "VOID">
            if (result.${method.returnValue.name} != null) {
                return result.${method.returnValue.name};
            }
            <#if method.returnValue.required>
            throw new io.craft.thrift.TException("${method.name} return value required");
            <#else>
            return null;
            </#if>
            </#if>
        }

        </#list>
    }
}