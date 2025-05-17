package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
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
            System.out.println("Endereco invalido. Use <endereco>:<porta>.");
            System.exit(1);
        }
        
        // Validar <vizinhos.txt>
        File neighborsFile = new File(args[1]);
        if ( !neighborsFile.exists() || !neighborsFile.isFile()){
            System.out.println("Erro: O arquivo de vizinhos nao existe ou nao e um arquivo valido.");
            System.exit(1);
        }
            
        // Validar <diretorio_compartilhado>
        File folder = new File(args[2]);
        if (!folder.isDirectory() || !folder.exists()) {
            System.out.println("Erro: O diretorio compartilhado nao existe ou nao e um diretorio valido.");
            System.exit(1);
        }
        
        int portNumber = Integer.parseInt(port);
        
        
        Client client = new Client(address, portNumber, neighborsFile, folder);
        
        
        try {
            Semaphore exitSemaphore = new Semaphore(0);
            CountDownLatch latch = new CountDownLatch(1);
            
            //Starts server thread
            ServerListener serverListener = new ServerListener(client, latch);
            Thread serverThread = new Thread(serverListener);
            serverThread.start();
            
            //Await serverSocket to be succesfully created
            latch.await(); 
            LinkedList<Peer> neighborsList = readNeighbors(neighborsFile);
            client.setNeighborsList(neighborsList);
            
            //Starts menu thread
            ClientMenu clientMenu = new ClientMenu(client, exitSemaphore);
            Thread clientMenuThread = new Thread(clientMenu);
            
            //Starts messageHandler thread
            MessageHandler messageHandler = new MessageHandler(client, exitSemaphore);
            Thread messageHandlerThread = new Thread(messageHandler);
            
            
            clientMenuThread.start();
            messageHandlerThread.start();
            
            serverThread.join();
            clientMenuThread.join();
            messageHandlerThread.join();

        } catch (NumberFormatException e) {
            System.err.println("Porta invalida: " + port);
            System.exit(1);
        } catch (InterruptedException e) {
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
                        System.out.println("Erro: Porta invalida na linha -> " + line);
                        continue;
                    }
                    Peer p = new Peer(address, port);
                    list.add(p);
                    System.out.println("Adicionando novo peer " + address + ":" + port + " status OFFLINE");
                } else {
                    System.out.println("Erro: Formato invalido na linha -> " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
            System.exit(1);
        }
        return list;
    }
}