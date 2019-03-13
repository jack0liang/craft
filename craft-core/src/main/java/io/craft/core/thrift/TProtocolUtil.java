package io.craft.core.thrift;

public class TProtocolUtil {

    public static void skip(TProtocol protocol, TType type) throws TException {
        skip(protocol, type, Integer.MAX_VALUE);
    }

    public static void skip(TProtocol protocol, TType type, int maxDepth) throws TException {
        if (maxDepth <= 0) {
            throw new TException("Maximum skip depth exceeded");
        }
        switch (type) {
            case BOOL:
                protocol.readBool();
                break;

            case BYTE:
                protocol.readByte();
                break;

            case SHORT:
                protocol.readShort();
                break;

            case INT:
                protocol.readInt();
                break;

            case LONG:
                protocol.readLong();
                break;

            case DOUBLE:
                protocol.readDouble();
                break;

            case STRING:
                protocol.readString();
                break;

            case STRUCT:
                protocol.readStructBegin();
                while (true) {
                    TField field = protocol.readFieldBegin();
                    if (TType.STOP.equals(field.type)) {
                        break;
                    }
                    skip(protocol, field.type, maxDepth - 1);
                    protocol.readFieldEnd();
                }
                protocol.readStructEnd();
                break;

            case MAP:
                TMap map = protocol.readMapBegin();
                for (int i = 0; i < map.size; i++) {
                    skip(protocol, map.keyType, maxDepth - 1);
                    skip(protocol, map.valueType, maxDepth - 1);
                }
                protocol.readMapEnd();
                break;

            case SET:
                TSet set = protocol.readSetBegin();
                for (int i = 0; i < set.size; i++) {
                    skip(protocol, set.type, maxDepth - 1);
                }
                protocol.readSetEnd();
                break;

            case LIST:
                TList list = protocol.readListBegin();
                for (int i = 0; i < list.size; i++) {
                    skip(protocol, list.type, maxDepth - 1);
                }
                protocol.readListEnd();
                break;

            default:
                throw new TException("Unrecognized type " + type);
        }
    }
}
