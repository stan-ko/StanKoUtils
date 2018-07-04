package com.stanko.tools;

import android.text.TextUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

/**
 * (c)
 * <p/>
 * Authors:
 * Stan Koshutsky<Stan.Koshutsky@gmail.com>
 */

public class NetUtils {

    /**
     * Returns MAC address of any found interface name in query eth0, wlan0
     * @return  mac address or empty string
     */
    public static String getMACAddress() {
        final String ethMAC = NetUtils.getMACAddress("eth0");
        if (!TextUtils.isEmpty(ethMAC))
            return ethMAC;

        final String wlanMAC = NetUtils.getMACAddress("wlan0");
        if (!TextUtils.isEmpty(wlanMAC))
            return wlanMAC;

        return getMACAddress(null);
    }

    public static String getEthMACAddress() {
        final String ethMAC = NetUtils.getMACAddress("eth0");
        if (!TextUtils.isEmpty(ethMAC))
            return ethMAC;

        return getMACAddress(null);
    }

    public static String getWiFiMACAddress() {
        final String wlanMAC = NetUtils.getMACAddress("wlan0");
        if (!TextUtils.isEmpty(wlanMAC))
            return wlanMAC;

        return getMACAddress(null);
    }

    /**
     * Returns MAC address of the given interface name.
     * @param interfaceName eth0, wlan0 or NULL=use first interface
     * @return  mac address or empty string
     */
    public static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac==null) return "";
                StringBuilder buf = new StringBuilder();
                for (int idx=0; idx<mac.length; idx++)
                    buf.append(String.format("%02X:", mac[idx]));
                if (buf.length()>0) buf.deleteCharAt(buf.length()-1);
                return buf.toString();
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
        /*try {
            // this is so Linux hack
            return loadFileAsString("/sys/class/net/" +interfaceName + "/address").toUpperCase().trim();
        } catch (IOException ex) {
            return null;
        }*/
    }

    /**
     * Get IP address from first non-localhost interface
     * @param useIPv4: true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }

}