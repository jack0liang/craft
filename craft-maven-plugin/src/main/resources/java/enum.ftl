package ${packageName};

<#if deprecated>
@java.lang.Deprecated
</#if>
public enum ${className} implements io.craft.core.thrift.TEnum {

    <#list fields as field>
    <#if field.deprecated>
    @java.lang.Deprecated
    </#if>
    ${field.name}(${field.sequence}),

    </#list>
    ;

    private final int value;

    ${className}(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    public static ${className} findByValue(int value) {
        switch (value) {
            <#list fields as field>
            case ${field.sequence}:
                return ${field.name};
            </#list>
            default:
                return null;
        }
    }
}