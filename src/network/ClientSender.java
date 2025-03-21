package network;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class ClientSender implements Runnable {
    private String address;
    private int port;
    private String neighbors;

    public ClientSender(String address, int port, String neighbors) {
        this.address = address;
        this.port = port;
        this.neighbors = neighbors;
    }


    @Override
        public void run() {
            // adicionar logica para enviar as mensagens

            try (Socket socket = new Socket(address, port)) {
                OutputStream output = socket.getOutputStream();
    
                // - exemplo: envio de uma mensagem HELLO
                String message = "HELLO";
                output.write(message.getBytes());
                System.out.println("Mensagem enviada: " + message);
    
            } catch (IOException e) {
                System.err.println("Erro ao enviar mensagem: " + e.getMessage());
            }
        }
    }

    //TODO: processar mensagens,atualizar lista de peers etc