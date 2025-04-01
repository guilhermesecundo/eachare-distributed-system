package network;

import java.io.File;
import java.util.Iterator;
import java.util.Scanner;
import models.*;

public class ClientMenu implements Runnable {
    private final Client client;
    private final Scanner scanner;

    public ClientMenu(Client client) {
        this.client = client;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        int option;
        do {
            System.out.print(
                """
                Escolha um comando:
                    [1] Listar peers
                    [2] Obter peers
                    [3] Listar arquivos locais
                    [4] Buscar arquivos
                    [5] Exibir estatísticas
                    [6] Alterar tamanho de chunk
                    [9] Sair
                >""");

            while (!scanner.hasNextInt()) {
                System.out.println("Entrada inválida. Digite um número.");
                scanner.next();
                System.out.print(">");
            }
            option = scanner.nextInt();

            switch (option) {
                case 1 -> listarPeers();
                case 2 -> obterPeers();
                case 3 -> listarArquivosLocais();
                case 4 -> buscarArquivos();
                case 5 -> exibirEstatisticas();
                case 6 -> alterarTamanhoChunk();
                case 9 -> sair();
                default -> System.out.println("Opção inválida. Tente outra opção.");
            }
        } while (option != 9);
    }

    private void listarPeers() {
        String message = "Lista de peers: \n    [0] voltar para o menu anterior\n";

        Iterator<Peer> iterator = client.getNeighborList().iterator();

        int counter = 0;
        while (iterator.hasNext()) {
            Peer p = iterator.next();
            
            //  [1] 255.255.255.255:8000 ONLINE
            message = message + String.format("    [%d] %s:%d %s\n", counter + 1, p.getAddress(), p.getPort(), p.getStatus());
            counter++;
        }
        System.out.print(message + ">");
        int option = scanner.nextInt();

        if (option == 0) {
            return;
        }
        
        if (option > 0 && option <= counter) {
            Peer p = client.getNeighborList().get(counter - 1); 
            client.addMessage(p, "HELLO", null);
        }
    }

    private void obterPeers() {
        for (Peer peer : client.getNeighborList()) {
            try {
                client.addMessage(peer, "GET_PEERS", null);

                /* peer.setStatus("ONLINE");
                System.out.println("Atualizando peer " + peer.getAddress() + ":" + peer.getPort() + " status ONLINE"); */

            } catch (Exception e) {
                /* peer.setStatus("OFFLINE");
                System.out.println("Atualizando peer " + peer.getAddress() + ":" + peer.getPort() + " status OFFLINE"); */
            }
        }
    }

    private void listarArquivosLocais() {
        File folder = client.getFolder();
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    System.out.println(file.getName());
                }
            } else {
                System.out.println("O diretorio está vazio.");
            }
        } else {
            System.out.println("Diretorio invalido.");
        }
    }

    private void buscarArquivos() {
        // Ainda nao sera implementado
    }

    private void exibirEstatisticas() {
        // Ainda nao sera implementado
    }

    private void alterarTamanhoChunk() {
        // Ainda nao sera implementado
    }

    private void sair() {
 
        client.updateClock();
        System.out.println("=> Atualizando relógio para " + client.getClock());

        for (Peer peer : client.getNeighborList()) {
            if (peer.getStatus().equals("ONLINE")) {

                try {
                    client.addMessage(peer, "BYE", null);

                    peer.setStatus("OFFLINE");
                    System.out.println(
                            "Atualizando peer " + peer.getAddress() + ":" + peer.getPort() + " status OFFLINE");

                
                } catch (Exception e) {
                    System.err.println("Erro ao enviar BYE para " + peer.getAddress() +
                            ":" + peer.getPort() + ": " + e.getMessage());
                    peer.setStatus("OFFLINE");
                  
                }
            }
        }
        System.out.println("Saindo...");
        System.exit(0);

    }

} 
    
