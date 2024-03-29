package io.craft.core.constant;

public class Constants {

    public final static int DEFAULT_MAX_FRAME_LENGTH = 16384000;//16m

    public final static int DEFAULT_BYTEBUF_SIZE = 1024;

    public final static int BUSINESS_THREAD_KEEP_ALIVE = 5;//MINUTES

    public final static String APPLICATION_NAMESPACE = "application.namespace";

    public final static String APPLICATION_NAME = "application.name";

    public final static String APPLICATION_PORT = "application.port";

    public final static String PROPERTIES_PATH = "/properties/";

    public final static String HOSTS_PATH = "/hosts/";

    public final static short SERVICE_NAME_SEQUENCE = 1;

    public final static short TRACE_ID_SEQUENCE = 2;

    public final static short COOKIE_SEQUENCE = 3;

    public final static short SEQUENCE_START_OFFSET = 10;//保留前10个sequence以备他用

    public final static int INT_BYTE_LENGTH = 4;

    public final static String ROOT_LOG_LEVEL_CONFIG_KEY = "log.level.root";

}
