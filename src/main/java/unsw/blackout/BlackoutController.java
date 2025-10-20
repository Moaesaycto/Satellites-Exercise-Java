package unsw.blackout;

import java.util.ArrayList;
import java.util.List;

import unsw.blackout.devices.Device;
import unsw.blackout.devices.DesktopDevice;
import unsw.blackout.devices.LaptopDevice;
import unsw.blackout.devices.HandheldDevice;
import unsw.blackout.FileTransferException.VirtualFileAlreadyExistsException;
import unsw.blackout.FileTransferException.VirtualFileNoBandwidthException;
import unsw.blackout.FileTransferException.VirtualFileNoStorageSpaceException;
import unsw.blackout.FileTransferException.VirtualFileNotFoundException;

import unsw.blackout.satellites.Satellite;
import unsw.blackout.satellites.StandardSatellite;
import unsw.blackout.satellites.TeleportingSatellite;
import unsw.blackout.satellites.RelaySatellite;

import unsw.response.models.EntityInfoResponse;
import unsw.utils.Angle;
import unsw.utils.MathsHelper;
import static unsw.utils.MathsHelper.RADIUS_OF_JUPITER;

public class BlackoutController {
    private List<Device> devices = new ArrayList<Device>();
    private List<Satellite> satellites = new ArrayList<Satellite>();

    public void createDevice(String deviceId, String type, Angle position) {
        switch (type) {
            case "HandheldDevice": devices.add(new HandheldDevice(deviceId, position)); break;
            case "LaptopDevice": devices.add(new LaptopDevice(deviceId, position)); break;
            case "DesktopDevice": devices.add(new DesktopDevice(deviceId, position)); break;
            default: break;
        }
    }

    public void removeDevice(String deviceId) {
        for (int i = 0; i < devices.size(); i++) {
            if (devices.get(i).getDeviceId() == deviceId) {
                devices.remove(i);
            }
        }
    }

    public void createSatellite(String satelliteId, String type, double height, Angle position) {
        switch (type) {
            case "StandardSatellite": satellites.add(new StandardSatellite(satelliteId, height, position)); break;
            case "TeleportingSatellite": satellites.add(new TeleportingSatellite(satelliteId, height, position)); break;
            case "RelaySatellite": satellites.add(new RelaySatellite(satelliteId, height, position)); break;
            default: break;
        }
    }

    public void removeSatellite(String satelliteId) {
        for (int i = 0; i < satellites.size(); i++) {
            if (satellites.get(i).getSatelliteId() == satelliteId) {
                satellites.remove(i);
            }
        }
    }

    public List<String> listDeviceIds() {
        List<String> deviceIds = new ArrayList<String>();
        for (Device device : devices) {
            deviceIds.add(device.getDeviceId());
        }
        return deviceIds;
    }

    public List<String> listSatelliteIds() {
        List<String> satelliteIds = new ArrayList<String>();
        for (Satellite satellite : satellites) {
            satelliteIds.add(satellite.getSatelliteId());
        }
        return satelliteIds;
    }

    public void addFileToDevice(String deviceId, String filename, String content) {
        Device device = this.getDeviceFromId(deviceId);
        File file = new File(filename, content);
        device.addFile(file);
    }

    private Device getDeviceFromId(String id) {
        for (Device device : devices) {
            if (device.getDeviceId().equals(id)) {
                return device;
            }
        }
        return null;
    }

    private Satellite getSatelliteFromId(String id) {
        for (Satellite satellite : satellites) {
            if (satellite.getSatelliteId().equals(id)) {
                return satellite;
            }
        }
        return null;
    }

    public EntityInfoResponse getInfo(String id) {
        switch (this.getType(id)) {
            case ("Device"): return this.getDeviceFromId(id).getInfoDevice();
            case ("Satellite"): return this.getSatelliteFromId(id).getInfoSatellite();
            default: return null;
        }
    }

    public void simulate() {
        for (Satellite satellite : satellites) {
            // Update Position of Satellite
            satellite.updatePosition();

            satellite.updateFileUpload();
            if (satellite.isTransferring() || satellite.getTransferFromId() == "") continue;
            satellite.toggleSending(false);
            }

        for (Device device : devices) {
            // Update Position of Device
            device.updateFileUpload();
            if (device.isTransferring() || device.getTransferFromId().equals("")) continue;
            device.toggleSending(false);
        }
    }

    /**
     * Simulate for the specified number of minutes.
     * You shouldn't need to modify this function.
     */
    public void simulate(int numberOfMinutes) {
        for (int i = 0; i < numberOfMinutes; i++) {
            simulate();
        }
    }

    public List<String> communicableEntitiesInRange(String id) {
        if (this.getType(id) == "Satellite") return communicableEntitiesInRangeSatellite(this.getSatelliteFromId(id));
        else return communicableEntitiesInRangeDevice(this.getDeviceFromId(id));
    }

