package unsw.blackout.satellites;

import unsw.utils.Angle;

public class RelaySatellite extends Satellite {
    private boolean clockwise = true;

    public RelaySatellite(String satelliteId, double height, Angle position) {
        super("RelaySatellite", satelliteId, 1500, true, true, true, 300000, height, position);
    }

    @Override
    public void updatePosition() {
        Angle toAdd = Angle.fromRadians(this.getLinearSpeed() / this.getHeight());

        // Identifying which direction to travel
        if (this.getPosition().toDegrees() <= 345 && this.getPosition().toDegrees() > 190) clockwise = true;
        else if (this.getPosition().toDegrees() > 345 || this.getPosition().toDegrees() < 140) clockwise = false;

        if (clockwise) this.setPosition(this.getPosition().subtract(toAdd));
        else this.setPosition(this.getPosition().add(toAdd));
    }
}

