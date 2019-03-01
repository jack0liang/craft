package io.craft.proxy.proxy;

import io.craft.core.message.CraftFramedMessage;
import io.netty.channel.Channel;

public interface Proxy {

    void accept(Channel client, CraftFramedMessage message);

}
