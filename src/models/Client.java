
package models;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class Client {
    private final String address;
    private boolean last_arrow;
    private final int port;
    private final File neighborsFile;
    private final File folder;
    private int chunkSize;
    private final ReentrantLock bufferLock = new ReentrantLock();

    private final ReentrantLock printLock;
    private CountDownLatch responseLatch;
    private final Clock clock;

    private LinkedList<Peer> neighborList;
    private final LinkedBlockingQueue<Message> messageList;

    private final LinkedList<FoundFile> foundFiles = new LinkedList<>();
    private final LinkedList<Chunk> fileBuffer = new LinkedList<>();
    private int totalFileParts;

    private final LinkedList<Statistics> downloadStats = new LinkedList<>();
    private long downloadStartTime;
    private int downloadPeerCount;
    private int downloadChunkSize;
    private long downloadFileSize;

    public Client(String address, int port, File neighborsFile, File folder) {
        this.last_arrow = true;
        this.address = address;
        this.port = port;
        this.neighborsFile = neighborsFile;
        this.folder = folder;
        this.chunkSize = 256;

        this.clock = new Clock();
        this.printLock = new ReentrantLock();
        this.responseLatch = new CountDownLatch(0);

        this.neighborList = null;
        this.messageList = new LinkedBlockingQueue<Message>();
    }

    public void startDownload(int chunkSize, int peerCount, long fileSize) {
        this.downloadPeerCount = peerCount;
        this.downloadChunkSize = chunkSize;
        this.downloadFileSize = fileSize;
        this.downloadStartTime = System.nanoTime();
    }

    public void completeDownload() {
        double elapsedTime = (System.nanoTime() - downloadStartTime) / 1_000_000_000.0;
        downloadStats.add(new Statistics(downloadChunkSize, downloadPeerCount, downloadFileSize, elapsedTime));
    }

    public LinkedList<Statistics> getDownloadStats() {
        return downloadStats;
    }

    public void addMessage(Peer p, String type, LinkedList<String> extraArgs) {
        Message message = new Message(p, type, extraArgs);
        try {
            this.messageList.put(message);
        } catch (InterruptedException ex) {
        }
    }

    public FoundFile findFile(String fileName, long fileSize) {
        for (FoundFile file : this.foundFiles) {
            if (file.getFileName().equals(fileName) && file.getFileSize() == fileSize) {
                return file;
            }
        }
        return null;
    }

    public Peer findPeer(String address, int port) {
        for (Peer peer : this.neighborList) {
            if (peer.getAddress().equals(address) && peer.getPort() == port) {
                return peer;
            }
        }
        return null;
    }

    public void removePeer(Peer peer) {
        neighborList.remove(peer);
    }

    public void clearFoundFiles() {
        foundFiles.clear();
    }

    // Getters and Setters
    public File getFolder() {
        return this.folder;
    }

    public File getNeighborsFile() {
        return this.neighborsFile;
    }

    public String getAddress() {
        return this.address;
    }

    public int getPort() {
        return this.port;
    }

    public Clock getClock() {
        return this.clock;
    }

    public void updateClock() {
        this.clock.updateClock();
    }

    public LinkedList<Peer> getNeighborList() {
        return neighborList;
    }

    public void setNeighborsList(LinkedList<Peer> list) {
        this.neighborList = list;
    }

    public void addPeer(Peer peer) {
        neighborList.add(peer);
    }

    public LinkedBlockingQueue<Message> getMessageList() {
        return this.messageList;
    }

    public ReentrantLock getPrintLock() {
        return this.printLock;
    }

    public CountDownLatch getResponseLatch() {
        return this.responseLatch;
    }

    public void setResponseLatch(CountDownLatch latch) {
        this.responseLatch = latch;
    }

    public LinkedList<FoundFile> getFoundFiles() {
        return this.foundFiles;
    }

    public boolean getlast_arrow() {
        return last_arrow;
    }

    public void setlast_arrow(boolean last_arrow) {
        this.last_arrow = last_arrow;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getTotalFileParts() {
        return totalFileParts;
    }

    public void setTotalFileParts(int total) {
        this.totalFileParts = total;
    }

    public LinkedList<Chunk> getFileBuffer() {
        return this.fileBuffer;
    }

    public void addFileChunk(Chunk newChunk) {
        bufferLock.lock();
        try {

            fileBuffer.removeIf(chunk -> chunk.getPart() == newChunk.getPart());
            fileBuffer.add(newChunk);

        } finally {
            bufferLock.unlock();
        }
    }
}
