
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

    // possivelmente mudar
    // alterei esta funcao para suportar outros tipos de mensagens (HELLO, BYE...
    public void sendMessage(Peer p, String type, String... extraArgs) {
        this.clock.updateClock();
        System.out.println("=> Atualizando relógio para " + this.clock.getClock());

        String message = String.format("%s:%d %d %s\n", this.address, this.port, this.clock.getClock(), type); //FIXME: todos os clocks serao arrumados posteriormente

        System.out.println(
                "Encaminhando mensagem \"" + message.trim() + "\" para " + p.getAddress() + ":" + p.getPort());

        try {
            PrintStream outPrintStream = new PrintStream(p.getSocket().getOutputStream());
            outPrintStream.print(message);
        } catch (IOException e) {
            System.out.println("Nao enviou "); // TODO:Retirar dps
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