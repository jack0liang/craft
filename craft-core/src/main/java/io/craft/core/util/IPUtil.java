package io.craft.core.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPUtil {

    private static InetAddress localAddress;

    public static final String getLocalIPV4() throws UnknownHostException {
        String ip;

        if (localAddress == null) {
            localAddress = InetAddress.getLocalHost();
        }

        ip = localAddress.getHostAddress();

        return ip;
    }
}
