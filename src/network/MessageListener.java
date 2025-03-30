package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import models.*;

public class MessageListener implements Runnable {
    private final Client client;
    private final Socket socket;

    public MessageListener(Client client, Socket socket) {
        this.client = client;
        this.socket = socket;
    }

    // Talvez precise mudar
    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String message;
            while ((message = reader.readLine()) != null) { // Le ate o null
                // TODO: fazer algo com message

                String[] messageParts = message.split(" ");
                if (messageParts.length < 3) {
                    System.err.println("Erro ao ler mensagem");
                    continue;

                }

                String origin = messageParts[0];
                String type = messageParts[2];
                Peer sender = createPeerFromAddress(origin);
                
                if (client.findPeer(sender.getAddress(), sender.getPort()) == null) {
                    client.addPeer(sender);
                }

                //separei o envio do recebimento de mensagens, aqui ele so recebe e processa a mensagem
                switch (type) {
                    case "HELLO":
                        updatePeerStatus(sender, "ONLINE");
                        break;
                    case "GET_PEERS":
                        updatePeerStatus(sender, "ONLINE");
                        sendPeerListTo(sender);
                        break;
                    case "PEER_LIST":
                        appendList(messageParts);
                        break;
                    case "BYE":
                        updatePeerStatus(sender, "OFFLINE");
                        break;
                    default:
                        System.err.println("Tipo de mensagem desconhecido: " + type);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler mensagem " + e.getMessage()); // tirar depois
        } finally {
            closeConnection();
        }
    }

    private void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("Conexão fechada.");
            }
        } catch (IOException e) {
            System.err.println("Erro ao fechar conexão: " + e.getMessage());
        }
    }

    private void appendList(String[] parts) {

        if (parts.length < 4) {
            System.err.println("Erro ao ler lista de peers: " + String.join(" ", parts));
            return;
        }
        for (int i = 4; i < parts.length; i++) {
            String[] peerParts = parts[i].split(":");
            if (peerParts.length == 3) {
                System.err.println("Erro ao ler peer: " + parts[i]);
                continue;
            }
            String address = peerParts[0];
            int port = Integer.parseInt(peerParts[1]);
            Peer peer = new Peer(address, port);
            client.addPeer(peer);
        }
    }

    private void sendPeerListTo(Peer sender) {
        StringBuilder peerList = new StringBuilder();
        int count = 0;

        for (Peer peer : client.getNeighborList()) {
            if (!peer.equals(sender)) {
                peerList.append(peer.getAddress()).append(":").append(peer.getPort()).append(":") //Ta porra KKKKKKKKKKKKK
                        .append(peer.getStatus()).append(":0 ");
                count++;
            }
        }

        client.sendMessage(sender, "PEER_LIST", String.valueOf(count), peerList.toString().trim());
    }

    private void updatePeerStatus(Peer sender, String status) {
        Peer peer = client.findPeer(sender.getAddress(), sender.getPort());
        if (peer != null) {
            peer.setStatus(status);
        } else {
            System.err.println("Peer não encontrado: " + sender.getAddress() + ":" + sender.getPort());
        }
    }

    private Peer createPeerFromAddress(String origin) {
        String[] parts = origin.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Formato de origem inválido: " + origin);
        }
        return new Peer(parts[0], Integer.parseInt(parts[1]));
    }
}
