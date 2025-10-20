package unsw.blackout.devices;

import unsw.utils.Angle;

public class HandheldDevice extends Device {
    public HandheldDevice(String deviceId, Angle position) {
        super("HandheldDevice", deviceId, position, 50000);
    }
}
