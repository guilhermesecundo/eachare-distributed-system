package models;

import java.util.LinkedList;

public class FoundFile {  
    private final String fileName;
    private final long fileSize;
    private final LinkedList<String> peerAddresses;

    public FoundFile(String fileName, long fileSize, String peerAddress) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.peerAddresses = new LinkedList<String>();
        this.peerAddresses.add(peerAddress);
    }

     
    public String getFileName() { return fileName; }
    public long getFileSize() { return fileSize; }
    public LinkedList<String> getPeerAddresses() { return peerAddresses; }
    public void addAddress(String address){this.peerAddresses.add(address); }
}