package network;

import java.io.File;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import models.*;

public class ClientMenu implements Runnable {
    private final Client client;
    private final Scanner scanner;
    private final Semaphore exitSemaphore;

    public ClientMenu(Client client, Semaphore exitSemaphore) {
        this.client = client;
        this.exitSemaphore = exitSemaphore;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        int option = 0;
        do {
            if (!client.getMessageList().isEmpty()) {
                try {
                    Thread.sleep(500);
                    continue;
                } catch (InterruptedException ex) {
                }
            }
            
            try {
                client.getPrintLock().lock();
                System.out.print(
                    """
                    \nEscolha um comando:
                            [1] Listar peers
                            [2] Obter peers
                            [3] Listar arquivos locais
                            [4] Buscar arquivos
                            [5] Exibir estatísticas
                            [6] Alterar tamanho de chunk
                            [9] Sair
                    >""");
            }finally {
                client.getPrintLock().unlock();
            }

            while (!scanner.hasNextInt()) {
                System.out.println("    Entrada inválida. Digite um número.");
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
                default -> System.out.println("    Opção inválida. Tente outra opção.");
            }
            
        } while (option != 9);
    }

    private void listarPeers() {
        client.getPrintLock().lock();
        int counter = 0;
        int option = 0;
        
        try {
            String message = "\nLista de peers: \n        [0] voltar para o menu anterior\n";
            for (Peer p  : client.getNeighborList()) {
                //  [1] 255.255.255.255:8000 ONLINE
                message = message + String.format("        [%d] %s:%d %s\n", counter + 1, p.getAddress(), p.getPort(), p.getStatus());
                counter++;
            }
            
            System.out.print(message + ">");
        }finally {
            client.getPrintLock().unlock();
        }

        while (!scanner.hasNextInt()) {
            System.out.println("    Entrada inválida. Digite um número.");
            scanner.next();
            System.out.print(">");
        }

        option = scanner.nextInt();

        if (option == 0) {
            return;
        }
        
        if (option > 0 && option <= counter) {
            Peer p = client.getNeighborList().get(option - 1); 
            client.addMessage(p, "HELLO", null);
        }
    }

    private void obterPeers() {
        for (Peer peer : client.getNeighborList()) {
            client.addMessage(peer, "GET_PEERS", null);
            
        }
    }

    private void listarArquivosLocais() {
        File folder = client.getFolder();
        client.getPrintLock().lock();
        try {
            if (folder.isDirectory()) {
                System.out.println("\n");
                File[] files = folder.listFiles();
                if (files != null) {
                    for (File file : files) {
                        System.out.println(file.getName());
                    }
                } else {
                    System.out.println("    O diretorio esta vazio.");
                }
            } else {
                System.out.println("    Diretorio invalido.");
            }
        }finally {
            client.getPrintLock().unlock();
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
        System.out.println("    Saindo...");
        for (Peer peer : client.getNeighborList()) {
            if (peer.getStatus().equals("ONLINE")) {
                client.addMessage(peer, "BYE", null);
            }
        }
        if (client.getMessageList().isEmpty()) {
            System.exit(0);
        }
        try {
            exitSemaphore.acquire();
            System.exit(0);
        } catch (InterruptedException ex) {
        }
    }

} 
    
