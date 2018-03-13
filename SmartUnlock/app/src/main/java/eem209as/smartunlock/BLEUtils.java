package eem209as.smartunlock;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.Set;

/**
 * Created by boyang on 3/11/18.
 */

public class BLEUtils {

    public static String getDeviceList(Context context){
        StringBuilder res = new StringBuilder();
        BluetoothAdapter mBlurAdapter= BluetoothAdapter.getDefaultAdapter();
        BluetoothManager mBlurManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        List<BluetoothDevice> connectedDevices = mBlurManager.getConnectedDevices(BluetoothProfile.GATT);
        Set<BluetoothDevice> pairedDevices = mBlurAdapter.getBondedDevices();
        if (pairedDevices.isEmpty()) {
            Log.e("DeviceActivity ", "Paired Device not founds");
            return "";
        }
        else if(connectedDevices.isEmpty()){
            Log.e("DeviceActivity ", "Connected Device not founds");
            return "";
        }

        for (BluetoothDevice devices : connectedDevices) {
            res.append("Device: address: ").append(devices.getAddress());
//            Log.d("DeviceActivity", "Device : address : " + devices.getAddress() + " name :"
//                    + devices.getName());
        }

//        for (BluetoothDevice devices : pairedDevices) {
//            Log.d("DeviceActivity", "Device : address : " + devices.getAddress() + " name :"
//                    + devices.getName());
//        }
        return res.toString();
    }
}
