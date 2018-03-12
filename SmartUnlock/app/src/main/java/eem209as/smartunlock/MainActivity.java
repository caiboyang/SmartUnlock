package eem209as.smartunlock;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationDelegate {

    private TextView mTextMessage;
    private TextView displayText;
    private Button refreshBtn;
    private boolean mPermissionReady;

    private SensorManager sm;
    private LocationManager lm = null;
    private LocationListener myLocationListener = null;

    private float ax = 0;
    private float ay = 0;
    private float az = 0;
    private double g = 0.0;
    private double lat = 0.0;
    private double lng = 0.0;
    private float acu = 0;
    private double alt = 0.0;
    private float speed = 0;
    private String provider = "";

    static final String LOG_TAG = MainActivity.class.getCanonicalName();


//    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
//            = new BottomNavigationView.OnNavigationItemSelectedListener() {
//
//        @Override
//        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//            switch (item.getItemId()) {
//                case R.id.navigation_home:
//                    mTextMessage.setText(R.string.title_home);
//                    return true;
//                case R.id.navigation_dashboard:
//                    mTextMessage.setText(R.string.title_dashboard);
//                    return true;
//                case R.id.navigation_notifications:
//                    mTextMessage.setText(R.string.title_notifications);
//                    return true;
//            }
//            return false;
//        }
//    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * permission check
         */
        int coarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int fineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        mPermissionReady = coarseLocationPermission == PackageManager.PERMISSION_GRANTED
                && fineLocationPermission == PackageManager.PERMISSION_GRANTED;

        if (!mPermissionReady) {
            requestPermission();
        }

//        mTextMessage = (TextView) findViewById(R.id.message);
        displayText = findViewById(R.id.text_display);
        refreshBtn = findViewById(R.id.refresh_button);
//        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
//        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        myLocationListener = new MyLocationListener(this);

        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

        lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if(mPermissionReady){
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
        }
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnSetTextView();
            }
        });
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, 11);
    }

    @SuppressLint("MissingPermission")
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Map<String, Integer> perm = new HashMap<>();
        perm.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_DENIED);
        perm.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_DENIED);
        for (int i = 0; i < permissions.length; i++) {
            perm.put(permissions[i], grantResults[i]);
        }
        if (perm.get(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && perm.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.i(LOG_TAG, "permission granted");
            mPermissionReady = true;
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
        }
        else{
            new AlertDialog.Builder(this)
                    .setMessage(R.string.permission_warning)
                    .setPositiveButton(R.string.dismiss, null)
                    .show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void btnSetTextView() {
        StringBuilder dis = new StringBuilder("You just Refreshed!!!\n");
        dis.append("ax is: ").append(ax).append("\n");
        dis.append("ay is: ").append(ay).append("\n");
        dis.append("az is: ").append(az).append("\n");
        dis.append("g is: ").append(g).append("\n");
        dis.append("latitude is: ").append(lat).append("\n");
        dis.append("longitude is: ").append(lng).append("\n");
        dis.append("alt is: ").append(alt).append("\n");
        dis.append("acu is: ").append(acu).append("\n");
        dis.append("speed is: ").append(speed).append("\n");
        dis.append("provider is: ").append(provider).append("\n");
        displayText.setText(dis);

    }

    public void setTextView() {
        StringBuilder dis = new StringBuilder();
        dis.append("ax is: ").append(ax).append("\n");
        dis.append("ay is: ").append(ay).append("\n");
        dis.append("az is: ").append(az).append("\n");
        dis.append("g is: ").append(g).append("\n");
        dis.append("latitude is: ").append(lat).append("\n");
        dis.append("longitude is: ").append(lng).append("\n");
        dis.append("alt is: ").append(alt).append("\n");
        dis.append("acu is: ").append(acu).append("\n");
        dis.append("speed is: ").append(speed).append("\n");
        dis.append("provider is: ").append(provider).append("\n");
        displayText.setText(dis);
    }

    @Override
    protected void onPause() {
//        sm.unregisterListener(this);
//        lm.removeUpdates(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
//        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
//        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
//        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        sm.unregisterListener(this);
        lm.removeUpdates(myLocationListener);
        super.onDestroy();
    }

    public void onSensorChanged(SensorEvent event) {
        if (Sensor.TYPE_ACCELEROMETER == event.sensor.getType()) {

            float[] values = event.values;
            this.ax = values[0];
            this.ay = values[1];
            this.az = values[2];

            this.g = Math.sqrt(ax * ax + ay * ay + az * az);
        }

    }

    public void onAccuracyChanged(Sensor arg0, int arg1) {

    }

    @Override
    public void returnLocation(Location location) {
//         获取纬度
            lat = location.getLatitude();
            // 获取经度
            lng = location.getLongitude();
            // 位置提供者
            provider = location.getProvider();
            // 位置的准确性
            acu = location.getAccuracy();
            // 高度信息
            alt = location.getAltitude();
            // 方向角
//            float bearing = location.getBearing();
            // 速度 米/秒
            speed = location.getSpeed();
            setTextView();
    }

}
