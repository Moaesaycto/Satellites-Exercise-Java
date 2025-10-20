package unsw.blackout.devices;

import unsw.utils.Angle;

public class DesktopDevice extends Device {
    public DesktopDevice(String deviceId, Angle position) {
        super("DesktopDevice", deviceId, position, 200000);
    }
}
