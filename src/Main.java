import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import models.*;
import network.*;

public class Main {
    public static void main(String[] args) {

        if (args.length != 3) {
            System.out.println("Uso correto: ./eachare <endereco>:<porta> <vizinhos.txt> <diretorio_compartilhado>");
            System.exit(1);
        }

        // <endereco>:<porta>
        String fullAddress = args[0];
        String address = "";
        String port = "";

        String[] parts = fullAddress.split(":");

        if (parts.length == 2) {
            address = parts[0];
            port = parts[1];
        } else {
            System.out.println("Endereco inválido. Use <endereco>:<porta>.");
            System.exit(1);
        }
        
        // Validar <vizinhos.txt>
        File neighborsFile = new File(args[1]);
        if ( !(neighborsFile.exists() && neighborsFile.isFile()) ){
            System.out.println("Erro: O arquivo de vizinhos não existe ou não é um arquivo válido.");
            System.exit(1);
        }
            
        // Validar <diretorio_compartilhado>
        File folder = new File(args[2]);
        if (!(folder.isDirectory() && folder.exists())) {
            System.out.println("Erro: O diretório compartilhado não existe ou não é um diretório válido.");
            System.exit(1);
        }
        
        int portNumber = Integer.parseInt(port);
        
        LinkedList<Peer> neighborList = readNeighbors(neighborsFile);

        Clock clock = new Clock();
        Client client = new Client(address, portNumber, neighborsFile, folder, clock, neighborList);
        
        
        try {
            ServerListener serverListener = new ServerListener(client);
            Thread serverThread = new Thread(serverListener);
            serverThread.start();

            ClientSender clientSender = new ClientSender(client);
            Thread clientThread = new Thread(clientSender);
            clientThread.start();

            serverThread.join();
            clientThread.join();

        } catch (NumberFormatException e) {
            System.err.println("Porta inválida: " + port);
            System.exit(1);
        } catch (InterruptedException e) {
            System.err.println("Thread interrompida: " + e.getMessage());
        }
    }

    public static LinkedList<Peer> readNeighbors(File f){
        LinkedList<Peer> list = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");

                if (parts.length == 2) {
                    String address = parts[0];
                    int port;

                    try {
                        port = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        System.out.println("Erro: Porta inválida na linha -> " + line);
                        continue;
                    }
                    Peer p = new Peer(address, port);
                    list.add(p);
                    System.out.println("Adicionando novo peer " + address + ":" + port + "status OFFLINE");
                } else {
                    System.out.println("Erro: Formato inválido na linha -> " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
        }
        return list;
    }
}