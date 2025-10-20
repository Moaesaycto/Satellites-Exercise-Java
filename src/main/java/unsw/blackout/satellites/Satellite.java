package unsw.blackout.satellites;

import unsw.blackout.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unsw.blackout.devices.Device;
import unsw.response.models.EntityInfoResponse;
import unsw.response.models.FileInfoResponse;
import unsw.utils.Angle;

public abstract class Satellite {
    // Variables for key pieces of information
    private String type;
    private String satelliteId;
    private double range;
    private int linearSpeed;
    private boolean supportsHandheld;
    private boolean supportsLaptop;
    private boolean supportsDesktop;
    private double height;
    private Angle position;

    // Default restrictions for file transfers
    private int capacity = 80;
    private int receiveRate = 0;
    private int sendRate = 0;

    // Variables for file transfer options
    private List<File> files = new ArrayList<File>();
    private File queuedFile;
    private int currIdx;
    private int transferSpeed = 0;
    private boolean isSending = false;
    private String transferFromId = "";

    public Satellite(String type, String satelliteId, int linearSpeed, boolean supportsHandheld, boolean supportsLaptop,
                     boolean supportsDesktop, int range, double height, Angle position) {
        setType(type);
        setRange(range);
        setSatelliteId(satelliteId);
        setLinearSpeed(linearSpeed);
        setSupportsHandheld(supportsHandheld);
        setSupportsLaptop(supportsLaptop);
        setSupportsDesktop(supportsDesktop);
        setHeight(height);
        setPosition(position);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSatelliteId() {
        return satelliteId;
    }

    private void setSatelliteId(String satelliteId) {
        this.satelliteId = satelliteId;
    }

    public double getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public int getLinearSpeed() {
        return linearSpeed;
    }
    public void setLinearSpeed(int linearSpeed) {
        this.linearSpeed = linearSpeed;
    }
    public boolean isSupportsHandheld() {
        return supportsHandheld;
    }
    private void setSupportsHandheld(boolean supportsHandheld) {
        this.supportsHandheld = supportsHandheld;
    }
    public boolean isSupportsLaptop() {
        return supportsLaptop;
    }
    private void setSupportsLaptop(boolean supportsLaptop) {
        this.supportsLaptop = supportsLaptop;
    }
    public boolean isSupportsDesktop() {
        return supportsDesktop;
    }
    private void setSupportsDesktop(boolean supportsDesktop) {
        this.supportsDesktop = supportsDesktop;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public Angle getPosition() {
        return position;
    }

    public void setPosition(Angle position) {

        // We must make sure theta in between 0 and 360.
        double currPos = position.toDegrees();
        while (currPos >= 360) currPos = currPos - 360;
        while (currPos < 0) currPos = currPos + 360;

        this.position = Angle.fromDegrees(currPos);
    }

    public boolean supportsDevice(Device device) {
        switch (device.getType()) {
            case ("HandheldDevice"): return this.supportsHandheld;
            case ("LaptopDevice"): return this.supportsLaptop;
            case ("DesktopDevice"): return this.supportsDesktop;
            default: return false;
        }
    }

    public EntityInfoResponse getInfoSatellite() {
        // Create HashMap and add each file individually with filename as key
        Map<String, FileInfoResponse> fileInfos = new HashMap<>();
        for (File file : files) {
            fileInfos.put(file.getFilename(), file.getInfoFile());
        }
        return new EntityInfoResponse(this.satelliteId, this.position, this.height, this.type, fileInfos);
    }

    // Default position update
    public void updatePosition() {
        Angle toAdd = Angle.fromRadians(this.getLinearSpeed() / this.getHeight());
        this.setPosition(this.getPosition().subtract(toAdd));
    }

    public boolean containsFile(String filename) {
        for (File file : files) {
            if (file.getFilename().equals(filename)) return true;
        }
        return false;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getReceiveRate() {
        return receiveRate;
    }

    public int getSendRate() {
        return sendRate;
    }

    public File getFileFromFilename(String filename) {
        for (File file : files) {
            if (file.getFilename().equals(filename)) return file;
        }
        return null;
    }

    /*
     * Queue file for upload:
     *  > Sets the device in transfer mode
     *  > Keeps track of the amount of bytes sent
     *  > Inserts partially completed file to main files
    */
    public void queueFile(File file, int transSpeed, String fromId) {
        // Initialising local fields
        transferFromId = fromId;
        queuedFile = file;
        transferSpeed = transSpeed;

        // Preparing temporary file for upload
        File upload = new File(file.getFilename(), "");
        upload.setSize(file.getSize());
        upload.setTransferCompleted(false);
        currIdx = 0;

        files.add(upload);
    }

    public void updateFileUpload() {
        // tempIdx used to update the current amount of sent bytes at the end
        int tempIdx = 0;
         // Initial checking
        if (queuedFile == null) return;
        File mainFile = null;

        // Getting file
        for (File file : files) {
            if (file.getFilename().equals(queuedFile.getFilename())) mainFile = file;
        }

        // Adding in the specified amount of byte (transfer speed)
        for (int i = 0; i < transferSpeed; i++) {
            if (currIdx + i >= queuedFile.getSize() - 1) {
                 // If file is sent entirely, complete the file and reset local fields
                mainFile.setContents(mainFile.getContents() + queuedFile.getContents().charAt(currIdx + i));
                mainFile.setTransferCompleted(true);
                queuedFile = null;
                currIdx = 0;
                return;
            }
            mainFile.setContents(mainFile.getContents() + queuedFile.getContents().charAt(currIdx + i));
            tempIdx++;
            mainFile.setSize(queuedFile.getSize());
        }

        currIdx = currIdx + tempIdx;
    }

    // Check to see if the Device is currently receiving a file
    public boolean isTransferring() {
        return (queuedFile != null);
    }

      // Update whether or not this device is sending a file
    public void toggleSending(boolean status) {
        isSending = status;
    }

    public boolean isSending() {
        return isSending;
    }

    public String getTransferFromId() {
        return transferFromId;
    }

    public int getNumberOfFiles() {
        return files.size();
    }

    public int getTotalSizeOfFiles() {
        int total = 0;
        for (File file : files) {
            total = total + file.getSize();
        }
        return total;
    }

    public String canFit(File file) {
        return "";
    }

    // Force transfer: Meant for task 2c
    public void transferOutOfRange() {
        queuedFile = null;
    }

    // Force transfer: Meant for task 2c
    public void forceAddTransfer() {
        files.add(queuedFile);
        this.forceRemoveTransfer();
    }

    // Force transfer: Meant for task 2c
    public void forceRemoveTransfer() {
        for (int i = 0; i < files.size(); i++) {
            System.out.println(files.get(i));
            if (files.get(i).getSize() != files.get(i).getContents().length()) {
                files.remove(i);
                break;
            }
        }
        currIdx = 0;
        queuedFile = null;
    }
}
