package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerListener implements Runnable {
    private int port;

    public ServerListener(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("servidor escutando na porta " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Conexao recebida de: " + clientSocket.getInetAddress());

                // (talvez) criar uma nova thread para lidar com a conexão
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }
}
