package unsw.blackout;

import unsw.response.models.FileInfoResponse;

public class File {
    private String filename;
    private String contents;
    private int size;
    private boolean hasTransferCompleted = true;

    public boolean isTransferCompleted() {
        return hasTransferCompleted;
    }

    public void setTransferCompleted(boolean hasTransferCompleted) {
        this.hasTransferCompleted = hasTransferCompleted;
    }

    public File(String filename, String contents) {
        setFilename(filename);
        setContents(contents);
        setSize(contents.length());
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
        this.setSize(contents.length());
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public FileInfoResponse getInfoFile() {
        return new FileInfoResponse(this.filename, this.contents, this.size, this.hasTransferCompleted);
    }
}
