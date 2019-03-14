package io.craft.abc;

public interface UserService {

    io.craft.abc.model.UserModel get(java.lang.Long id) throws io.craft.core.thrift.TException;

    void ping() throws io.craft.core.thrift.TException;

    io.craft.abc.model.UserModel gets(java.util.List<java.util.List<java.util.List<java.lang.Long>>> ids) throws io.craft.core.thrift.TException;


    class get_args extends io.craft.core.thrift.TArgs {
    
        private static final io.craft.core.thrift.TStruct STRUCT_DESC = new io.craft.core.thrift.TStruct("get_args");
    
        private static final io.craft.core.thrift.TField ID_FIELD_DESC = new io.craft.core.thrift.TField("id", io.craft.core.thrift.TType.LONG, (short)10);
    
        private java.lang.Long id;
    
        public get_args() {
        }
    
        public get_args(
                java.lang.Long id
            )
            {
            this();
            this.id = id;
        }
    
        public java.lang.Long getId() {
            return this.id;
        }
    
        public get_args setId(java.lang.Long id) {
            this.id = id;
            return this;
        }
    
    
        @Override
        public boolean equals(java.lang.Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            get_args obj = (get_args) o;
            return true && java.util.Objects.equals(id, obj.id);
        }
    
        @Override
        public int hashCode() {
            int result = 0;
            result = 31 * result + (id != null ? id.hashCode() : 0);
            return result;
        }
    
        @Override
        public java.lang.String toString() {
            java.lang.StringBuilder sb = new java.lang.StringBuilder("get_args(");
            sb.append("id:");
            sb.append(this.id);
            sb.append(")");
            return sb.toString();
        }
    
        @Override
        protected void readInternal(io.craft.core.thrift.TProtocol protocol, io.craft.core.thrift.TField field) throws io.craft.core.thrift.TException {
            switch (field.sequence) {
                case 10: // id LONG
                    if (field.type == io.craft.core.thrift.TType.LONG) {
                        {
                            this.id = protocol.readLong();
                        }
                    } else {
                        io.craft.core.thrift.TProtocolUtil.skip(protocol, field.type);
                    }
                    break;
                default:
                    io.craft.core.thrift.TProtocolUtil.skip(protocol, field.type);
            }
        }
    
        @Override
        protected void validateInternal() throws io.craft.core.thrift.TException {
            // check for required fields, which can't be checked in the validate method
        }
    
        @Override
        protected void writeInternal(io.craft.core.thrift.TProtocol protocol) throws io.craft.core.thrift.TException {
            if (this.id != null) {
                protocol.writeFieldBegin(ID_FIELD_DESC);
                    if (this.id != null) {
                        {
                            protocol.writeLong(this.id);
                        }
                    }
                protocol.writeFieldEnd();
            }
        }
    
    }
    
    class get_result implements io.craft.core.thrift.TSerializable {
    
        private static final io.craft.core.thrift.TStruct STRUCT_DESC = new io.craft.core.thrift.TStruct("get_result");
    
        private static final io.craft.core.thrift.TField SUCCESS_FIELD_DESC = new io.craft.core.thrift.TField("success", io.craft.core.thrift.TType.STRUCT, (short)0);
    
        private io.craft.abc.model.UserModel success;
    
        public get_result() {
        }
    
        public get_result(
                io.craft.abc.model.UserModel success
            )
            {
            this();
            this.success = success;
        }
    
        public io.craft.abc.model.UserModel getSuccess() {
            return this.success;
        }
    
        public get_result setSuccess(io.craft.abc.model.UserModel success) {
            this.success = success;
            return this;
        }
    
    
        @Override
        public boolean equals(java.lang.Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            get_result obj = (get_result) o;
            return true && java.util.Objects.equals(success, obj.success);
        }
    
        @Override
        public int hashCode() {
            int result = 0;
            result = 31 * result + (success != null ? success.hashCode() : 0);
            return result;
        }
    
