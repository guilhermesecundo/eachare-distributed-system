
package models;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;

public class Client {
    private String address;
    private int port;
    private File neighborsFile;
    private File folder;
    private Clock clock;
    private LinkedList<Peer> neighborList;

    public Client(String address, int port, File neighborsFile, File folder, Clock clock,
            LinkedList<Peer> neighborList) {
        this.address = address;
        this.port = port;
        this.neighborsFile = neighborsFile;
        this.folder = folder;
        this.clock = clock;
        this.neighborList = neighborList;
    }

    // possivelmente mudar
    // alterei esta funcao para suportar outros tipos de mensagens (HELLO,
    // GET_PEERS, PEER_LIST...)
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

}