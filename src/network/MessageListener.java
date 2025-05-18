package network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
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
            while ((message = reader.readLine()) != null) {
                String[] messageParts = message.split(" ");
                if (messageParts.length < 3)
                    continue;

                String origin = messageParts[0];
                int receivedClock = Integer.parseInt(messageParts[1]);
                String originParts[] = origin.split(":");
                String type = messageParts[2];

                // Create a peer if its the first message and assigns it a socket
                if (!hasPeer) {
                    sender = client.findPeer(originParts[0], Integer.parseInt(originParts[1]));
                    if (sender == null) {
                        sender = createPeerFromAddress(originParts[0], originParts[1]);
                        client.addPeer(sender);
                        //TODO: add to neighbor file
                    }
                    sender.setSocket(this.socket);
                    hasPeer = true;
                }

                client.getPrintLock().lock();
                client.getClock().getClockLock().lock();

                try {
                    int newClock = client.getClock().mergeClocks(receivedClock);
                    if(client.getlast_arrow()){
                        System.out.print("\n");
                        client.setlast_arrow(false);
                    }
                    System.out.println("    => Atualizando relogio para " + newClock);

                    if (receivedClock > sender.getClock()) {
                        sender.setClock(receivedClock);
                    }

                    if ("PEER_LIST".equals(type)) {
                        System.out.println("    Resposta recebida: \"" + message + "\"");
                    } else {
                        System.out.println("    Mensagem recebida: \"" + message + "\"");
                    }

                    switch (type) {
                        case "HELLO" -> updatePeerStatus(sender, "ONLINE");
                        case "GET_PEERS" -> {
                            updatePeerStatus(sender, "ONLINE");
                            sendPeerListTo(sender);
                        }
                        case "PEER_LIST" -> {
                            appendList(messageParts);
                            client.getResponseLatch().countDown();
                        }
                        case "BYE" -> {
                            updatePeerStatus(sender, "OFFLINE");
                        }
                        case "LS" -> {
                            updatePeerStatus(sender, "ONLINE");
                            sendFileListTo(sender);
                        }
                        case "LS_LIST" -> {
                            collectListFile(messageParts, sender);
                            client.getResponseLatch().countDown();
                        }

                        case "DL" -> {
                            String fileName = messageParts[3];
                            sendFile(sender, fileName);
                        }
                        case "FILE" -> {
                            saveFile(messageParts);
                        }

                        default -> System.err.println("Tipo de mensagem desconhecido: " + type);
                    }

                } finally {
                    client.getPrintLock().unlock();
                    client.getClock().getClockLock().unlock();
                }
            }
        } catch (IOException e) {

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

        // Add to neighbor file
        File f = client.getNeighborsFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f, true))) {
            for (int i = 4; i < parts.length; i++) {
                String[] peerParts = parts[i].split(":");
                if (peerParts.length != 4) {
                    System.err.println("Formato invalido do peer: " + parts[i]);
                    continue;
                }

                String address = peerParts[0];
                int port = Integer.parseInt(peerParts[1]);
                String status = peerParts[2];
                int peerClock = Integer.parseInt(peerParts[3]);

                Peer p = client.findPeer(address, port);
                if (p != null) {
                    if (peerClock > p.getClock()) {
                        p.setClock(peerClock);
                        p.setStatus(status);
                    }

                } else {
                    Peer peer = new Peer(address, port);
                    peer.setStatus(status);
                    peer.setClock(peerClock);
                    client.addPeer(peer);
                    System.out.printf("    Adicionando novo peer %s:%d status %s%n", address, port, status);
                    writer.newLine();
                    writer.write(address + ":" + port + ":" + status + ":" + peerClock);
                }
            }
        } catch (IOException e) {

        }
    }

    private void sendPeerListTo(Peer sender) {
        LinkedList<String> peerList = new LinkedList<>();
        int count = 0;

        for (Peer peer : client.getNeighborList()) {
            if (!peer.equals(sender)) {
                peerList.add(String.format("%s:%d:%s:%d", peer.getAddress(), peer.getPort(), peer.getStatus(),
                        peer.getClock()));
                count++;
            }
        }
        peerList.addFirst(Integer.toString(count));

        client.addMessage(sender, "PEER_LIST", peerList);
    }

    private void updatePeerStatus(Peer sender, String status) {
        if (sender != null && !sender.getStatus().equals(status)) {
            sender.setStatus(status);
            System.out.printf("    Atualizando peer %s:%d status %s%n",
                    sender.getAddress(), sender.getPort(), status);
        }
    }

    private Peer createPeerFromAddress(String address, String port) {
        return new Peer(address, Integer.parseInt(port));
    }

    private void sendFileListTo(Peer sender) {
        LinkedList<String> fileList = new LinkedList<>();
        File folder = client.getFolder();
        File[] files = folder.listFiles();
        int count = (files != null) ? files.length : 0;

        fileList.add(Integer.toString(count));
        if (files != null) {
            for (File file : files) {
                fileList.add(file.getName() + ":" + file.length());
            }
        }

        client.addMessage(sender, "LS_LIST", fileList);
    }

    private void collectListFile(String[] parts, Peer sender) {
        if (parts.length < 4)
            return;

        int fileCount = Integer.parseInt(parts[3]);
        for (int i = 4; i < 4 + fileCount; i++) {
            if (i >= parts.length)
                break;
            String[] fileInfo = parts[i].split(":");
            if (fileInfo.length == 2) {
                String fileName = fileInfo[0];
                long fileSize = Long.parseLong(fileInfo[1]);
                String peerAddress = sender.getAddress() + ":" + sender.getPort();
                client.getFoundFiles().add(new FoundFile(fileName, fileSize, peerAddress));
            }
        }
    }

    private void sendFile(Peer destinationPeer, String fileName) {
        try {
            File file = new File(client.getFolder(), fileName);
            if (!file.exists()) {
                System.err.println("    Arquivo não encontrado: " + fileName);
                return;
            }

            byte[] fileContent = Files.readAllBytes(file.toPath());
            String base64Content = Base64.getEncoder().encodeToString(fileContent);

            LinkedList<String> args = new LinkedList<>();
            args.add(fileName);
            args.add("0"); // Ainda nao sera usado agora
            args.add("0"); //Ainda nao sera usado agora  
            args.add(base64Content);

            client.addMessage(destinationPeer, "FILE", args);
        } catch (IOException e) {
            System.err.println("    Erro ao ler arquivo: " + fileName);
        }
    }

     
    private void saveFile(String[] parts) {
        if (parts.length < 7) {
            System.err.println("    Mensagem FILE mal formatada.");
            return;
        }

        String fileName = parts[3];
        String base64Content = parts[6];

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Content);
            Path downloadPath = Paths.get(client.getFolder().getAbsolutePath(), fileName);  
            Files.write(downloadPath, decodedBytes);

            System.out.println("    Download do arquivo " + fileName + " finalizado.");
        } catch (IOException e) {
            System.err.println("    Erro ao processar arquivo: " + fileName);
        }
    }
}
