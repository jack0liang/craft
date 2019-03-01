package io.craft.proxy.constant;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public class Constants {

    public static final String SERVER_ATTRIBUTE_CLIENT_KEY = "server-attribute-client";

    public static final AttributeKey<Channel> SERVER_ATTRIBUTE_CLIENT = AttributeKey.newInstance(SERVER_ATTRIBUTE_CLIENT_KEY);

}
