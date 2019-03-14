package io.craft.abc.model;

public class UserModel implements io.craft.core.thrift.TSerializable {

    private static final io.craft.core.thrift.TStruct STRUCT_DESC = new io.craft.core.thrift.TStruct("UserModel");

    private static final io.craft.core.thrift.TField ID_FIELD_DESC = new io.craft.core.thrift.TField("id", io.craft.core.thrift.TType.LONG, (short)0);
    private static final io.craft.core.thrift.TField NAME_FIELD_DESC = new io.craft.core.thrift.TField("name", io.craft.core.thrift.TType.STRING, (short)1);
    private static final io.craft.core.thrift.TField LISTS_FIELD_DESC = new io.craft.core.thrift.TField("lists", io.craft.core.thrift.TType.LIST, (short)2);
    private static final io.craft.core.thrift.TField SETS_FIELD_DESC = new io.craft.core.thrift.TField("sets", io.craft.core.thrift.TType.SET, (short)3);
    private static final io.craft.core.thrift.TField MAPS_FIELD_DESC = new io.craft.core.thrift.TField("maps", io.craft.core.thrift.TType.MAP, (short)4);
    private static final io.craft.core.thrift.TField JOINDATE_FIELD_DESC = new io.craft.core.thrift.TField("joinDate", io.craft.core.thrift.TType.LONG, (short)5);
    private static final io.craft.core.thrift.TField USERTYPE_FIELD_DESC = new io.craft.core.thrift.TField("userType", io.craft.core.thrift.TType.INT, (short)6);
    private static final io.craft.core.thrift.TField DEC_FIELD_DESC = new io.craft.core.thrift.TField("dec", io.craft.core.thrift.TType.STRING, (short)7);

    private java.lang.Long id;
    private java.lang.String name;
    private java.util.List<java.lang.String> lists;
    private java.util.Set<java.lang.Integer> sets;
    private java.util.Map<java.lang.String, java.lang.String> maps;
    private java.util.Date joinDate;
    private io.craft.abc.constant.UserType userType;
    private java.math.BigDecimal dec;

    public UserModel() {
    }

