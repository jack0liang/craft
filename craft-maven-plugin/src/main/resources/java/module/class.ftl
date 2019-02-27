<#macro class className fields static>
public <#if static?? && static>static </#if>class ${className} implements org.apache.thrift.TBase<${className}, ${className}._Fields>, java.io.Serializable, Cloneable, Comparable<${className}> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("${className}");

    <#list fields as field>
    private static final org.apache.thrift.protocol.TField ${field.name?upper_case}_FIELD_DESC = new org.apache.thrift.protocol.TField("${field.name}", org.apache.thrift.protocol.TType.${field.type.getName()}, (short)${field.sequence});
    </#list>

    private static final _Fields[] REQUIRES = {<#list fields as field><#if field.required?? && field.required>_Fields.${field.name?upper_case}<#sep>, </#sep></#if></#list>};

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new ${className}StandardSchemeFactory();

    <#list fields as field>
    private ${field.fullClassName} ${field.name};
    </#list>

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        <#list fields as field>
        ${field.name?upper_case}((short)${field.sequence}, "${field.name}"),

        </#list>
        ;

        private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

        static {
            for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                <#list fields as field>
                case ${field.sequence}: // ${field.name}
                    return ${field.name?upper_case};
                </#list>
                default:
                    return null;
            }
        }

        /**
        * Find the _Fields constant that matches fieldId, throwing an exception
        * if it is not found.
        */
        public static _Fields findByThriftIdOrThrow(int fieldId) {
            _Fields fields = findByThriftId(fieldId);
            if (fields == null) throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");
            return fields;
        }

        /**
        * Find the _Fields constant that matches name, or null if its not found.
        */
        public static _Fields findByName(java.lang.String name) {
            return byName.get(name);
        }

        private final short _thriftId;
        private final java.lang.String _fieldName;

        _Fields(short thriftId, java.lang.String fieldName) {
            _thriftId = thriftId;
            _fieldName = fieldName;
        }

        public short getThriftFieldId() {
            return _thriftId;
        }

        public java.lang.String getFieldName() {
            return _fieldName;
        }
    }


    <#--// isset id assignments-->
    <#--<#list attributes as attribute>-->
    <#--private static final int __${attribute.upperCaseName?replace("_", "")}_ISSET_ID = 0;-->
    <#--</#list>-->
    <#--private byte __isset_bitfield = 0;-->
    public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
    static {
        java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        <#list fields as field>
        tmpMap.put(
            _Fields.${field.name?upper_case},
            new org.apache.thrift.meta_data.FieldMetaData(
                "${field.name}",
                org.apache.thrift.TFieldRequirementType.<#if !field.required??>DEFAULT<#elseif field.required == true>REQUIRED<#else>OPTIONAL</#if>,
                <#if field.type.getName() == "SET">
                new org.apache.thrift.meta_data.SetMetaData(
                    org.apache.thrift.protocol.TType.${field.type.getName()},
                    new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.${field.genericTypes[0].type.getName()})
                )
                <#elseif field.type.getName() == "LIST">
                new org.apache.thrift.meta_data.ListMetaData(
                    org.apache.thrift.protocol.TType.${field.type.getName()},
                    new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.${field.genericTypes[0].type.getName()})
                )
                <#elseif field.type.getName() == "MAP">
                new org.apache.thrift.meta_data.MapMetaData(
                    org.apache.thrift.protocol.TType.${field.type.getName()},
                    new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.${field.genericTypes[0].type.getName()}),
                    new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.${field.genericTypes[1].type.getName()})
                )
                <#else>
                new org.apache.thrift.meta_data.FieldValueMetaData(
                    org.apache.thrift.protocol.TType.${field.type.getName()}
                )
                </#if>
            )
        );
        </#list>
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(${className}.class, metaDataMap);
    }

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

    /**
    * Performs a deep copy on <i>other</i>.
    */
    public ${className}(${className} other) {
        <#list fields as field>
        this.${field.name} = other.${field.name};
        </#list>
    }

    public ${className} deepCopy() {
        return new ${className}(this);
    }

    @Override
    public void clear() {
        <#list fields as field>
        this.${field.name} = null;
        </#list>
    }

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

    public void setFieldValue(_Fields field, java.lang.Object value) {
        switch (field) {
            <#list fields as field>
            case ${field.name?upper_case}:
                set${field.name?cap_first}((${field.fullClassName})value);
                break;
            </#list>
        }
    }

    public java.lang.Object getFieldValue(_Fields field) {
        switch (field) {
            <#list fields as field>
            case ${field.name?upper_case}:
                return get${field.name?cap_first}();
            </#list>

        }
        throw new java.lang.IllegalStateException();
    }

    /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new java.lang.IllegalArgumentException();
        }

        switch (field) {
            <#list fields as field>
            case ${field.name?upper_case}:
                return ${field.name} != null;
            </#list>
        }
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof ${className})
            return this.equals((${className})that);
        return false;
    }

    public boolean equals(${className} that) {
        if (that == null)
            return false;
        if (this == that)
            return true;

        <#list fields as field>
        if (${field.name} != null ? !${field.name}.equals(that.${field.name}) : that.${field.name} != null) return false;
        </#list>
        return true;
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
    public int compareTo(${className} other) {
        return Integer.valueOf(hashCode()).compareTo(Integer.valueOf(other.hashCode()));
    }

    public _Fields fieldForId(int fieldId) {
        return _Fields.findByThriftId(fieldId);
    }

    public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
        STANDARD_SCHEME_FACTORY.getScheme().read(iprot, this);
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
        STANDARD_SCHEME_FACTORY.getScheme().write(oprot, this);
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

    public void validate() throws org.apache.thrift.TException {
        //do nth...
    }

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        try {
            write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
        try {
            // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class ${className}StandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
        public ${className}StandardScheme getScheme() {
            return new ${className}StandardScheme();
        }
    }

    private static class ${className}StandardScheme extends org.apache.thrift.scheme.StandardScheme<${className}> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, ${className} struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TField schemeField;
            iprot.readStructBegin();
            while (true) {
                schemeField = iprot.readFieldBegin();
                if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                    break;
                }
                switch (schemeField.id) {
                    <#list fields as field>
                    case ${field.sequence}: // ${field.name} ${field.type.getName()}
                    <#if field.type.getName() == "LIST">
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list = iprot.readListBegin();
                                struct.${field.name} = new java.util.ArrayList<${field.genericTypes[0].fullClassName}>(_list.size);
                                ${field.genericTypes[0].fullClassName} _elem;
                                for (int _i = 0; _i < _list.size; ++_i)
                                {
                                    _elem = iprot.read${field.genericTypes[0].type.getName()?lower_case?cap_first}();
                                    struct.${field.name}.add(_elem);
                                }
                                iprot.readListEnd();
                            }
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                    <#elseif field.type.getName() == "SET">
                        if (schemeField.type == org.apache.thrift.protocol.TType.SET) {
                            {
                                org.apache.thrift.protocol.TSet _set = iprot.readSetBegin();
                                struct.${field.name} = new java.util.HashSet<${field.genericTypes[0].fullClassName}>(_set.size);
                                ${field.genericTypes[0].fullClassName} _elem;
                                for (int _i = 0; _i < _set.size; ++_i)
                                {
                                    _elem = iprot.read${field.genericTypes[0].type.getName()?lower_case?cap_first}();
                                    struct.${field.name}.add(_elem);
                                }
                                iprot.readSetEnd();
                            }
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                    <#elseif field.type.getName() == "MAP">
                        if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
                            {
                                org.apache.thrift.protocol.TMap _map = iprot.readMapBegin();
                                struct.${field.name} = new java.util.HashMap<${field.genericTypes[0].fullClassName},${field.genericTypes[1].fullClassName}>(2 * _map.size);
                                ${field.genericTypes[0].fullClassName} _key;
                                ${field.genericTypes[1].fullClassName} _val;
                                for (int _i = 0; _i < _map.size; ++_i)
                                {
                                    _key = iprot.read${field.genericTypes[0].type.getName()?lower_case?cap_first}();
                                    _val = iprot.read${field.genericTypes[1].type.getName()?lower_case?cap_first}();
                                    struct.${field.name}.put(_key, _val);
                                }
                                iprot.readMapEnd();
                            }
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                    <#elseif field.type.getName() == "STRUCT">
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                            struct.${field.name} = new ${field.fullClassName}();
                            struct.${field.name}.read(iprot);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                    <#elseif field.type.getCode() == "DATE">
                        if (schemeField.type == org.apache.thrift.protocol.TType.${field.type.getName()}) {
                            struct.${field.name} = new java.util.Date(iprot.read${field.type.getName()?lower_case?cap_first}());
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                    <#else>
                        if (schemeField.type == org.apache.thrift.protocol.TType.${field.type.getName()}) {
                            struct.${field.name} = iprot.read${field.type.getName()?lower_case?cap_first}();
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                    </#if>
                        break;
                    </#list>
                    default:
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                }
                iprot.readFieldEnd();
            }
            iprot.readStructEnd();
            // check for required fields of primitive type, which can't be checked in the validate method
            <#list fields as field>
            <#if field.required?? && field.required>
            if (struct.${field.name} == null) {
                throw new org.apache.thrift.protocol.TProtocolException("Required field '${field.name}' was not found in serialized data! Struct: " + toString());
            }
            </#if>
            </#list>
            struct.validate();
        }

        public void write(org.apache.thrift.protocol.TProtocol oprot, ${className} struct) throws org.apache.thrift.TException {
            // check for required fields of primitive type, which can't be checked in the validate method
            <#list fields as field>
            <#if field.required?? && field.required>
            if (struct.${field.name} == null) {
                throw new org.apache.thrift.protocol.TProtocolException("Required field '${field.name}' was not found in struct! Struct: " + struct.toString());
            }
            </#if>
            </#list>
            struct.validate();

            oprot.writeStructBegin(STRUCT_DESC);
            <#list fields as field>
            if (struct.${field.name} != null) {
                oprot.writeFieldBegin(${field.name?upper_case}_FIELD_DESC);
                <#if field.type.getName() == "SET">
                {
                    oprot.writeSetBegin(new org.apache.thrift.protocol.TSet(org.apache.thrift.protocol.TType.${field.genericTypes[0].type.getName()}, struct.${field.name}.size()));
                    for (${field.genericTypes[0].fullClassName} _iter11 : struct.${field.name})
                    {
                        oprot.write${field.genericTypes[0].type.getName()?lower_case?cap_first}(_iter11);
                    }
                    oprot.writeSetEnd();
                }
                <#elseif field.type.getName() == "LIST">
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.${field.genericTypes[0].type.getName()}, struct.${field.name}.size()));
                    for (${field.genericTypes[0].fullClassName} _iter10 : struct.${field.name})
                    {
                        oprot.write${field.genericTypes[0].type.getName()?lower_case?cap_first}(_iter10);
                    }
                    oprot.writeListEnd();
                }
                <#elseif field.type.getName() == "MAP">
                {
                    oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.${field.genericTypes[0].type.getName()}, org.apache.thrift.protocol.TType.${field.genericTypes[1].type.getName()}, struct.${field.name}.size()));
                    for (java.util.Map.Entry<${field.genericTypes[0].fullClassName}, ${field.genericTypes[1].fullClassName}> _iter12 : struct.${field.name}.entrySet())
                    {
                        oprot.write${field.genericTypes[0].type.getName()?lower_case?cap_first}(_iter12.getKey());
                        oprot.write${field.genericTypes[1].type.getName()?lower_case?cap_first}(_iter12.getValue());
                    }
                    oprot.writeMapEnd();
                }
                <#elseif field.type.getName() == "STRUCT">
                struct.${field.name}.write(oprot);
                <#elseif field.type.getCode() == "DATE">
                oprot.write${field.type.getName()?lower_case?cap_first}(struct.${field.name}.getTime());
                <#else>
                oprot.write${field.type.getName()?lower_case?cap_first}(struct.${field.name});
                </#if>
                oprot.writeFieldEnd();
            }
            </#list>
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }

    }

}
</#macro>