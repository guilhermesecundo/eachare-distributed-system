package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import models.*;

public class ServerListener implements Runnable {
    private final Client client;

    public ServerListener(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(client.getPort())) {
            System.out.println("servidor escutando na porta " + client.getPort());

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Conexao recebida de: " + clientSocket.getInetAddress().toString());

                /* Peer peer = new Peer(?, ?);
                peer.setSocket(clientSocket);
                this.client.addPeer(peer);
                MessageListener messageListener = new MessageListener(this.client, clientSocket);
                Thread listenerThread = new Thread(messageListener);
                serverThread.start();  */
                
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }
}
