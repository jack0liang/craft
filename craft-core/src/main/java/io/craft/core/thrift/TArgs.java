package io.craft.core.thrift;

import java.util.HashMap;
import java.util.Map;

import static io.craft.core.constant.Constants.*;

public abstract class TArgs implements TSerializable {

    private static final TField SERVICENAME_DESC = new TField("name", TType.STRING, SERVICE_NAME_SEQUENCE);
    private static final TField TRACEID_DESC = new TField("traceId", TType.STRING, TRACE_ID_SEQUENCE);
    private static final TField COOKIE_DESC = new TField("cookie", TType.MAP, COOKIE_SEQUENCE);

    private String serviceName;

    private String traceId;

    private Map<String, String> cookie;

    public final String getServiceName() {
        return serviceName;
    }

    public final String getTraceId() {
        return traceId;
    }

    public final Map<String, String> getCookie() {
        return cookie;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public void setCookie(Map<String, String> cookie) {
        this.cookie = cookie;
    }

    @Override
    public void read(TProtocol protocol) throws TException {
        TField field;
        protocol.readStructBegin();
        while(true) {
            field = protocol.readFieldBegin();
            if (field.type == TType.STOP) {
                break;
            }
            if (field.sequence == SERVICE_NAME_SEQUENCE) {
                if (field.type == TType.STRING) {
                    this.serviceName = protocol.readString();
                } else {
                    TProtocolUtil.skip(protocol, TType.STRING);
                }
            } else if (field.sequence == TRACE_ID_SEQUENCE) {
                if (field.type == TType.STRING) {
                    this.traceId = protocol.readString();
                } else {
                    TProtocolUtil.skip(protocol, TType.STRING);
                }
            } else if (field.sequence == COOKIE_SEQUENCE) {
                if (field.type == TType.MAP) {
                    {
                        TMap map = protocol.readMapBegin();
                        this.cookie = new HashMap<>(2 * map.size);
                        String key;
                        String val;
                        for (int i = 0; i < map.size; ++i) {
                            key = protocol.readString();
                            val = protocol.readString();
                            this.cookie.put(key, val);
                        }
                        protocol.readMapEnd();
                    }
                } else {
                    TProtocolUtil.skip(protocol, TType.MAP);
                }
            } else {
                readInternal(protocol, field);
            }
            protocol.readFieldEnd();
        }
        protocol.readStructEnd();

        this.validate();
    }

    private void validate() throws TException {
        if (this.serviceName == null) {
            throw new TException("Required field 'name' was not found in serialized data!");
        }
        if (this.traceId == null) {
            throw new TException("Required field 'traceId' was not found in serialized data!");
        }
        validateInternal();
    }

    abstract protected void readInternal(TProtocol protocol, TField field) throws TException;

    abstract protected void validateInternal() throws TException;

    abstract protected void writeInternal(TProtocol protocol) throws TException;

    @Override
    public void write(TProtocol protocol) throws TException {

        this.validate();
        //二进制协议下不需要结构体头
        protocol.writeStructBegin(null);

        protocol.writeFieldBegin(SERVICENAME_DESC);
        protocol.writeString(this.serviceName);
        protocol.writeFieldEnd();

        protocol.writeFieldBegin(TRACEID_DESC);
        protocol.writeString(this.traceId);
        protocol.writeFieldEnd();

        if (this.cookie != null) {
            protocol.writeFieldBegin(COOKIE_DESC);
            {
                protocol.writeMapBegin(new TMap(TType.STRING, TType.STRING, this.cookie.size()));
                for (Map.Entry<String, String> entry : this.cookie.entrySet()) {
                    protocol.writeString(entry.getKey());
                    protocol.writeString(entry.getValue());
                }
                protocol.writeMapEnd();
            }
        }
        protocol.writeFieldEnd();

        writeInternal(protocol);

        protocol.writeFieldStop();
        protocol.writeStructEnd();
    }
}
