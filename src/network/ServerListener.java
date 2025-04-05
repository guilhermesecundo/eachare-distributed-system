package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import models.*;

public class ServerListener implements Runnable {
    private final Client client;
    private final CountDownLatch latch;

    public ServerListener(Client client, CountDownLatch latch) {
        this.client = client;
        this.latch = latch;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(client.getPort())) {
            this.latch.countDown();
            while (true) {
                Socket clientSocket = serverSocket.accept();

                MessageListener messageListener = new MessageListener(this.client, clientSocket);
                Thread listenerThread = new Thread(messageListener);
                listenerThread.start(); 
            }
        } catch (IOException e) {
            if (e instanceof java.net.BindException) {
                System.out.println("Porta ja em uso. Utilize outra porta.");
                System.exit(1);
            }
        }
    }
}
