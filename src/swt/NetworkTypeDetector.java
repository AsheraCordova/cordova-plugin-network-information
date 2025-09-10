package org.apache.cordova.networkinformation;
import java.net.*;
import java.util.*;

public class NetworkTypeDetector {

    public enum NetworkType {
        wifi,
        ethernet,
        mobile,
        unknown
    }

    /**
     * Get active network interfaces and classify them.
     */
    public static Map<NetworkInterface, NetworkType> getActiveNetworkTypes() {
        Map<NetworkInterface, NetworkType> result = new HashMap<>();

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface netIf = interfaces.nextElement();

                if (!netIf.isUp() || netIf.isLoopback()) {
                    continue; // ignore inactive and loopback
                }

                NetworkType type = detectType(netIf);
                result.put(netIf, type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Detect type based on interface name/display name.
     */
    private static NetworkType detectType(NetworkInterface netIf) {
        String name = netIf.getName().toLowerCase();
        String displayName = netIf.getDisplayName().toLowerCase();

        if (name.contains("wlan") || name.contains("wifi") || displayName.contains("wi-fi")) {
            return NetworkType.wifi;
        } else if (name.contains("eth") || displayName.contains("ethernet") || name.startsWith("en")) {
            return NetworkType.ethernet;
        } else if (name.contains("ppp") || name.contains("wwan") ||
                   displayName.contains("mobile") || displayName.contains("cellular") ||
                   displayName.contains("broadband")) {
            return NetworkType.mobile;
        }

        return NetworkType.unknown;
    }

    /**
     * Quick utility: return first active type (if any).
     */
    public static NetworkType getPrimaryNetworkType() {
        Map<NetworkInterface, NetworkType> active = getActiveNetworkTypes();
        if (!active.isEmpty()) {
            return active.values().iterator().next();
        }
        return NetworkType.unknown;
    }

    // Demo
    public static void main(String[] args) {
        Map<NetworkInterface, NetworkType> activeTypes = getActiveNetworkTypes();
        for (Map.Entry<NetworkInterface, NetworkType> entry : activeTypes.entrySet()) {
            NetworkInterface netIf = entry.getKey();
            System.out.println("Interface: " + netIf.getDisplayName() + " -> " + entry.getValue());

            Enumeration<InetAddress> addresses = netIf.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                System.out.println("   IP: " + addr.getHostAddress());
            }
        }

        System.out.println("Primary Network Type: " + getPrimaryNetworkType());
    }
}
