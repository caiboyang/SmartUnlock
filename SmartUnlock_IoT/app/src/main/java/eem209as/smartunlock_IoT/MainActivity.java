package eem209as.smartunlock_IoT;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationDelegate {

    private TextView mTextMessage;
    private TextView displayText;
    private Button refreshBtn;

    private boolean mPermissionReady;

    private SensorManager sm;
    private LocationManager lm = null;
    private LocationListener myLocationListener = null;

    private DataClass myData = new DataClass();
    protected String clientId;

    static final String LOG_TAG = MainActivity.class.getCanonicalName();

    AWSIoTConnect awsConnection = null;

//    // Create the Handler object (on the main thread by default)
//    Handler handler = new Handler();
//    // Define the code block to be executed
//    private Runnable runnableCode = new Runnable() {
//        @Override
//        public void run() {
//            // Do something here on the main thread
//            Log.d("Handlers", "Called on main thread");
//            setTextView();
//            Toast.makeText(getApplicationContext(), "UI refreshed!!!", Toast.LENGTH_LONG).show();
//            handler.postDelayed(this, 30000);
//        }
//    };


//    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
//            = new BottomNavigationView.OnNavigationItemSelectedListener() {
//
//        @Override
//        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//            switch (item.getItemId()) {
//                case R.id.navigation_predict:
//                    mTextMessage.setText(R.string.title_predict);
//                    return true;
//                case R.id.navigation_train:
//                    mTextMessage.setText(R.string.title_train);
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
//
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

        clientId = UUID.randomUUID().toString();
        awsConnection = new AWSIoTConnect(this);
        awsConnection.onCreateCall();

        // Run the above code block on the main thread after 2 seconds
//        handler.post(runnableCode);

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTextView();

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

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void setTextView() {
        myData.wifiInfo = WifiUtils.getDetailsWifiInfo(this);
//        timeStamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
        Date now = Calendar.getInstance().getTime();
        myData.dayStamp = new SimpleDateFormat("EEEE").format(now);
        myData.timeStamp = new SimpleDateFormat("HH:mm:ss").format(now);
        StringBuilder dis = new StringBuilder("You just Refreshed!!!\n");
        dis.append("day is: ").append(myData.dayStamp).append("\n");
        dis.append("time is: ").append(myData.timeStamp).append("\n");
        dis.append("the current situation is: ").append(myData.isSafe ? "Safe" : "Dangerous").append("\n");
        dis.append("ax is: ").append(myData.ax).append("\n");
        dis.append("ay is: ").append(myData.ay).append("\n");
        dis.append("az is: ").append(myData.az).append("\n");
        dis.append("g is: ").append(myData.g).append("\n");
        dis.append("latitude is: ").append(myData.lat).append("\n");
        dis.append("longitude is: ").append(myData.lng).append("\n");
        dis.append("altitude is: ").append(myData.alt).append("\n");
        dis.append("accuracy is: ").append(myData.acu).append("\n");
        dis.append("speed is: ").append(myData.speed).append("\n");
        dis.append("provider is: ").append(myData.provider).append("\n");
        dis.append("Wifi info: \n");
        dis.append("BSSID: ").append(myData.wifiInfo.get("BSSID")).append("\n");
        dis.append("SSID: ").append(myData.wifiInfo.get("SSID")).append("\n");
        dis.append("RSSI: ").append(myData.wifiInfo.get("RSSI")).append("\n");
//        dis.append("Wifi info: ").append(WifiUtils.getDetailsWifiInfo(this)).append("\n");
        dis.append("Bluetooth info: ").append(BLEUtils.getDeviceList(this)).append("\n");
        displayText.setText(dis);
        awsConnection.sendData(myData);
//        new SendRequest().execute();

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
            myData.ax = values[0];
            myData.ay = values[1];
            myData.az = values[2];

            myData.g = Math.sqrt(myData.ax * myData.ax + myData.ay * myData.ay + myData.az * myData.az);
        }

    }

    public void onAccuracyChanged(Sensor arg0, int arg1) {

    }

    @Override
    public void returnLocation(Location location) {
//         获取纬度
        myData.lat = location.getLatitude();
            // 获取经度
        myData.lng = location.getLongitude();
            // 位置提供者
        myData.provider = location.getProvider();
            // 位置的准确性
        myData.acu = location.getAccuracy();
            // 高度信息
        myData.alt = location.getAltitude();
            // 方向角
//            float bearing = location.getBearing();
            // 速度 米/秒
        myData.speed = location.getSpeed();
//            setTextView();
    }

    protected void dataReceived(){
        Log.d(LOG_TAG, "dataReceived!!!");
        runOnUiThread(() -> {
            StringBuilder dis = new StringBuilder("Data received!!!\n");
            dis.append("result is: ").append(myData.result).append("\n");
            displayText.setText(dis);
        });
    }



}
