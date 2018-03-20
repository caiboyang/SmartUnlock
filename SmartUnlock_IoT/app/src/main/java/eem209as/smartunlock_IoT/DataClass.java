package eem209as.smartunlock_IoT;

import java.util.Map;

/**
 * Created by boyang on 3/19/18.
 */

class DataClass {
    boolean isSafe = false;
    float ax = 0;
    float ay = 0;
    float az = 0;
    double g = 0.0;
    double lat = 0.0;
    double lng = 0.0;
    float acu = 0;
    double alt = 0.0;
    float speed = 0;
    String provider = "";
    String timeStamp = "";
    String dayStamp = "";
    Map<String, String> wifiInfo = null;
}