    public UserModel(
            java.lang.Long id, 
            java.lang.String name, 
            java.util.List<java.lang.String> lists, 
            java.util.Set<java.lang.Integer> sets, 
            java.util.Map<java.lang.String, java.lang.String> maps, 
            java.util.Date joinDate, 
            io.craft.abc.constant.UserType userType, 
            java.math.BigDecimal dec
        )
        {
        this();
        this.id = id;
        this.name = name;
        this.lists = lists;
        this.sets = sets;
        this.maps = maps;
        this.joinDate = joinDate;
        this.userType = userType;
        this.dec = dec;
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

    public java.util.Map<java.lang.String, java.lang.String> getMaps() {
        return this.maps;
    }

    public UserModel setMaps(java.util.Map<java.lang.String, java.lang.String> maps) {
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

    public io.craft.abc.constant.UserType getUserType() {
        return this.userType;
    }

    public UserModel setUserType(io.craft.abc.constant.UserType userType) {
        this.userType = userType;
        return this;
    }

    public java.math.BigDecimal getDec() {
        return this.dec;
    }

    public UserModel setDec(java.math.BigDecimal dec) {
        this.dec = dec;
        return this;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserModel obj = (UserModel) o;
        return true && java.util.Objects.equals(id, obj.id) && java.util.Objects.equals(name, obj.name) && java.util.Objects.equals(lists, obj.lists) && java.util.Objects.equals(sets, obj.sets) && java.util.Objects.equals(maps, obj.maps) && java.util.Objects.equals(joinDate, obj.joinDate) && java.util.Objects.equals(userType, obj.userType) && java.util.Objects.equals(dec, obj.dec);
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
        result = 31 * result + (userType != null ? userType.hashCode() : 0);
        result = 31 * result + (dec != null ? dec.hashCode() : 0);
        return result;
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
        sb.append(",");
        sb.append("userType:");
        sb.append(this.userType);
        sb.append(",");
        sb.append("dec:");
        sb.append(this.dec);
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
                case 0: // id LONG
                    if (field.type == io.craft.core.thrift.TType.LONG) {
                        {
                            this.id = protocol.readLong();
                        }
                    } else {
                        io.craft.core.thrift.TProtocolUtil.skip(protocol, field.type);
                    }
                    break;
                case 1: // name STRING
                    if (field.type == io.craft.core.thrift.TType.STRING) {
                        {
                            this.name = protocol.readString();
                        }
                    } else {
                        io.craft.core.thrift.TProtocolUtil.skip(protocol, field.type);
                    }
                    break;
                case 2: // lists LIST
                    if (field.type == io.craft.core.thrift.TType.LIST) {
                        {
                            io.craft.core.thrift.TList list0 = protocol.readListBegin();
                            this.lists = new java.util.ArrayList<java.lang.String>(list0.size);
                            java.lang.String val0;
                            for (int i0 = 0; i0 < list0.size; ++i0) {
                            {
                                val0 = protocol.readString();
                            }
                                this.lists.add(val0);
                            }
                            protocol.readListEnd();
                        }
                    } else {
                        io.craft.core.thrift.TProtocolUtil.skip(protocol, field.type);
                    }
                    break;
                case 3: // sets SET
                    if (field.type == io.craft.core.thrift.TType.SET) {
                        {
                            io.craft.core.thrift.TSet set0 = protocol.readSetBegin();
                            this.sets = new java.util.HashSet<java.lang.Integer>(set0.size);
                            java.lang.Integer val0;
                            for (int i0 = 0; i0 < set0.size; ++i0) {
                            {
                                val0 = protocol.readInt();
                            }
                                this.sets.add(val0);
                            }
                            protocol.readSetEnd();
                        }
                    } else {
                        io.craft.core.thrift.TProtocolUtil.skip(protocol, field.type);
                    }
                    break;
                case 4: // maps MAP
                    if (field.type == io.craft.core.thrift.TType.MAP) {
                        {
                            io.craft.core.thrift.TMap map0 = protocol.readMapBegin();
                            this.maps = new java.util.HashMap<java.lang.String,java.lang.String>(2 * map0.size);
                            java.lang.String key0;
                            java.lang.String val0;
                            for (int i0 = 0; i0 < map0.size; ++i0) {
                            {
                                key0 = protocol.readString();
                            }
                            {
                                val0 = protocol.readString();
                            }
                                this.maps.put(key0, val0);
                            }
                        }
                    } else {
                        io.craft.core.thrift.TProtocolUtil.skip(protocol, field.type);
                    }
                    break;
                case 5: // joinDate LONG
                    if (field.type == io.craft.core.thrift.TType.LONG) {
                        {
                            this.joinDate = new java.util.Date(protocol.readLong());
                        }
                    } else {
                        io.craft.core.thrift.TProtocolUtil.skip(protocol, field.type);
                    }
                    break;
                case 6: // userType INT
                    if (field.type == io.craft.core.thrift.TType.INT) {
                        {
                            this.userType = io.craft.abc.constant.UserType.findByValue(protocol.readInt());
                        }
                    } else {
                        io.craft.core.thrift.TProtocolUtil.skip(protocol, field.type);
                    }
                    break;
                case 7: // dec STRING
                    if (field.type == io.craft.core.thrift.TType.STRING) {
                        {
                            this.dec = new java.math.BigDecimal(protocol.readString());
                        }
                    } else {
                        io.craft.core.thrift.TProtocolUtil.skip(protocol, field.type);
                    }
                    break;
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
    }

    @Override
    public void write(io.craft.core.thrift.TProtocol protocol) throws io.craft.core.thrift.TException {
        validate();
        protocol.writeStructBegin(STRUCT_DESC);
        if (this.id != null) {
            protocol.writeFieldBegin(ID_FIELD_DESC);
            if (this.id != null) {
                {
                    protocol.writeLong(this.id);
                }
            }
            protocol.writeFieldEnd();
        }
        if (this.name != null) {
            protocol.writeFieldBegin(NAME_FIELD_DESC);
            if (this.name != null) {
                {
                    protocol.writeString(this.name);
                }
            }
            protocol.writeFieldEnd();
        }
        if (this.lists != null) {
            protocol.writeFieldBegin(LISTS_FIELD_DESC);
            if (this.lists != null) {
                {
                    protocol.writeListBegin(new io.craft.core.thrift.TList(io.craft.core.thrift.TType.STRING, this.lists.size()));
                    for (java.lang.String val0 : this.lists) {
                if (val0 != null) {
                    {
                        protocol.writeString(val0);
                    }
                }
                    }
                    protocol.writeListEnd();
                }
            }
            protocol.writeFieldEnd();
        }
        if (this.sets != null) {
            protocol.writeFieldBegin(SETS_FIELD_DESC);
            if (this.sets != null) {
                {
                    protocol.writeSetBegin(new io.craft.core.thrift.TSet(io.craft.core.thrift.TType.INT, this.sets.size()));
                    for (java.lang.Integer val0 : this.sets) {
                if (val0 != null) {
                    {
                        protocol.writeInt(val0);
                    }
                }
                    }
                    protocol.writeSetEnd();
                }
            }
            protocol.writeFieldEnd();
        }
        if (this.maps != null) {
            protocol.writeFieldBegin(MAPS_FIELD_DESC);
            if (this.maps != null) {
                {
                    protocol.writeMapBegin(new io.craft.core.thrift.TMap(io.craft.core.thrift.TType.STRING, io.craft.core.thrift.TType.STRING, this.maps.size()));
                    for (java.util.Map.Entry<java.lang.String, java.lang.String> entry0 : this.maps.entrySet()) {
                if (entry0.getKey() != null) {
                    {
                        protocol.writeString(entry0.getKey());
                    }
                }
                if (entry0.getValue() != null) {
                    {
                        protocol.writeString(entry0.getValue());
                    }
                }
                    }
                    protocol.writeMapEnd();
                }
            }
            protocol.writeFieldEnd();
        }
        if (this.joinDate != null) {
            protocol.writeFieldBegin(JOINDATE_FIELD_DESC);
            if (this.joinDate != null) {
                {
                    protocol.writeLong(this.joinDate.getTime());
                }
            }
            protocol.writeFieldEnd();
        }
        if (this.userType != null) {
            protocol.writeFieldBegin(USERTYPE_FIELD_DESC);
            if (this.userType != null) {
                {
                    protocol.writeInt(this.userType.getValue());
                }
            }
            protocol.writeFieldEnd();
        }
        if (this.dec != null) {
            protocol.writeFieldBegin(DEC_FIELD_DESC);
            if (this.dec != null) {
                {
                    protocol.writeString(this.dec.toString());
                }
            }
            protocol.writeFieldEnd();
        }
        protocol.writeFieldStop();
        protocol.writeStructEnd();
    }

}
