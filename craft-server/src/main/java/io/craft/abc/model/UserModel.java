package io.craft.abc.model;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Craft IDL Compiler (1.0.0)", date = "2019-3-4 19:59:49")
public class UserModel implements org.apache.thrift.TBase<UserModel, UserModel._Fields>, java.io.Serializable, Cloneable, Comparable<UserModel> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("UserModel");

    private static final org.apache.thrift.protocol.TField ID_FIELD_DESC = new org.apache.thrift.protocol.TField("id", org.apache.thrift.protocol.TType.I64, (short)0);
    private static final org.apache.thrift.protocol.TField NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("name", org.apache.thrift.protocol.TType.STRING, (short)1);
    private static final org.apache.thrift.protocol.TField LISTS_FIELD_DESC = new org.apache.thrift.protocol.TField("lists", org.apache.thrift.protocol.TType.LIST, (short)2);
    private static final org.apache.thrift.protocol.TField SETS_FIELD_DESC = new org.apache.thrift.protocol.TField("sets", org.apache.thrift.protocol.TType.SET, (short)3);
    private static final org.apache.thrift.protocol.TField MAPS_FIELD_DESC = new org.apache.thrift.protocol.TField("maps", org.apache.thrift.protocol.TType.MAP, (short)4);
    private static final org.apache.thrift.protocol.TField JOINDATE_FIELD_DESC = new org.apache.thrift.protocol.TField("joinDate", org.apache.thrift.protocol.TType.I64, (short)5);

    private static final _Fields[] REQUIRES = {};

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new UserModelStandardSchemeFactory();

    private java.lang.Long id;
    private java.lang.String name;
    private java.util.List<java.lang.String> lists;
    private java.util.Set<java.lang.Integer> sets;
    private java.util.Map<java.lang.Double, java.lang.Short> maps;
    private java.util.Date joinDate;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        ID((short)0, "id"),

        NAME((short)1, "name"),

        LISTS((short)2, "lists"),

        SETS((short)3, "sets"),

        MAPS((short)4, "maps"),

        JOINDATE((short)5, "joinDate"),

        ;

        private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

        static {
            for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 0: // id
                    return ID;
                case 1: // name
                    return NAME;
                case 2: // lists
                    return LISTS;
                case 3: // sets
                    return SETS;
                case 4: // maps
                    return MAPS;
                case 5: // joinDate
                    return JOINDATE;
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


    public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
    static {
        java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(
            _Fields.ID,
            new org.apache.thrift.meta_data.FieldMetaData(
                "id",
                org.apache.thrift.TFieldRequirementType.OPTIONAL,
                new org.apache.thrift.meta_data.FieldValueMetaData(
                    org.apache.thrift.protocol.TType.I64
                )
            )
        );
        tmpMap.put(
            _Fields.NAME,
            new org.apache.thrift.meta_data.FieldMetaData(
                "name",
                org.apache.thrift.TFieldRequirementType.OPTIONAL,
                new org.apache.thrift.meta_data.FieldValueMetaData(
                    org.apache.thrift.protocol.TType.STRING
                )
            )
        );
        tmpMap.put(
            _Fields.LISTS,
            new org.apache.thrift.meta_data.FieldMetaData(
                "lists",
                org.apache.thrift.TFieldRequirementType.OPTIONAL,
                new org.apache.thrift.meta_data.ListMetaData(
                    org.apache.thrift.protocol.TType.LIST,
                    new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)
                )
            )
        );
        tmpMap.put(
            _Fields.SETS,
            new org.apache.thrift.meta_data.FieldMetaData(
                "sets",
                org.apache.thrift.TFieldRequirementType.OPTIONAL,
                new org.apache.thrift.meta_data.SetMetaData(
                    org.apache.thrift.protocol.TType.SET,
                    new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)
                )
            )
        );
        tmpMap.put(
            _Fields.MAPS,
            new org.apache.thrift.meta_data.FieldMetaData(
                "maps",
                org.apache.thrift.TFieldRequirementType.OPTIONAL,
                new org.apache.thrift.meta_data.MapMetaData(
                    org.apache.thrift.protocol.TType.MAP,
                    new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE),
                    new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I16)
                )
            )
        );
        tmpMap.put(
            _Fields.JOINDATE,
            new org.apache.thrift.meta_data.FieldMetaData(
                "joinDate",
                org.apache.thrift.TFieldRequirementType.OPTIONAL,
                new org.apache.thrift.meta_data.FieldValueMetaData(
                    org.apache.thrift.protocol.TType.I64
                )
            )
        );
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(UserModel.class, metaDataMap);
    }

    public UserModel() {
    }

    public UserModel(
            java.lang.Long id, 
            java.lang.String name, 
            java.util.List<java.lang.String> lists, 
            java.util.Set<java.lang.Integer> sets, 
            java.util.Map<java.lang.Double, java.lang.Short> maps, 
            java.util.Date joinDate
    )
    {
        this();
        this.id = id;
        this.name = name;
        this.lists = lists;
        this.sets = sets;
        this.maps = maps;
        this.joinDate = joinDate;
    }

    /**
    * Performs a deep copy on <i>other</i>.
    */
    public UserModel(UserModel other) {
        this.id = other.id;
        this.name = other.name;
        this.lists = other.lists;
        this.sets = other.sets;
        this.maps = other.maps;
        this.joinDate = other.joinDate;
    }

    public UserModel deepCopy() {
        return new UserModel(this);
    }

    @Override
    public void clear() {
        this.id = null;
        this.name = null;
        this.lists = null;
        this.sets = null;
        this.maps = null;
        this.joinDate = null;
    }

    public java.lang.Long getId() {
        return this.id;
    }

    public UserModel setId(java.lang.Long id) {
        this.id = id;
        return this;
    }

    public java.lang.String getName() {
        return this.name;
    }

    public UserModel setName(java.lang.String name) {
        this.name = name;
        return this;
    }

    public java.util.List<java.lang.String> getLists() {
        return this.lists;
    }

    public UserModel setLists(java.util.List<java.lang.String> lists) {
        this.lists = lists;
        return this;
    }

    public java.util.Set<java.lang.Integer> getSets() {
        return this.sets;
    }

    public UserModel setSets(java.util.Set<java.lang.Integer> sets) {
        this.sets = sets;
        return this;
    }

    public java.util.Map<java.lang.Double, java.lang.Short> getMaps() {
        return this.maps;
    }

    public UserModel setMaps(java.util.Map<java.lang.Double, java.lang.Short> maps) {
        this.maps = maps;
        return this;
    }

    public java.util.Date getJoinDate() {
        return this.joinDate;
    }

    public UserModel setJoinDate(java.util.Date joinDate) {
        this.joinDate = joinDate;
        return this;
    }


    public void setFieldValue(_Fields field, java.lang.Object value) {
        switch (field) {
            case ID:
                setId((java.lang.Long)value);
                break;
            case NAME:
                setName((java.lang.String)value);
                break;
            case LISTS:
                setLists((java.util.List<java.lang.String>)value);
                break;
            case SETS:
                setSets((java.util.Set<java.lang.Integer>)value);
                break;
            case MAPS:
                setMaps((java.util.Map<java.lang.Double, java.lang.Short>)value);
                break;
            case JOINDATE:
                setJoinDate((java.util.Date)value);
                break;
        }
    }

    public java.lang.Object getFieldValue(_Fields field) {
        switch (field) {
            case ID:
                return getId();
            case NAME:
                return getName();
            case LISTS:
                return getLists();
            case SETS:
                return getSets();
            case MAPS:
                return getMaps();
            case JOINDATE:
                return getJoinDate();

        }
        throw new java.lang.IllegalStateException();
    }

    /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new java.lang.IllegalArgumentException();
        }

        switch (field) {
            case ID:
                return id != null;
            case NAME:
                return name != null;
            case LISTS:
                return lists != null;
            case SETS:
                return sets != null;
            case MAPS:
                return maps != null;
            case JOINDATE:
                return joinDate != null;
        }
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof UserModel)
            return this.equals((UserModel)that);
        return false;
    }

    public boolean equals(UserModel that) {
        if (that == null)
            return false;
        if (this == that)
            return true;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (lists != null ? !lists.equals(that.lists) : that.lists != null) return false;
        if (sets != null ? !sets.equals(that.sets) : that.sets != null) return false;
        if (maps != null ? !maps.equals(that.maps) : that.maps != null) return false;
        if (joinDate != null ? !joinDate.equals(that.joinDate) : that.joinDate != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (lists != null ? lists.hashCode() : 0);
        result = 31 * result + (sets != null ? sets.hashCode() : 0);
        result = 31 * result + (maps != null ? maps.hashCode() : 0);
        result = 31 * result + (joinDate != null ? joinDate.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(UserModel other) {
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
        java.lang.StringBuilder sb = new java.lang.StringBuilder("UserModel(");
        sb.append("id:");
        sb.append(this.id);
        sb.append(",");
        sb.append("name:");
        sb.append(this.name);
        sb.append(",");
        sb.append("lists:");
        sb.append(this.lists);
        sb.append(",");
        sb.append("sets:");
        sb.append(this.sets);
        sb.append(",");
        sb.append("maps:");
        sb.append(this.maps);
        sb.append(",");
        sb.append("joinDate:");
        sb.append(this.joinDate);
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

    private static class UserModelStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
        public UserModelStandardScheme getScheme() {
            return new UserModelStandardScheme();
        }
    }

    private static class UserModelStandardScheme extends org.apache.thrift.scheme.StandardScheme<UserModel> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, UserModel struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TField schemeField;
            iprot.readStructBegin();
            while (true) {
                schemeField = iprot.readFieldBegin();
                if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                    break;
                }
                switch (schemeField.id) {
                    case 0: // id I64
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.id = iprot.readI64();
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 1: // name STRING
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.name = iprot.readString();
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2: // lists LIST
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList list = iprot.readListBegin();
                                struct.lists = new java.util.ArrayList<java.lang.String>(list.size);
                                java.lang.String val;
                                for (int i = 0; i < list.size; ++i)
                                {
                                    val = iprot.readString();
                                    struct.lists.add(val);
                                }
                                iprot.readListEnd();
                            }
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3: // sets SET
                        if (schemeField.type == org.apache.thrift.protocol.TType.SET) {
                            {
                                org.apache.thrift.protocol.TSet set = iprot.readSetBegin();
                                struct.sets = new java.util.HashSet<java.lang.Integer>(set.size);
                                java.lang.Integer val;
                                for (int i = 0; i < set.size; ++i)
                                {
                                    val = iprot.readI32();
                                    struct.sets.add(val);
                                }
                                iprot.readSetEnd();
                            }
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 4: // maps MAP
                        if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
                            {
                                org.apache.thrift.protocol.TMap map = iprot.readMapBegin();
                                struct.maps = new java.util.HashMap<java.lang.Double,java.lang.Short>(2 * map.size);
                                java.lang.Double key;
                                java.lang.Short val;
                                for (int i = 0; i < map.size; ++i)
                                {
                                    key = iprot.readDouble();
                                    val = iprot.readI16();
                                    struct.maps.put(key, val);
                                }
                                iprot.readMapEnd();
                            }
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 5: // joinDate I64
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.joinDate = new java.util.Date(iprot.readI64());
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    default:
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                }
                iprot.readFieldEnd();
            }
            iprot.readStructEnd();
            // check for required fields of primitive type, which can't be checked in the validate method
            struct.validate();
        }

        public void write(org.apache.thrift.protocol.TProtocol oprot, UserModel struct) throws org.apache.thrift.TException {
            // check for required fields of primitive type, which can't be checked in the validate method
            struct.validate();

            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.id != null) {
                oprot.writeFieldBegin(ID_FIELD_DESC);
                oprot.writeI64(struct.id);
                oprot.writeFieldEnd();
            }
            if (struct.name != null) {
                oprot.writeFieldBegin(NAME_FIELD_DESC);
                oprot.writeString(struct.name);
                oprot.writeFieldEnd();
            }
            if (struct.lists != null) {
                oprot.writeFieldBegin(LISTS_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.lists.size()));
                    for (java.lang.String val : struct.lists)
                    {
                        oprot.writeString(val);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.sets != null) {
                oprot.writeFieldBegin(SETS_FIELD_DESC);
                {
                    oprot.writeSetBegin(new org.apache.thrift.protocol.TSet(org.apache.thrift.protocol.TType.I32, struct.sets.size()));
                    for (java.lang.Integer val : struct.sets)
                    {
                        oprot.writeI32(val);
                    }
                    oprot.writeSetEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.maps != null) {
                oprot.writeFieldBegin(MAPS_FIELD_DESC);
                {
                    oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.DOUBLE, org.apache.thrift.protocol.TType.I16, struct.maps.size()));
                    for (java.util.Map.Entry<java.lang.Double, java.lang.Short> entry : struct.maps.entrySet())
                    {
                        oprot.writeDouble(entry.getKey());
                        oprot.writeI16(entry.getValue());
                    }
                    oprot.writeMapEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.joinDate != null) {
                oprot.writeFieldBegin(JOINDATE_FIELD_DESC);
                oprot.writeI64(struct.joinDate.getTime());
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }

    }

}
