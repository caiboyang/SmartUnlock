package eem209as.smartunlock;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by boyang on 3/11/18.
 */

public class MyLocationListener implements LocationListener {
    private Location myLocation = null;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss.SSSZ");
    LocationDelegate locationDelegate = null;
    private static final String tag = "MyLocationListener";

    public MyLocationListener(LocationDelegate locationDelegate){
        this.locationDelegate = locationDelegate;
    }
    @Override
    public void onLocationChanged(Location location) {

        if (LocationUtils.isBetterLocation(location, myLocation)) {

            locationDelegate.returnLocation(location);

            String locationTime = sdf.format(new Date(location.getTime()));
            String currentTime = null;

            if (myLocation != null) {
                currentTime = sdf.format(new Date(myLocation.getTime()));
            }
            myLocation = location;

//            displayText.setText("经度：" + lng + "\n纬度：" + lat + "\n服务商：" + provider + "\n准确性：" + acu + "\n高度：" + alt + "\n方向角：" + bearing
//                    + "\n速度：" + speed + "\n上次上报时间：" + currentTime + "\n最新上报时间：" + locationTime + "\n您所在的城市：" + sb.toString());

        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i(tag, "onStatusChanged: " + provider);

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i(tag, "onProviderEnabled: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i(tag, "onProviderDisabled: " + provider);
    }
}
