package io.craft.core.util;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class IPUtil {

    /**
     * 根据网卡名获取IP V4
     * @param device 网卡名
     * @return IP
     */
    public static final String getIPV4(String device) {
        String ip = null;
        try {
            NetworkInterface network = NetworkInterface.getByName(device);
            if (network != null) {
                Enumeration<InetAddress> addresses = network.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address) {
                        ip = address.getHostAddress();
                    }
                }
            }
        } catch (IOException e) {
            //do nth;
        }
        return ip;
    }
}
