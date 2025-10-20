package unsw.blackout.satellites;

import unsw.blackout.File;
import unsw.utils.Angle;

public class TeleportingSatellite extends Satellite {
    private int capacity = 200;
    private boolean clockwise = true;
    private int receiveRate = 15;
    private int sendRate = 10;

    public TeleportingSatellite(String satelliteId, double height, Angle position) {
        super("TeleportingSatellite", satelliteId, 1000, true, true, true, 200000, height, position);

        // Update the direction for upper hemisphere
        if (this.getPosition().compareTo(Angle.fromDegrees(180)) == -1) clockwise = false;
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

        if (clockwise) this.setPosition(this.getPosition().subtract(toAdd));
        else this.setPosition(this.getPosition().add(toAdd));

        if ((this.getPosition().compareTo(Angle.fromDegrees(180)) == 1  && !clockwise)
         || (this.getPosition().compareTo(Angle.fromDegrees(180)) == -1) && clockwise) {
            this.setPosition(Angle.fromDegrees(0));
            clockwise = clockwise ^ true; // COMP1521 moment
        }
    }

    @Override
    public String canFit(File file) {
        if (this.getTotalSizeOfFiles() + file.getSize() > this.getCapacity()) return "Max Storage Reached";
        return "";
    }

    // Force transfer: Meant for task 2c
    @Override
    public void transferOutOfRange() {
        super.forceAddTransfer();
    }
}

