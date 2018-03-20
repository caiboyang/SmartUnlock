package eem209as.smartunlock_IoT;

import android.location.Location;

/**
 * Created by boyang on 3/11/18.
 */

public interface LocationDelegate {
    void returnLocation(Location location);
}