        @Override
        public java.lang.String toString() {
            java.lang.StringBuilder sb = new java.lang.StringBuilder("get_result(");
            sb.append("success:");
            sb.append(this.success);
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
                    case 0: // success STRUCT
                        if (field.type == io.craft.core.thrift.TType.STRUCT) {
                            {
                                this.success = new io.craft.abc.model.UserModel();
                                this.success.read(protocol);
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
            if (this.success != null) {
                protocol.writeFieldBegin(SUCCESS_FIELD_DESC);
                if (this.success != null) {
                    {
                        this.success.write(protocol);
                    }
                }
                protocol.writeFieldEnd();
            }
            protocol.writeFieldStop();
            protocol.writeStructEnd();
        }
    
    }
    class ping_args extends io.craft.core.thrift.TArgs {
    
        private static final io.craft.core.thrift.TStruct STRUCT_DESC = new io.craft.core.thrift.TStruct("ping_args");
    
    
    
        public ping_args() {
        }
    
    
    
        @Override
        public boolean equals(java.lang.Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ping_args obj = (ping_args) o;
            return true;
        }
    
        @Override
        public int hashCode() {
            int result = 0;
            return result;
        }
    
        @Override
        public java.lang.String toString() {
            java.lang.StringBuilder sb = new java.lang.StringBuilder("ping_args(");
            sb.append(")");
            return sb.toString();
        }
    
        @Override
        protected void readInternal(io.craft.core.thrift.TProtocol protocol, io.craft.core.thrift.TField field) throws io.craft.core.thrift.TException {
        }
    
        @Override
        protected void validateInternal() throws io.craft.core.thrift.TException {
            // check for required fields, which can't be checked in the validate method
        }
    
        @Override
        protected void writeInternal(io.craft.core.thrift.TProtocol protocol) throws io.craft.core.thrift.TException {
        }
    
    }
    
    class ping_result implements io.craft.core.thrift.TSerializable {
    
        private static final io.craft.core.thrift.TStruct STRUCT_DESC = new io.craft.core.thrift.TStruct("ping_result");
    
    
    
        public ping_result() {
        }
    
    
    
        @Override
        public boolean equals(java.lang.Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ping_result obj = (ping_result) o;
            return true;
        }
    
        @Override
        public int hashCode() {
            int result = 0;
            return result;
        }
    
        @Override
        public java.lang.String toString() {
            java.lang.StringBuilder sb = new java.lang.StringBuilder("ping_result(");
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
            protocol.writeFieldStop();
            protocol.writeStructEnd();
        }
    
    }
    class gets_args extends io.craft.core.thrift.TArgs {
    
        private static final io.craft.core.thrift.TStruct STRUCT_DESC = new io.craft.core.thrift.TStruct("gets_args");
    
        private static final io.craft.core.thrift.TField IDS_FIELD_DESC = new io.craft.core.thrift.TField("ids", io.craft.core.thrift.TType.LIST, (short)10);
    
        private java.util.List<java.util.List<java.util.List<java.lang.Long>>> ids;
    
        public gets_args() {
        }
    
        public gets_args(
                java.util.List<java.util.List<java.util.List<java.lang.Long>>> ids
            )
            {
            this();
            this.ids = ids;
        }
    
        public java.util.List<java.util.List<java.util.List<java.lang.Long>>> getIds() {
            return this.ids;
        }
    
        public gets_args setIds(java.util.List<java.util.List<java.util.List<java.lang.Long>>> ids) {
            this.ids = ids;
            return this;
        }
    
    
        @Override
        public boolean equals(java.lang.Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            gets_args obj = (gets_args) o;
            return true && java.util.Objects.equals(ids, obj.ids);
        }
    
        @Override
        public int hashCode() {
            int result = 0;
            result = 31 * result + (ids != null ? ids.hashCode() : 0);
            return result;
        }
    
        @Override
        public java.lang.String toString() {
            java.lang.StringBuilder sb = new java.lang.StringBuilder("gets_args(");
            sb.append("ids:");
            sb.append(this.ids);
            sb.append(")");
            return sb.toString();
        }
    
        @Override
        protected void readInternal(io.craft.core.thrift.TProtocol protocol, io.craft.core.thrift.TField field) throws io.craft.core.thrift.TException {
            switch (field.sequence) {
                case 10: // ids LIST
                    if (field.type == io.craft.core.thrift.TType.LIST) {
                        {
                            io.craft.core.thrift.TList list0 = protocol.readListBegin();
                            this.ids = new java.util.ArrayList<java.util.List<java.util.List<java.lang.Long>>>(list0.size);
                            java.util.List<java.util.List<java.lang.Long>> val0;
                            for (int i0 = 0; i0 < list0.size; ++i0) {
                            {
                                io.craft.core.thrift.TList list1 = protocol.readListBegin();
                                val0 = new java.util.ArrayList<java.util.List<java.lang.Long>>(list1.size);
                                java.util.List<java.lang.Long> val1;
                                for (int i1 = 0; i1 < list1.size; ++i1) {
                                    {
                                        io.craft.core.thrift.TList list2 = protocol.readListBegin();
                                        val1 = new java.util.ArrayList<java.lang.Long>(list2.size);
                                        java.lang.Long val2;
                                        for (int i2 = 0; i2 < list2.size; ++i2) {
                                                {
                                                    val2 = protocol.readLong();
                                                }
                                            val1.add(val2);
                                        }
                                        protocol.readListEnd();
                                    }
                                    val0.add(val1);
                                }
                                protocol.readListEnd();
                            }
                                this.ids.add(val0);
                            }
                            protocol.readListEnd();
                        }
                    } else {
                        io.craft.core.thrift.TProtocolUtil.skip(protocol, field.type);
                    }
                    break;
                default:
                    io.craft.core.thrift.TProtocolUtil.skip(protocol, field.type);
            }
        }
    
        @Override
        protected void validateInternal() throws io.craft.core.thrift.TException {
            // check for required fields, which can't be checked in the validate method
        }
    
        @Override
        protected void writeInternal(io.craft.core.thrift.TProtocol protocol) throws io.craft.core.thrift.TException {
            if (this.ids != null) {
                protocol.writeFieldBegin(IDS_FIELD_DESC);
                    if (this.ids != null) {
                        {
                            protocol.writeListBegin(new io.craft.core.thrift.TList(io.craft.core.thrift.TType.LIST, this.ids.size()));
                            for (java.util.List<java.util.List<java.lang.Long>> val0 : this.ids) {
                        if (val0 != null) {
                            {
                                protocol.writeListBegin(new io.craft.core.thrift.TList(io.craft.core.thrift.TType.LIST, val0.size()));
                                for (java.util.List<java.lang.Long> val1 : val0) {
                                if (val1 != null) {
                                    {
                                        protocol.writeListBegin(new io.craft.core.thrift.TList(io.craft.core.thrift.TType.LONG, val1.size()));
                                        for (java.lang.Long val2 : val1) {
                                            if (val2 != null) {
                                                {
                                                    protocol.writeLong(val2);
                                                }
                                            }
                                        }
                                        protocol.writeListEnd();
                                    }
                                }
                                }
                                protocol.writeListEnd();
                            }
                        }
                            }
                            protocol.writeListEnd();
                        }
                    }
                protocol.writeFieldEnd();
            }
        }
    
    }
    
    class gets_result implements io.craft.core.thrift.TSerializable {
    
        private static final io.craft.core.thrift.TStruct STRUCT_DESC = new io.craft.core.thrift.TStruct("gets_result");
    
        private static final io.craft.core.thrift.TField SUCCESS_FIELD_DESC = new io.craft.core.thrift.TField("success", io.craft.core.thrift.TType.STRUCT, (short)0);
    
        private io.craft.abc.model.UserModel success;
    
        public gets_result() {
        }
    
        public gets_result(
                io.craft.abc.model.UserModel success
            )
            {
            this();
            this.success = success;
        }
    
        public io.craft.abc.model.UserModel getSuccess() {
            return this.success;
        }
    
        public gets_result setSuccess(io.craft.abc.model.UserModel success) {
            this.success = success;
            return this;
        }
    
    
        @Override
        public boolean equals(java.lang.Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            gets_result obj = (gets_result) o;
            return true && java.util.Objects.equals(success, obj.success);
        }
    
        @Override
        public int hashCode() {
            int result = 0;
            result = 31 * result + (success != null ? success.hashCode() : 0);
            return result;
        }
    
        @Override
        public java.lang.String toString() {
            java.lang.StringBuilder sb = new java.lang.StringBuilder("gets_result(");
            sb.append("success:");
            sb.append(this.success);
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
                    case 0: // success STRUCT
                        if (field.type == io.craft.core.thrift.TType.STRUCT) {
                            {
                                this.success = new io.craft.abc.model.UserModel();
                                this.success.read(protocol);
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
            if (this.success != null) {
                protocol.writeFieldBegin(SUCCESS_FIELD_DESC);
                if (this.success != null) {
                    {
                        this.success.write(protocol);
                    }
                }
                protocol.writeFieldEnd();
            }
            protocol.writeFieldStop();
            protocol.writeStructEnd();
        }
    
    }

    class Processor<I extends UserService> extends io.craft.core.thrift.TProcessor<I> {

        private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Processor.class);

        public Processor(I iface) {
            super(getProcessMap(iface, new java.util.HashMap<java.lang.String, io.craft.core.thrift.TFunction<I, ? extends io.craft.core.thrift.TArgs, ? extends io.craft.core.thrift.TSerializable>>()));
        }

        private static <I extends UserService> java.util.Map<java.lang.String, io.craft.core.thrift.TFunction<I, ? extends io.craft.core.thrift.TArgs, ? extends io.craft.core.thrift.TSerializable>> getProcessMap(I iface, java.util.Map<java.lang.String, io.craft.core.thrift.TFunction<I, ? extends io.craft.core.thrift.TArgs, ? extends io.craft.core.thrift.TSerializable>> processMap) {
            processMap.put("get", new get(iface));
            processMap.put("ping", new ping(iface));
            processMap.put("gets", new gets(iface));
            return processMap;
        }

        public static class get<I extends UserService> extends io.craft.core.thrift.TFunction<I, get_args, get_result> {

            public get(I iface) {
                super(iface, "get");
            }

            public get_args getEmptyArgsInstance() {
                return new get_args();
            }

            public get_result getResult(get_args args) throws io.craft.core.thrift.TException {
                get_result result = new get_result();
                result.success = iface.get(args.id);
                return result;
            }
        }

        public static class ping<I extends UserService> extends io.craft.core.thrift.TFunction<I, ping_args, ping_result> {

            public ping(I iface) {
                super(iface, "ping");
            }

            public ping_args getEmptyArgsInstance() {
                return new ping_args();
            }

            public ping_result getResult(ping_args args) throws io.craft.core.thrift.TException {
                ping_result result = new ping_result();
                iface.ping();
                return result;
            }
        }

        public static class gets<I extends UserService> extends io.craft.core.thrift.TFunction<I, gets_args, gets_result> {

            public gets(I iface) {
                super(iface, "gets");
            }

            public gets_args getEmptyArgsInstance() {
                return new gets_args();
            }

            public gets_result getResult(gets_args args) throws io.craft.core.thrift.TException {
                gets_result result = new gets_result();
                result.success = iface.gets(args.ids);
                return result;
            }
        }

    }

    class Client extends io.craft.core.client.CraftClient implements UserService {

        public static final String SERVICE_NAME = "io.craft.abc";

        public Client() {
            super(SERVICE_NAME);
        }

        public io.craft.abc.model.UserModel get(java.lang.Long id) throws io.craft.core.thrift.TException {
            io.netty.util.concurrent.Future<io.craft.core.message.CraftMessage> future = send_get(id);
            return recv_get(future);
        }

        private io.netty.util.concurrent.Future<io.craft.core.message.CraftMessage> send_get(java.lang.Long id) throws io.craft.core.thrift.TException  {
            get_args args = new get_args();
            args.setServiceName(SERVICE_NAME);
            String traceId = io.craft.core.util.TraceUtil.getTraceId();
            if (traceId != null) {
                args.setTraceId(traceId);
            } else {
                args.setTraceId(io.craft.core.util.TraceUtil.generateTraceId());
            }
            args.setCookie(io.craft.core.util.TraceUtil.getCookie());
            args.setId(id);
            return sendBase("get", args);
        }

        private io.craft.abc.model.UserModel recv_get(io.netty.util.concurrent.Future<io.craft.core.message.CraftMessage> future) throws io.craft.core.thrift.TException  {
            get_result result = new get_result();
            receiveBase(future, result, "get");
            if (result.success != null) {
                return result.success;
            }
            return null;
        }

        public void ping() throws io.craft.core.thrift.TException {
            io.netty.util.concurrent.Future<io.craft.core.message.CraftMessage> future = send_ping();
            recv_ping(future);
        }

        private io.netty.util.concurrent.Future<io.craft.core.message.CraftMessage> send_ping() throws io.craft.core.thrift.TException  {
            ping_args args = new ping_args();
            args.setServiceName(SERVICE_NAME);
            String traceId = io.craft.core.util.TraceUtil.getTraceId();
            if (traceId != null) {
                args.setTraceId(traceId);
            } else {
                args.setTraceId(io.craft.core.util.TraceUtil.generateTraceId());
            }
            args.setCookie(io.craft.core.util.TraceUtil.getCookie());
            return sendBase("ping", args);
        }

        private void recv_ping(io.netty.util.concurrent.Future<io.craft.core.message.CraftMessage> future) throws io.craft.core.thrift.TException  {
            ping_result result = new ping_result();
            receiveBase(future, result, "ping");
        }

        public io.craft.abc.model.UserModel gets(java.util.List<java.util.List<java.util.List<java.lang.Long>>> ids) throws io.craft.core.thrift.TException {
            io.netty.util.concurrent.Future<io.craft.core.message.CraftMessage> future = send_gets(ids);
            return recv_gets(future);
        }

        private io.netty.util.concurrent.Future<io.craft.core.message.CraftMessage> send_gets(java.util.List<java.util.List<java.util.List<java.lang.Long>>> ids) throws io.craft.core.thrift.TException  {
            gets_args args = new gets_args();
            args.setServiceName(SERVICE_NAME);
            String traceId = io.craft.core.util.TraceUtil.getTraceId();
            if (traceId != null) {
                args.setTraceId(traceId);
            } else {
                args.setTraceId(io.craft.core.util.TraceUtil.generateTraceId());
            }
            args.setCookie(io.craft.core.util.TraceUtil.getCookie());
            args.setIds(ids);
            return sendBase("gets", args);
        }

        private io.craft.abc.model.UserModel recv_gets(io.netty.util.concurrent.Future<io.craft.core.message.CraftMessage> future) throws io.craft.core.thrift.TException  {
            gets_result result = new gets_result();
            receiveBase(future, result, "gets");
            if (result.success != null) {
                return result.success;
            }
            return null;
        }

    }
}