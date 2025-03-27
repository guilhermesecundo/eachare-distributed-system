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

    //Talvez precise mudar
    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String message;
            while ((message = reader.readLine()) != null) { // Le ate o null
                //TODO: fazer algo com message
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler mensagem " + e.getMessage()); //tirar depois
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
}

