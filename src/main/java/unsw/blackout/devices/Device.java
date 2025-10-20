package unsw.blackout.devices;

import unsw.blackout.File;
import unsw.response.models.EntityInfoResponse;
import unsw.response.models.FileInfoResponse;

import static unsw.utils.MathsHelper.RADIUS_OF_JUPITER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unsw.utils.Angle;

public abstract class Device {
    // Variables for key pieces of information
    private String type;
    private String deviceId;
    private Angle position;
    private double range;
    private List<File> files;

    // Variables for file transfer options
    private File queuedFile;
    private int currIdx;
    private int transferSpeed = 0;
    private boolean isSending = false;
    private String transferFromId = "";

    public Device(String type, String deviceId, Angle position, int range) {
        setType(type);
        setDeviceId(deviceId);
        setPosition(position);
        setRange(range);
        files = new ArrayList<File>();
    }

    public double getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public String getType() {
        return type;
    }

    private void setType(String type) {
        this.type = type;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Angle getPosition() {
        return position;
    }

    public void setPosition(Angle position) {
        this.position = position;
    }

    public void addFile(File file) {
        files.add(file);
    }

    public EntityInfoResponse getInfoDevice() {
        // Create HashMap and add each file individually with filename as key
        Map<String, FileInfoResponse> fileInfos = new HashMap<>();
        for (File file : files) {
            fileInfos.put(file.getFilename(), file.getInfoFile());
        }
        return new EntityInfoResponse(this.deviceId, this.position, RADIUS_OF_JUPITER, this.type, fileInfos);
    }

    public boolean containsFile(String filename) {
        for (File file : files) {
            if (file.getFilename().equals(filename)) return true;
        }
        return false;
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

    // Force transfer: Meant for task 2c
    public void forceAddTransfer() {
        files.add(queuedFile);
        this.forceRemoveTransfer();
    }

    // Force transfer: Meant for task 2c
    public void forceRemoveTransfer() {
        for (int i = 0; i < files.size(); i++) {
            if (files.get(i).getSize() != files.get(i).getContents().length()) {
                files.remove(i);
                break;
            }
        }
        currIdx = 0;
        queuedFile = null;
    }
}
