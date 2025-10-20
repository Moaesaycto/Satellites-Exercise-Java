package unsw.blackout.satellites;

import unsw.blackout.File;
import unsw.utils.Angle;

public class StandardSatellite extends Satellite {
    private int capacity = 80;
    private int receiveRate = 1;
    private int sendRate = 1;

    public StandardSatellite(String satelliteId, double height, Angle position) {
        super("StandardSatellite", satelliteId, 2500, true,
              true, false, 150000, height, position);
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public int getReceiveRate() {
        return receiveRate;
    }

    @Override
    public int getSendRate() {
        return sendRate;
    }

    @Override
    public void updatePosition() {
        Angle toAdd = Angle.fromRadians(this.getLinearSpeed() / this.getHeight());
        this.setPosition(this.getPosition().subtract(toAdd));
    }

    @Override
    public String canFit(File file) {
        if (this.getNumberOfFiles() >= 3) return "Max Files Reached";
        if (this.getTotalSizeOfFiles() + file.getSize() > this.getCapacity()) return "Max Storage Reached";
        return "";
    }

    // Force transfer: Meant for task 2c
    @Override
    public void transferOutOfRange() {
        super.forceRemoveTransfer();
    }
}

