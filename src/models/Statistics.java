package models;

public class Statistics {
    private final int chunkSize;
    private final int peerCount;
    private final long fileSize;
    private final double downloadTime;

    public Statistics(int chunkSize, int peerCount, long fileSize, double downloadTime) {
        this.chunkSize = chunkSize;
        this.peerCount = peerCount;
        this.fileSize = fileSize;
        this.downloadTime = downloadTime;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public int getPeerCount() {
        return peerCount;
    }

    public long getFileSize() {
        return fileSize;
    }

    public double getDownloadTime() {
        return downloadTime;
    }
}