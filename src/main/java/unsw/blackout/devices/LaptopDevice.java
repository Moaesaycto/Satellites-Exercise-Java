package unsw.blackout.devices;

import unsw.utils.Angle;

public class LaptopDevice extends Device {
    public LaptopDevice(String deviceId, Angle position) {
        super("LaptopDevice", deviceId, position, 100000);
    }
}
