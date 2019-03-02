package io.craft.core.constant;

import io.craft.core.message.CraftFramedMessage;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;

public class Constants {

    public final static int DEFAULT_MAX_FRAME_LENGTH = 16384000;//16m

    public final static int DEFAULT_BYTEBUF_SIZE = 1024;

    public final static int BUSINESS_THREAD_KEEP_ALIVE = 5;//MINUTES

    public final static String APPLICATION_NAMESPACE = "application.namespace";

    public final static String APPLICATION_NAME = "application.name";

    public final static String APPLICATION_PORT = "application.port";

    public final static String PROPERTIES_PATH = "/properties/";

    public final static String HOSTS_PATH = "/hosts/";

    public final static short SERVICE_NAME_SEQUENCE = Short.MIN_VALUE;

    public final static short TRACE_ID_SEQUENCE = (short) (Short.MIN_VALUE + 1);

    public final static short HEADER_SEQUENCE = (short) (Short.MIN_VALUE + 2);

}
