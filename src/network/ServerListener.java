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
            System.out.println("servidor escutando na porta " + client.getPort()); //FIXME:tirar dps

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Conexao recebida de: " + clientSocket.getInetAddress().toString());  //FIXME:tirar dps
                

                System.out.println(clientSocket.getPort() + "reste");
                String address = clientSocket.getInetAddress().toString();
                address = address.substring(1); //removes the "/" from string

                Peer p = client.findPeer(address, clientSocket.getPort());

                if (p == null) {
                    Peer peer = new Peer(address, clientSocket.getPort());
                    peer.setSocket(clientSocket);
                    this.client.addPeer(peer);
                }else{
                    p.setSocket(clientSocket);
                }

                MessageListener messageListener = new MessageListener(this.client, clientSocket);
                Thread listenerThread = new Thread(messageListener);
                listenerThread.start(); 
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }
}