    /*
     * CommuicableEntities for satellite ONLY
     *
     * Adds all relevant satellites to list. If satellite is Relay, add to list of
     * relay and check those as well (almost like a stack/recursion with).
     */
    private List<String> communicableEntitiesInRangeSatellite(Satellite satelliteMain) {
        List<String> entitiesInRange = new ArrayList<>();
        List<String> checkList = new ArrayList<>();
        checkList.add(satelliteMain.getSatelliteId());

        for (int i = 0; i < checkList.size(); i++) {
            // Loops for main satellite AND any found relay satellites
            for (Satellite satellite : satellites) {
                if (this.isSatelliteInRange(satellite, this.getSatelliteFromId(checkList.get(i)))) {
                    if (satellite instanceof RelaySatellite && !checkList.contains(satellite.getSatelliteId())) {
                        // Add relay to list of relays if yet to be checked
                        checkList.add(satellite.getSatelliteId());
                    }
                    entitiesInRange.add(satellite.getSatelliteId());
                }
            }
            for (Device device : devices) {
                if (this.isDeviceInRange(device, this.getSatelliteFromId(checkList.get(i)))) {
                    entitiesInRange.add(device.getDeviceId());
                }
            }
        }
        return cleanEntitiesInRangeList(entitiesInRange, satelliteMain.getSatelliteId());
    }

    /*
     * CommunicableEntities for devices ONLY
     *
     * Similar to above, adds any found relay satellites to a list and checks those as well.
     */
    private List<String> communicableEntitiesInRangeDevice(Device deviceMain) {
        List<String> entitiesInRange = new ArrayList<>();
        List<String> checkList = new ArrayList<>();

        // Initial search for relays
        for (Satellite satellite : satellites) {
            if (this.isDeviceInRange(deviceMain, satellite)) {
                if (satellite instanceof RelaySatellite) checkList.add(satellite.getSatelliteId());
                entitiesInRange.add(satellite.getSatelliteId());
            }
        }

        // If any relays are found, they will be checked here. Otherwise, this part will be skipped.
        for (int i = 0; i < checkList.size(); i++) {
            for (Satellite satellite : satellites) {
                if (this.isSatelliteInRange(satellite, this.getSatelliteFromId(checkList.get(i)))) {
                    if (satellite instanceof RelaySatellite && !checkList.contains(satellite.getSatelliteId())) {
                        checkList.add(satellite.getSatelliteId());
                    }
                    entitiesInRange.add(satellite.getSatelliteId());
                }
            }
        }
        return cleanEntitiesInRangeList(entitiesInRange, deviceMain.getDeviceId());
    }

    /*
     * Removes duplicates and the original entity's ID from a list of communicable Entities.
     */
    private List<String> cleanEntitiesInRangeList(List<String> listOfEntities, String original) {
        List<String> cleanedList = new ArrayList<>();
        for (String item : listOfEntities) {
            if (cleanedList.contains(item) || item == original) continue;
            if (this.compatibleIds(item, original)) cleanedList.add(item);
        }
        return cleanedList;
    }

    /*
     * Checks two IDs to see if they are of compatible types to make a transfer.
     */
    private boolean compatibleIds(String id1, String id2) {
        String type1 = this.getType(id1);
        String type2 = this.getType(id2);

        if (type1.equals(type2) && type1.equals("Satellite")) return true;
        if (type1.equals("Device") && type2.equals("Satellite")) {
            return this.getSatelliteFromId(id2).supportsDevice(this.getDeviceFromId(id1));
        } else if (type1.equals("Satellite") && type2.equals("Device")) {
            return this.getSatelliteFromId(id1).supportsDevice(this.getDeviceFromId(id2));
        }
        return false;
    }

    /*
     * Gets the general type of an entity from it's ID. That is, either Satellite or Device.
     */
    private String getType(String id) {
        Satellite tempSatellite = this.getSatelliteFromId(id);
        if (tempSatellite == null) return "Device";
        return "Satellite";
    }

    /*
     * Checks to see if a device is in FIRST ORDER contact with a satellite. That is to say,
     * not through a relay.
     */
    private boolean isDeviceInRange(Device device, Satellite satellite) {
        if (device == null || satellite == null) return false;
        boolean visible = MathsHelper.isVisible(RADIUS_OF_JUPITER, device.getPosition(),
                                                satellite.getHeight(), satellite.getPosition());

        double distance = MathsHelper.getDistance(satellite.getHeight(), satellite.getPosition(), device.getPosition());
        boolean closeEnough = satellite.getRange() >= distance && device.getRange() >= distance;

        return (visible && closeEnough) && satellite.supportsDevice(device);
    }

