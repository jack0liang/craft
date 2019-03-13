<#macro read field parent depth=0>
<@format blank=4*depth>
{
    <#if field.type.getName() == "LIST">
    io.craft.core.thrift.TList list${depth} = protocol.readListBegin();
    ${parent} = new java.util.ArrayList<${field.genericTypes[0].fullClassName}>(list${depth}.size);
    ${field.genericTypes[0].fullClassName} val${depth};
    for (int i${depth} = 0; i${depth} < list${depth}.size; ++i${depth}) {
        <@read field.genericTypes[0], "val"+depth depth+1 />
        ${parent}.add(val${depth});
    }
    protocol.readListEnd();
    <#elseif field.type.getName() == "SET">
    io.craft.core.thrift.TSet set${depth} = protocol.readSetBegin();
    ${parent} = new java.util.HashSet<${field.genericTypes[0].fullClassName}>(set${depth}.size);
    ${field.genericTypes[0].fullClassName} val${depth};
    for (int i${depth} = 0; i${depth} < set${depth}.size; ++i${depth}) {
        <@read field.genericTypes[0], "val"+depth depth+1 />
        ${parent}.add(val${depth});
    }
    protocol.readSetEnd();
    <#elseif field.type.getName() == "MAP">
    io.craft.core.thrift.TMap map${depth} = protocol.readMapBegin();
    ${parent} = new java.util.HashMap<${field.genericTypes[0].fullClassName},${field.genericTypes[1].fullClassName}>(2 * map${depth}.size);
    ${field.genericTypes[0].fullClassName} key${depth};
    ${field.genericTypes[1].fullClassName} val${depth};
    for (int i${depth} = 0; i${depth} < map${depth}.size; ++i${depth}) {
        <@read field.genericTypes[0], "key"+depth, depth+1 />
        <@read field.genericTypes[1], "val"+depth, depth+1 />
        ${parent}.put(key${depth}, val${depth});
    }
    <#elseif field.type.getName() == "STRUCT">
    ${parent} = new ${field.fullClassName}();
    ${parent}.read(protocol);
    <#elseif field.type.getCode() == "DATE">
    ${parent} = new java.util.Date(protocol.read${field.type.getName()?lower_case?cap_first}());
    <#else>
    ${parent} = protocol.read${field.type.getName()?lower_case?cap_first}();
    </#if>
}
</@format>
</#macro>

<#macro write field parent depth=0>
<@format blank=4*depth>
if (${parent} != null) {
    {
    <#if field.type.getName() == "SET">
        protocol.writeSetBegin(new io.craft.core.thrift.TSet(io.craft.core.thrift.TType.${field.genericTypes[0].type.getName()}, ${parent}.size()));
        for (${field.genericTypes[0].fullClassName} val${depth} : ${parent}) {
            <@write field.genericTypes[0], "val"+depth, depth+1 />
        }
        protocol.writeSetEnd();
    <#elseif field.type.getName() == "LIST">
        protocol.writeListBegin(new io.craft.core.thrift.TList(io.craft.core.thrift.TType.${field.genericTypes[0].type.getName()}, ${parent}.size()));
        for (${field.genericTypes[0].fullClassName} val${depth} : ${parent}) {
            <@write field.genericTypes[0], "val"+depth, depth+1 />
        }
        protocol.writeListEnd();
    <#elseif field.type.getName() == "MAP">
        protocol.writeMapBegin(new io.craft.core.thrift.TMap(io.craft.core.thrift.TType.${field.genericTypes[0].type.getName()}, io.craft.core.thrift.TType.${field.genericTypes[1].type.getName()}, ${parent}.size()));
        for (java.util.Map.Entry<${field.genericTypes[0].fullClassName}, ${field.genericTypes[1].fullClassName}> entry${depth} : ${parent}.entrySet()) {
            <@write field.genericTypes[0], "entry"+depth+".getKey()", depth+1 />
            <@write field.genericTypes[1], "entry"+depth+".getValue()", depth+1 />
        }
        protocol.writeMapEnd();
    <#elseif field.type.getName() == "STRUCT">
        ${parent}.write(protocol);
    <#elseif field.type.getCode() == "DATE">
        protocol.write${field.type.getName()?lower_case?cap_first}(${parent}.getTime());
    <#else>
        protocol.write${field.type.getName()?lower_case?cap_first}(${parent});
    </#if>
    }
}
</@format>
</#macro>

