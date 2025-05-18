package network;

import java.io.File;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
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
                                    [5] Exibir estatisticas
                                    [6] Alterar tamanho de chunk
                                    [9] Sair
                                >""");
                client.setlast_arrow(true);
            } finally {
                client.getPrintLock().unlock();
            }

            while (!scanner.hasNextInt()) {
                System.out.println("    Entrada invalida. Digite um numero.");
                scanner.next();
                System.out.print(">");
                client.setlast_arrow(true);
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
                default -> System.out.println("    Opcao invalida. Tente outra opcao.");
            }

        } while (option != 9);
    }

    private void listarPeers() {
        client.getPrintLock().lock();
        int counter = 0;

        try {
            String message = "\nLista de peers: \n        [0] voltar para o menu anterior\n";
            for (Peer p : client.getNeighborList()) {
                // [1] 255.255.255.255:8000 ONLINE
                message = message + String.format("        [%d] %s:%d %s\n", counter + 1, p.getAddress(), p.getPort(),
                        p.getStatus());
                counter++;
            }

            System.out.print(message + ">");
            client.setlast_arrow(true);
        } finally {
            client.getPrintLock().unlock();
        }

        while (!scanner.hasNextInt()) {
            System.out.println("    Entrada invalida. Digite um numero.");
            scanner.next();
            System.out.print(">");
            client.setlast_arrow(true);
        }

        int option = scanner.nextInt();

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
                System.out.print("\n");
                File[] files = folder.listFiles();
                if (files.length > 0) {
                    for (File file : files) {
                        System.out.println(file.getName());
                    }
                } else {
                    System.out.println("    O diretorio esta vazio.");
                }
            } else {
                System.out.println("    Diretorio invalido.");
            }
        } finally {
            client.getPrintLock().unlock();
        }
    }

    private void buscarArquivos() {
        client.getFoundFiles().clear();


        System.out.println("    Buscando arquivos...");

        int messageCounter = 0;
        //envia LS para os peers vizinhos
        for (Peer peer : client.getNeighborList()) {
            if (peer.getStatus().equals("ONLINE")) {
                client.addMessage(peer, "LS", null);
                messageCounter++;
            }
        }
        client.setResponseLatch(new CountDownLatch(messageCounter));

        try {
            client.getResponseLatch().await(); // aguarda todas as respostas
        } catch (InterruptedException ex) {
        }

        client.getPrintLock().lock();
        try {

            System.out.println("\nArquivos encontrados na rede:");
            System.out.println("|    | Nome            | Tamanho | Peer            |");
            System.out.printf("| [0] %-15s | %-7s | %-15s |%n", "<Cancelar>", "", "");

            int index = 1;
            for (FoundFile file : client.getFoundFiles()) {
                System.out.printf(
                        "| [%d] %-15s | %-7d | %-15s |%n",
                        index,
                        file.getFileName(),
                        file.getFileSize(),
                        file.getPeerAddress());
                index++;
            }

            System.out.print("Digite o numero do arquivo para fazer o download: \n>");
            client.setlast_arrow(true);

            int escolha = scanner.nextInt();

            if (escolha == 0)
                return;

            if (escolha > 0 && escolha <= client.getFoundFiles().size()) {
                FoundFile selectedFile = client.getFoundFiles().get(escolha - 1);
                String destinationAddress = selectedFile.getPeerAddress();
                String[] addressParts = destinationAddress.split(":");
                String ip = addressParts[0];
                int port = Integer.parseInt(addressParts[1]);

                Peer destinationPeer = client.findPeer(ip, port);

                if (destinationPeer != null && destinationPeer.getStatus().equals("ONLINE")) {
                    LinkedList<String> args = new LinkedList<>();
                    args.add(selectedFile.getFileName());
                    args.add("0"); // Ainda nao sera usado agora
                    args.add("0"); // Ainda nao sera usado agora

                    client.addMessage(destinationPeer, "DL", args);
                    System.out.println("    arquivo escolhido " + selectedFile.getFileName());
                } else {
                    System.out.println("    Peer " + destinationAddress + " indisponivel.");
                }
            } else {
                System.out.println("    Opcao invalida.");
            }
        } finally {
            client.getPrintLock().unlock();  
        }
    }

    private void exibirEstatisticas() {
        // Ainda nao sera implementado
    }

    private void alterarTamanhoChunk() {
        // Ainda nao sera implementado
    }

    private void sair() {
        System.out.println("    Saindo...");
        boolean hasMessage = false;
        for (Peer peer : client.getNeighborList()) {
            if (peer.getStatus().equals("ONLINE")) {
                client.addMessage(peer, "BYE", null);
                hasMessage = true;
            }
        }
        if (!hasMessage) {
            System.exit(0);
        }
        try {
            exitSemaphore.acquire();
            System.exit(0);
        } catch (InterruptedException ex) {
        }
    }

}
