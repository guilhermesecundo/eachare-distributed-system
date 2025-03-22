package models;

import java.io.File;
import java.util.LinkedList;

public class Client {
    private String address; 
    private int port;
    private File neighborsFile;
    private File folder;  
    private Clock clock; 
    private LinkedList<Peer> neighborList;

    public Client(String address, int port, File neighborsFile, File folder, Clock clock, LinkedList<Peer> neighborList) {
        this.address = address;
        this.port = port;
        this.neighborsFile = neighborsFile;
        this.folder = folder;
        this.clock = clock;
        this.neighborList = neighborList;
    }

    public File getFolder(){
        return this.folder;
    }
    public int getPort(){
        return this.port;
    }

    public Clock getClock(){
        return this.getClock();
    }

    public void updateClock(){
        this.updateClock();
    }

    public LinkedList<Peer> getNeighborList(){
        return neighborList;
    }

    public void addPeer(Peer peer) {
        neighborList.add(peer);
    }
    
    public void removePeer(Peer peer) {
        neighborList.remove(peer);
    }

   
}
