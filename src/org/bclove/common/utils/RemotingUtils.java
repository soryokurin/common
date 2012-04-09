
package org.bclove.common.utils;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;




public class RemotingUtils {
    static final Log log = LogFactory.getLog(RemotingUtils.class);


    public static String getLocalAddress() throws Exception {
        // 遍历网卡，查找一个非回路ip地址并返回
        final Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
        InetAddress ipv6Address = null;
        while (enumeration.hasMoreElements()) {
            final NetworkInterface networkInterface = enumeration.nextElement();
            final Enumeration<InetAddress> en = networkInterface.getInetAddresses();
            while (en.hasMoreElements()) {
                final InetAddress address = en.nextElement();
                if (!address.isLoopbackAddress()) {
                    if (address instanceof Inet6Address) {
                        ipv6Address = address;
                    }
                    else {
                        // 优先使用ipv4
                        return normalizeHostAddress(address);
                    }
                }

            }

        }
        // 没有ipv4，再使用ipv6
        if (ipv6Address != null) {
            return normalizeHostAddress(ipv6Address);
        }
        final InetAddress localHost = InetAddress.getLocalHost();
        return normalizeHostAddress(localHost);
    }


    public static String normalizeHostAddress(final InetAddress localHost) {
        if (localHost instanceof Inet6Address) {
            return "[" + localHost.getHostAddress() + "]";
        }
        else {
            return localHost.getHostAddress();
        }
    }
    
    public static void main(String[] args){
    	try {
			System.out.println(RemotingUtils.getLocalAddress());
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

}