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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;


public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationDelegate {

    private TextView mTextMessage;
    private TextView displayText;
    private Button refreshBtn;
    private boolean mPermissionReady;

    private SensorManager sm;
    private LocationManager lm = null;
    private LocationListener myLocationListener = null;

    protected float ax = 0;
    protected float ay = 0;
    protected float az = 0;
    protected double g = 0.0;
    protected double lat = 0.0;
    protected double lng = 0.0;
    protected float acu = 0;
    protected double alt = 0.0;
    protected float speed = 0;
    protected String provider = "";
    protected String timeStamp = "";
    protected Map<String, String> wifiInfo = null;

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
        wifiInfo = WifiUtils.getDetailsWifiInfo(this);
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        StringBuilder dis = new StringBuilder("You just Refreshed!!!\n");
        dis.append("time is: ").append(timeStamp).append("\n");
        dis.append("ax is: ").append(ax).append("\n");
        dis.append("ay is: ").append(ay).append("\n");
        dis.append("az is: ").append(az).append("\n");
        dis.append("g is: ").append(g).append("\n");
        dis.append("latitude is: ").append(lat).append("\n");
        dis.append("longitude is: ").append(lng).append("\n");
        dis.append("altitude is: ").append(alt).append("\n");
        dis.append("accuracy is: ").append(acu).append("\n");
        dis.append("speed is: ").append(speed).append("\n");
        dis.append("provider is: ").append(provider).append("\n");
        dis.append("Wifi info: \n");
        dis.append("BSSID: ").append(wifiInfo.get("BSSID")).append("\n");
        dis.append("SSID: ").append(wifiInfo.get("SSID")).append("\n");
        dis.append("RSSI: ").append(wifiInfo.get("RSSI")).append("\n");
//        dis.append("Wifi info: ").append(WifiUtils.getDetailsWifiInfo(this)).append("\n");
        dis.append("Bluetooth info: ").append(BLEUtils.getDeviceList(this)).append("\n");
        displayText.setText(dis);
        new SendRequest().execute();

    }

    public void setTextView() {
        StringBuilder dis = new StringBuilder();
        wifiInfo = WifiUtils.getDetailsWifiInfo(this);
        dis.append("ax is: ").append(ax).append("\n");
        dis.append("ay is: ").append(ay).append("\n");
        dis.append("az is: ").append(az).append("\n");
        dis.append("g is: ").append(g).append("\n");
        dis.append("latitude is: ").append(lat).append("\n");
        dis.append("longitude is: ").append(lng).append("\n");
        dis.append("altitude is: ").append(alt).append("\n");
        dis.append("accuracy is: ").append(acu).append("\n");
        dis.append("speed is: ").append(speed).append("\n");
        dis.append("provider is: ").append(provider).append("\n");
        dis.append("Wifi info: \n");
        dis.append("BSSID: ").append(wifiInfo.get("BSSID")).append("\n");
        dis.append("SSID: ").append(wifiInfo.get("SSID")).append("\n");
        dis.append("RSSI: ").append(wifiInfo.get("RSSI")).append("\n");
//        dis.append("Wifi info: ").append().append("\n");
        dis.append("Bluetooth info: ").append(BLEUtils.getDeviceList(this)).append("\n");
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



    public class SendRequest extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("https://script.google.com/macros/s/AKfycbxTRzHmTU8joKrn7cGiIB-EiBCBCG32zjdRoZTQdnlzz3vUW1QL/exec");
                // https://script.google.com/macros/s/AKfycbxTRzHmTU8joKrn7cGiIB-EiBCBCG32zjdRoZTQdnlzz3vUW1QL/exec
                JSONObject postDataParams = new JSONObject();

                String id = "1XWgJdLQUH5hGd9vTstlrqx9VSjSerWB4eXTVedqorBE";

                postDataParams.put("time", timeStamp);
                postDataParams.put("ax", ax);
                postDataParams.put("ay", ay);
                postDataParams.put("az", az);
                postDataParams.put("g", g);
                postDataParams.put("latitude", lat);
                postDataParams.put("longitude", lng);
                postDataParams.put("altitude", alt);
                postDataParams.put("accuracy", acu);
                postDataParams.put("speed", speed);
                postDataParams.put("provider", provider);
                postDataParams.put("wifi mac", wifiInfo.get("BSSID"));
                postDataParams.put("wifi ssid", wifiInfo.get("SSID"));
                postDataParams.put("wifi signal level", wifiInfo.get("RSSI"));

//                postDataParams.put("id", id);

                Log.i("params", postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder("");
                    String line = "";

                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                } else {
                    return new String("false : " + responseCode);
                }
            } catch (Exception e) {
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i("PostResult", result);
            Toast.makeText(getApplicationContext(), result,
                    Toast.LENGTH_LONG).show();

        }
    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while (itr.hasNext()) {

            String key = itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }


}