<#macro args className fields>
class ${className} extends io.craft.core.thrift.TArgs {

    private static final io.craft.core.thrift.TStruct STRUCT_DESC = new io.craft.core.thrift.TStruct("${className}");

    <#list fields as field>
    private static final io.craft.core.thrift.TField ${field.name?upper_case}_FIELD_DESC = new io.craft.core.thrift.TField("${field.name}", io.craft.core.thrift.TType.${field.type.getName()}, (short)${field.sequence});
    </#list>

    <#list fields as field>
    private ${field.fullClassName} ${field.name};
    </#list>

    public ${className}() {
    }

    <#if (fields?size &gt; 0) >
    public ${className}(
        <#list fields as field>
            ${field.fullClassName} ${field.name}<#sep>, </#sep>
        </#list>
        )
        {
        this();
        <#list fields as field>
        this.${field.name} = ${field.name};
        </#list>
    }
    </#if>

    <#list fields as field>
    <#if field.deprecated>
    @java.lang.Deprecated
    </#if>
    public ${field.fullClassName} get${field.name?cap_first}() {
        return this.${field.name};
    }

    <#if field.deprecated>
    @java.lang.Deprecated
    </#if>
    public ${className} set${field.name?cap_first}(${field.fullClassName} ${field.name}) {
        this.${field.name} = ${field.name};
        return this;
    }

    </#list>

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ${className} obj = (${className}) o;
        return true<#list fields as field> && java.util.Objects.equals(${field.name}, obj.${field.name})</#list>;
    }

    @Override
    public int hashCode() {
        int result = 0;
        <#list fields as field>
        result = 31 * result + (${field.name} != null ? ${field.name}.hashCode() : 0);
        </#list>
        return result;
    }

    @Override
    public java.lang.String toString() {
        java.lang.StringBuilder sb = new java.lang.StringBuilder("${className}(");
        <#list fields as field>
        sb.append("${field.name}:");
        sb.append(this.${field.name});
        <#sep>
            sb.append(",");
        </#sep>
        </#list>
        sb.append(")");
        return sb.toString();
    }

    @Override
    protected void readInternal(io.craft.core.thrift.TProtocol protocol, io.craft.core.thrift.TField field) throws io.craft.core.thrift.TException {
        <#list fields as field>
        switch (field.sequence) {
            case ${field.sequence}: // ${field.name} ${field.type.getName()}
                if (field.type == io.craft.core.thrift.TType.${field.type.getName()}) {
                    <@format blank=4*5>
                        <@read field "this."+field.name />
                    </@format>
                } else {
                    io.craft.core.thrift.TProtocolUtil.skip(protocol, field.type);
                }
                break;
            default:
                io.craft.core.thrift.TProtocolUtil.skip(protocol, field.type);
        }
        </#list>
    }

    @Override
    protected void validateInternal() throws io.craft.core.thrift.TException {
        // check for required fields, which can't be checked in the validate method
        <#list fields as field>
        <#if field.required?? && field.required>
        if (this.${field.name} == null) {
            throw new io.craft.core.thrift.TException("Required field '${field.name}' was not found in serialized data!");
        }
        </#if>
        </#list>
    }

    @Override
    protected void writeInternal(io.craft.core.thrift.TProtocol protocol) throws io.craft.core.thrift.TException {
    <#list fields as field>
        if (this.${field.name} != null) {
            protocol.writeFieldBegin(${field.name?upper_case}_FIELD_DESC);
            <@format blank=4*4>
            <@write field "this."+field.name />
            </@format>
            protocol.writeFieldEnd();
        }
    </#list>
    }

}
</#macro>

