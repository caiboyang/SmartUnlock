package eem209as.smartunlock;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by boyang on 3/11/18.
 */

public class WifiUtils {
    public static WifiInfo getWifiInfo(Context mContext){
        WifiManager mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = mWifiManager.getConnectionInfo();
        return info;
    }

    public static Map<String, String> getDetailsWifiInfo(Context mContext){
        Map<String, String> sInfo = new HashMap<>();
//        StringBuilder sInfo = new StringBuilder();
        WifiInfo mWifiInfo = getWifiInfo(mContext);

        int Ip = mWifiInfo.getIpAddress();
        String strIp = "" + (Ip & 0xFF) + "." + ((Ip >> 8) & 0xFF) + "." + ((Ip >> 16) & 0xFF) + "." + ((Ip >> 24) & 0xFF);

        sInfo.put("BSSID", mWifiInfo.getBSSID());
        sInfo.put("SSID", mWifiInfo.getSSID());
        sInfo.put("RSSI", String.valueOf(mWifiInfo.getRssi()));
//        sInfo.append("\n--SSID: ").append(mWifiInfo.getSSID());
//        sInfo.append("\n--myIpAddress : ").append(strIp);
//        sInfo.append("\n--MacAddress : ").append(mWifiInfo.getMacAddress());
//        sInfo.append("\n--NetworkId : ").append(mWifiInfo.getNetworkId());
//        sInfo.append("\n--LinkSpeed : ").append(mWifiInfo.getLinkSpeed()).append("Mbps");
//        sInfo.append("\n--Rssi(signal level in dB): ").append(mWifiInfo.getRssi());
//        sInfo.append("\n--SupplicantState : ").append(mWifiInfo.getSupplicantState());

        return sInfo;
    }

    public static String getAroundWifiDeciceInfo(Context mContext){
        StringBuffer sInfo = new StringBuffer();
        WifiManager mWifiManager = (WifiManager)mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
        List<ScanResult> scanResults=mWifiManager.getScanResults();//搜索到的设备列表
        for (ScanResult scanResult : scanResults) {
            sInfo.append("\n设备名：").append(scanResult.SSID).append(" 信号强度：").append(scanResult.level)
                    .append("/n :").append(mWifiManager.calculateSignalLevel(scanResult.level, 4));
        }
        return sInfo.toString();
    }

}
