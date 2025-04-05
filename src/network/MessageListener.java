package network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.LinkedList;
import models.*;

public class MessageListener implements Runnable {
    private final Client client;
    private final Socket socket;

    public MessageListener(Client client, Socket socket) {
        this.client = client;
        this.socket = socket;
    }

    @Override
    public void run() {
        String message;
        boolean hasPeer = false;
        Peer sender = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            while ((message = reader.readLine()) != null) { // Le ate o null
                String[] messageParts = message.split(" ");
                if (messageParts.length < 3) {
                    continue;
                }

                String origin = messageParts[0];
                String originParts[] = origin.split(":"); 
                String type = messageParts[2];
                
                //Create a peer if its the first message and assigns it a socket
                if (!hasPeer) {
                    sender = client.findPeer(originParts[0], Integer.parseInt(originParts[1]));
                    if (sender == null) {
                        sender = createPeerFromAddress(originParts[0], originParts[1]);
                        client.addPeer(sender);
                    }
                    sender.setSocket(this.socket);
                    hasPeer = true;
                }
                
                
                client.getPrintLock().lock();
                client.getClock().getClockLock().lock();

                try {
                    if ("PEER_LIST".equals(type)) {
                        System.out.println("    Resposta recebida: \"" + message + "\"");
                    }else{
                        System.out.println("\n    Mensagem recebida: \"" + message + "\"");
                    }
                    System.out.println("    => Atualizando relógio para " + client.getClock().updateClock());
    
                    switch (type) {
                        case "HELLO" -> {
                            updatePeerStatus(sender, "ONLINE");
                        }
                        case "GET_PEERS" -> {
                            updatePeerStatus(sender, "ONLINE");
                            sendPeerListTo(sender);
                        }
                        case "PEER_LIST" -> {
                            appendList(messageParts);
                            client.getResponseSemaphore().release();
                        }
                        case "BYE" -> {
                            updatePeerStatus(sender, "OFFLINE");
                        }
                        default -> System.err.println("Tipo de mensagem desconhecido: " + type);
                    }
                } finally {
                    client.getPrintLock().unlock();
                    client.getClock().getClockLock().unlock();
                }
            }
        } catch (IOException e) {
            sender.setSocket(null);
        } finally {
            closeConnection();
        }
    }

    private void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
        }
    }

    private void appendList(String[] parts) {
        if (parts.length < 4) {
            System.err.println("Erro ao ler lista de peers: " + String.join(" ", parts));
            return;
        }

        //Add to neighbor file
        File f = client.getNeighborsFile();
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f, true))) {
            for (int i = 4; i < parts.length; i++) {
                String[] peerParts = parts[i].split(":");
                if (peerParts.length == 3) {
                    System.err.println("Erro ao ler peer: " + parts[i]);
                    continue;
                }
                String address = peerParts[0];
                int port = Integer.parseInt(peerParts[1]);
                String status = peerParts[2];
    
                Peer p = client.findPeer(address, port);
    
                if (p != null) {
                    updatePeerStatus(p, status);
                    continue;
                }
    
                Peer peer = new Peer(address, port);
                peer.setStatus(status);
                client.addPeer(peer);
                System.out.println(String.format("    Adicionando novo peer %s:%d status %s", peer.getAddress(), peer.getPort(), peer.getStatus()));
                
                String newPeer = peer.getAddress() + ":" + peer.getPort();
                writer.newLine();
                writer.write(newPeer);
            }
        } catch (IOException e) {
        }
    }

    private void sendPeerListTo(Peer sender) {
        LinkedList<String> peerList = new LinkedList<String>();
        int count = 0;

        for (Peer peer : client.getNeighborList()) {
            if (!peer.equals(sender)) {
                peerList.add(String.format("%s:%d:%s:%d", peer.getAddress(), peer.getPort(), peer.getStatus(), 0));
                count++;
            }
        }
        peerList.addFirst(Integer.toString(count));

        client.addMessage(sender, "PEER_LIST", peerList);
    }

    private void updatePeerStatus(Peer sender, String status) {
        if (sender != null) {
            String prevStatus = sender.getStatus();
            if (!prevStatus.equals(status)) {
                sender.setStatus(status);
                System.out.println("    Atualizando peer " + sender.getAddress() + ":" + sender.getPort() + " status " + status);
            }
        } 
    }

    private Peer createPeerFromAddress(String address, String port) {
        return new Peer(address, Integer.parseInt(port));
    }
}