<#macro struct className fields>
class ${className} implements io.craft.core.thrift.TSerializable {

    private static final io.craft.core.thrift.TStruct STRUCT_DESC = new io.craft.core.thrift.TStruct("${className}");

    <#list fields as field>
    private static final io.craft.core.thrift.TField ${field.name?upper_case}_FIELD_DESC = new io.craft.core.thrift.TField("${field.name}", io.craft.core.thrift.TType.${field.type.getName()}, (short)${field.sequence});
    </#list>

    <#list fields as field>
    private ${field.fullClassName} ${field.name};
    </#list>

    public ${className}() {
    }

    <#if (fields?size &gt; 0) >
    public ${className}(
        <#list fields as field>
            ${field.fullClassName} ${field.name}<#sep>, </#sep>
        </#list>
        )
        {
        this();
        <#list fields as field>
        this.${field.name} = ${field.name};
        </#list>
    }
    </#if>

    <#list fields as field>
    <#if field.deprecated>
    @java.lang.Deprecated
    </#if>
    public ${field.fullClassName} get${field.name?cap_first}() {
        return this.${field.name};
    }

    <#if field.deprecated>
    @java.lang.Deprecated
    </#if>
    public ${className} set${field.name?cap_first}(${field.fullClassName} ${field.name}) {
        this.${field.name} = ${field.name};
        return this;
    }

    </#list>

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ${className} obj = (${className}) o;
        return true<#list fields as field> && java.util.Objects.equals(${field.name}, obj.${field.name})</#list>;
    }

    @Override
    public int hashCode() {
        int result = 0;
        <#list fields as field>
        result = 31 * result + (${field.name} != null ? ${field.name}.hashCode() : 0);
        </#list>
        return result;
    }

    @Override
    public java.lang.String toString() {
        java.lang.StringBuilder sb = new java.lang.StringBuilder("${className}(");
        <#list fields as field>
        sb.append("${field.name}:");
        sb.append(this.${field.name});
        <#sep>
        sb.append(",");
        </#sep>
        </#list>
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void read(io.craft.core.thrift.TProtocol protocol) throws io.craft.core.thrift.TException {
        io.craft.core.thrift.TField field;
        protocol.readStructBegin();
        while (true) {
            field = protocol.readFieldBegin();
            if (field.type == io.craft.core.thrift.TType.STOP) {
                break;
            }
            switch (field.sequence) {
            <#list fields as field>
                case ${field.sequence}: // ${field.name} ${field.type.getName()}
                    if (field.type == io.craft.core.thrift.TType.${field.type.getName()}) {
                    <@format blank=4*6>
                    <@read field "this."+field.name />
                    </@format>
                    } else {
                        io.craft.core.thrift.TProtocolUtil.skip(protocol, field.type);
                    }
                    break;
            </#list>
                default:
                    io.craft.core.thrift.TProtocolUtil.skip(protocol, field.type);
            }
            protocol.readFieldEnd();
        }
        protocol.readStructEnd();
        validate();
    }

    protected void validate() throws io.craft.core.thrift.TException {
        // check for required fields, which can't be checked in the validate method
        <#list fields as field>
        <#if field.required?? && field.required>
        if (this.${field.name} == null) {
            throw new io.craft.core.thrift.TException("Required field '${field.name}' was not found in serialized data!");
        }
        </#if>
        </#list>
    }

    @Override
    public void write(io.craft.core.thrift.TProtocol protocol) throws io.craft.core.thrift.TException {
        validate();
        protocol.writeStructBegin(STRUCT_DESC);
        <#list fields as field>
        if (this.${field.name} != null) {
            protocol.writeFieldBegin(${field.name?upper_case}_FIELD_DESC);
            <@format blank=4*3>
            <@write field "this."+field.name />
            </@format>
            protocol.writeFieldEnd();
        }
        </#list>
        protocol.writeFieldStop();
        protocol.writeStructEnd();
    }

}
</#macro>