package models;

public class FoundFile {  
    private final String fileName;
    private final long fileSize;
    private final String peerAddress;

    public FoundFile(String fileName, long fileSize, String peerAddress) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.peerAddress = peerAddress;
    }

     
    public String getFileName() { return fileName; }
    public long getFileSize() { return fileSize; }
    public String getPeerAddress() { return peerAddress; }
}