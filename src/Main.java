
import network.ClientSender;
import network.ServerListener;

public class Main {
    private static String address; 
    private static String port;
    private static String neighbors; // possivelmente mudar 
    // private static String folder;   possivelmente mudar
    // private static int clock;   

    public static void main(String[] args) {
        //Depois fazer as verificacoes necessarias
        address = args[0];
        port = args[1];
        neighbors = args[2];
        // folder = args[3];

        //Instancia objeto peer()
        // Inicializar o relogio
        // Clock clock = new Clock();

        try {
            int portNumber = Integer.parseInt(port);

            ServerListener serverListener = new ServerListener(portNumber);
            Thread serverThread = new Thread(serverListener);
            serverThread.start();

            ClientSender clientSender = new ClientSender(address, portNumber, neighbors);
            Thread clientThread = new Thread(clientSender);
            clientThread.start();

            // TODO: immplementar o menu de interação com o usuário
            // (enviar mensagens, listar arquivos, etc)

            serverThread.join();
            clientThread.join();

        } catch (NumberFormatException e) {
            System.err.println("Porta inválida: " + port);
            System.exit(1);
        } catch (InterruptedException e) {
            System.err.println("Thread interrompida: " + e.getMessage());
        }
    }
}