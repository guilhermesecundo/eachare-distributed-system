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
            while (true) {
                Socket clientSocket = serverSocket.accept();

                MessageListener messageListener = new MessageListener(this.client, clientSocket);
                Thread listenerThread = new Thread(messageListener);
                listenerThread.start(); 
            }
        } catch (IOException e) {
        }
    }
}
