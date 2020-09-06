package com.sjimtv.colordetector;

import android.util.Log;

import com.illposed.osc.OSCPortOut;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

public class OSCSendThread extends Thread {

    private OSCPortOut oscSender;
    private String broadcast_ip;
    private int send_port;

    OSCSendThread(int send_port) {
        broadcast_ip = makeBroadcast(getIPAddress(true));
        this.send_port = send_port;

    }


    @Override
    public void run() {

        try {
            // Connect to some IP address and port
            oscSender = new OSCPortOut(InetAddress.getByName(broadcast_ip), send_port);
        } catch (UnknownHostException e) {
            // Error handling when your IP isn't found
            Log.d("OSC", "Fout IP.");

        } catch (Exception e) {
            // Error handling for any other errors
            Log.d("OSC", "Andere fout.");
        }
    }


    OSCPortOut getSender() {
        return oscSender;
    }

    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        } // for now eat exceptions
        return "";
    }

    public String makeBroadcast(String ip){
        String[] broadcast_ip_list = ip.split("\\.");
        return broadcast_ip_list[0] + '.' + broadcast_ip_list[1] + '.' + broadcast_ip_list[2] + '.' + "255";
    }
}