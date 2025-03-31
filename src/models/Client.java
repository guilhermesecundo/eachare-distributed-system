
package models;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

public class Client {
    private String address;
    private int port;
    private File neighborsFile;
    private File folder;
    private Clock clock;
    private LinkedList<Peer> neighborList;
    private LinkedBlockingQueue<Message> messageList;

    public Client(String address, int port, File neighborsFile, File folder, LinkedList<Peer> neighborList) {
        this.address = address;
        this.port = port;
        this.neighborsFile = neighborsFile;
        this.folder = folder;
        this.neighborList = neighborList;
        this.clock = new Clock();
        this.messageList = new LinkedBlockingQueue<Message>();
    }

    // alterei esta funcao para suportar outros tipos de mensagens (HELLO, BYE...
    public void addMessage(Peer p, String type, LinkedList<String> extraArgs) {
        Message message = new Message(p, type, extraArgs);
        try {
            this.messageList.put(message);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        
    }

    public Peer findPeer(String address, int port) {
        for (Peer peer : neighborList) {
            if (peer.getAddress().equals(address) && peer.getPort() == port) {
                return peer;
            }
        }
        return null;
    }

    public File getFolder() {
        return this.folder;
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

    public void addPeer(Peer peer) {
        neighborList.add(peer);
    }

    public void removePeer(Peer peer) {
        neighborList.remove(peer);
    }

    public LinkedBlockingQueue getMessageList(){
        return this.messageList;
    }

}