        /*
     * Checks to see if a satellite is in FIRST ORDER contact with another satellite. That is to say,
     * not through a relay.
     */
    private boolean isSatelliteInRange(Satellite satellite1, Satellite satellite2) {
        if (satellite1.getSatelliteId() == satellite2.getSatelliteId()) return false;
        if (satellite1 == null || satellite2 == null) return false;
        boolean visible = MathsHelper.isVisible(satellite1.getHeight(), satellite1.getPosition(),
                                                satellite2.getHeight(), satellite2.getPosition());
        double distance = MathsHelper.getDistance(satellite1.getHeight(), satellite1.getPosition(),
                                                  satellite2.getHeight(), satellite2.getPosition());
        boolean closeEnough = satellite1.getRange() >= distance && satellite2.getRange() >= distance;
        return visible && closeEnough;
    }

    public void sendFile(String fileName, String fromId, String toId) throws FileTransferException {
        String fromType = this.getType(fromId);
        String toType = this.getType(toId);

        // Satellite to Satellite
        if (fromType.equals("Satellite") && toType.equals("Satellite")) {
            Satellite satelliteFrom = this.getSatelliteFromId(fromId);
            Satellite satelliteTo = this.getSatelliteFromId(toId);
            File file = satelliteFrom.getFileFromFilename(fileName);

            // Exceptions
            if (!satelliteFrom.containsFile(fileName)) throw new VirtualFileNotFoundException(fileName);
            if (satelliteFrom.isSending()) throw new VirtualFileNoBandwidthException(satelliteFrom.getSatelliteId());
            if (satelliteTo.isTransferring()) throw new VirtualFileNoBandwidthException(satelliteTo.getSatelliteId());
            if (satelliteTo.containsFile(fileName)) throw new VirtualFileAlreadyExistsException(fileName);

            String canFit = satelliteTo.canFit(file);
            if (!canFit.equals("")) throw new VirtualFileNoStorageSpaceException(canFit);

            // Some satellites have have a smaller receiving rate than sending rate, so the minimum is taken
            if (satelliteTo.getReceiveRate() < satelliteFrom.getSendRate()) {
                satelliteTo.queueFile(file, satelliteTo.getReceiveRate(), satelliteFrom.getSatelliteId());
            } else {
                satelliteTo.queueFile(file, satelliteFrom.getSendRate(), satelliteFrom.getSatelliteId());
            }
            satelliteTo.toggleSending(true);
        }

        // Device to Satellite
        if (fromType.equals("Device") && toType.equals("Satellite")) {
            Satellite satellite = this.getSatelliteFromId(toId);
            Device device = this.getDeviceFromId(fromId);
            File file = device.getFileFromFilename(fileName);

            // Exceptions
            if (!device.containsFile(fileName)) throw new VirtualFileNotFoundException(fileName);
            if (satellite.isTransferring()) throw new VirtualFileNoBandwidthException(satellite.getSatelliteId());
            if (satellite.containsFile(fileName)) throw new VirtualFileAlreadyExistsException(fileName);

            String canFit = satellite.canFit(file);
            if (!canFit.equals("")) throw new VirtualFileNoStorageSpaceException(canFit);

            // Devices have unlimited bandwidth, the only limitation is the satellite.
            satellite.queueFile(file, satellite.getReceiveRate(), device.getDeviceId());
            device.toggleSending(true);
        }

        // Satellite to Device
        if (fromType.equals("Satellite") && toType.equals("Device")) {
            Satellite satellite = this.getSatelliteFromId(fromId);
            Device device = this.getDeviceFromId(toId);
            File file = satellite.getFileFromFilename(fileName);

            // Exceptions
            if (device.containsFile(fileName)) throw new VirtualFileAlreadyExistsException(fileName);
            if (device.isTransferring()) throw new VirtualFileNoBandwidthException(device.getDeviceId());
            if (satellite.isSending()) throw new VirtualFileNoBandwidthException(satellite.getSatelliteId());
            if (!satellite.containsFile(fileName)) throw new VirtualFileNotFoundException(fileName);

            // Devices have unlimited bandwidth, the only limitation is the satellite.
            device.queueFile(file, satellite.getSendRate(), satellite.getSatelliteId());
            satellite.toggleSending(true);
        }
    }

    // Verifies if two entities are in Range in any order of contact.
    public boolean isInRange(String id1, String id2) {
        return communicableEntitiesInRange(id1).contains(id2);
    }

    public void createDevice(String deviceId, String type, Angle position, boolean isMoving) {
        createDevice(deviceId, type, position);
        // TODO: Task 3
    }

    public void createSlope(int startAngle, int endAngle, int gradient) {
        // TODO: Task 3
        // If you are not completing Task 3 you can leave this method blank :)
    }
}
