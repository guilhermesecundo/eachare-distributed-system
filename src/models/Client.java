
package models;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class Client {
    private final String address;
    private final int port;
    private final File neighborsFile;
    private final File folder;

    private final ReentrantLock printLock;
    private CountDownLatch responseLatch;
    private final Clock clock;

    private LinkedList<Peer> neighborList;
    private final LinkedBlockingQueue<Message> messageList;

    private final LinkedList<FoundFile> foundFiles = new LinkedList<>();


    public Client(String address, int port, File neighborsFile, File folder) {
        this.address = address;
        this.port = port;
        this.neighborsFile = neighborsFile;
        this.folder = folder;

        this.clock = new Clock();
        this.printLock = new ReentrantLock();
        this.responseLatch = new CountDownLatch(0); 

        this.neighborList = null;
        this.messageList = new LinkedBlockingQueue<Message>();
    }

    public void addMessage(Peer p, String type, LinkedList<String> extraArgs) {
        Message message = new Message(p, type, extraArgs);
        try {
            this.messageList.put(message);
        } catch (InterruptedException ex) {
        }
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
    
    //Getters and Setters
    public File getFolder() {
        return this.folder;
    }

    public File getNeighborsFile(){
        return this.neighborsFile;
    }

    
    public String getAddress(){
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

    public void setNeighborsList(LinkedList<Peer> list){
        this.neighborList = list;
    }
    
    public void addPeer(Peer peer) {
        neighborList.add(peer);
    }


    public LinkedBlockingQueue<Message> getMessageList(){
        return this.messageList;
    }

    public ReentrantLock getPrintLock(){
        return this.printLock;
    }

    public CountDownLatch getResponseLatch(){
        return this.responseLatch;
    }

    public void setResponseLatch(CountDownLatch latch){
        this.responseLatch = latch;
    }

    public LinkedList<FoundFile> getFoundFiles() {
        return this.foundFiles;
    }
}