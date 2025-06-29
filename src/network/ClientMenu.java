package network;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
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

    private static class SummaryMetrics {
        private int count = 0;
        private double sum = 0.0;
        private double sumSquares = 0.0;

        public void add(double value) {
            count++;
            sum += value;
            sumSquares += value * value;
        }

        public double getAverage() {
            return sum / count;

        }

        // variance = [sumSquares - (sum²/N)] / (N-1)
        // standard deviation = sqrt(variance)
        public double getStandardDeviation() {
            if (count <= 1)
                return 0.0;
            double variance = (sumSquares - (sum * sum) / count) / (count - 1);
            return Math.sqrt(variance);
        }

        public int getCount() {
            return count;
        }
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
            System.out.println("    Entrada invalida. Digite um numero. \n>");
            scanner.next();
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
        // envia LS para os peers vizinhos
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
        String fileName = "";
        try {
            System.out.println("\nArquivos encontrados na rede:");
            System.out.printf("%-8s | %-20s | %-10s | %s%n", "    ", "Nome", "Tamanho", "Peer");
            System.out.printf("[%2d] %-20s | %-10s | %s%n", 0, "<Cancelar>", "", "");

            int index = 1;
            for (FoundFile file : client.getFoundFiles()) {
                fileName = file.getFileName();
                long fileSize = file.getFileSize();
                String peers = String.join(", ", file.getPeerAddresses());

                System.out.printf("[%2d] %-20s | %-10d | %s%n",
                        index,
                        fileName,
                        fileSize,
                        peers);
                index++;
            }

            System.out.print("Digite o numero do arquivo para fazer o download: \n>");
            client.setlast_arrow(true);

            int escolha = scanner.nextInt();

            if (escolha == 0)
                return;

            if (escolha > 0 && escolha <= client.getFoundFiles().size()) {
                FoundFile selectedFile = client.getFoundFiles().get(escolha - 1);
                fileName = selectedFile.getFileName();
                LinkedList<String> destinationAddresses = selectedFile.getPeerAddresses();
                int numOfAddresses = destinationAddresses.size();

                long fileSize = selectedFile.getFileSize();
                int chunkSize = client.getChunkSize();

                // arredonda para cima o número de partes do arquivo,
                // evita uma parte a mais se o tam for exatamente o mesmo do chunk

                int numOfParts = (int) Math.ceil((double) fileSize / chunkSize);

                int j = 0; // Round robin insano

                client.startDownload(chunkSize, numOfAddresses, fileSize); // inicia a medição de tempo do download
                client.setTotalFileParts(numOfParts);

                System.out.println("    arquivo escolhido " + selectedFile.getFileName());
                for (int i = 0; i < numOfParts; i++) {
                    String[] addressParts = destinationAddresses.get(j).split(":");
                    String ip = addressParts[0];
                    int port = Integer.parseInt(addressParts[1]);

                    Peer destinationPeer = client.findPeer(ip, port);

                    if (destinationPeer != null && destinationPeer.getStatus().equals("ONLINE")) {
                        LinkedList<String> args = new LinkedList<>();
                        args.add(selectedFile.getFileName());
                        args.add(Integer.toString(chunkSize));
                        args.add(Integer.toString(i));
                        client.addMessage(destinationPeer, "DL", args);
                    } else {
                        System.out.println("    Peer " + destinationAddresses.get(j) + " indisponivel.");
                    }
                    j++;
                    if (j == numOfAddresses) {
                        j = 0;
                    }
                }
            } else {
                System.out.println("    Opcao invalida.");
            }
        } finally {
            client.getPrintLock().unlock();
        }

        client.setResponseLatch(new CountDownLatch(1));

        try {
            client.getResponseLatch().await(); // Aguarda finalização do download
            System.out.println("Download do arquivo " + fileName + " finalizado.");
        } catch (InterruptedException ex) {
        }

        client.clearFoundFiles();
    }

    private void exibirEstatisticas() {
        client.getPrintLock().lock();
        try {
            Map<String, SummaryMetrics> statsMap = new HashMap<>();
            LinkedList<Statistics> allStats = client.getDownloadStats();

            // agrupa stats por (chunkSize, peerCount, fileSize)
            for (Statistics stat : allStats) {
                String key = stat.getChunkSize() + "|" + stat.getPeerCount() + "|" + stat.getFileSize();
                statsMap.computeIfAbsent(key, k -> new SummaryMetrics()).add(stat.getDownloadTime());
            }

            System.out.println("\n  Tam. chunk | N peers | Tam. arquivo | N | Tempo [s] | Desvio");
            for (Map.Entry<String, SummaryMetrics> entry : statsMap.entrySet()) {
                String[] keys = entry.getKey().split("\\|");
                SummaryMetrics agg = entry.getValue();
                System.out.printf("%-10s | %-7s | %-12s | %-2d |  %-9.6f | %-9.6f\n",
                        keys[0], keys[1], keys[2],
                        agg.getCount(),
                        agg.getAverage(),
                        agg.getStandardDeviation());
            }
        } finally {
            client.getPrintLock().unlock();
        }
    }

    private void alterarTamanhoChunk() {
        client.getPrintLock().lock();
        try {
            System.out.print("\nDigite novo tamanho de chunk: \n>");

            while (!scanner.hasNextInt()) {
                System.out.println("    Entrada invalida. Digite um numero. \n>");
                scanner.next();
                client.setlast_arrow(true);
            }

            int option = scanner.nextInt();
            if (option <= 0) {
                System.out.println("    Tamanho deve ser positivo!");
            } else {
                client.setChunkSize(option);
                System.out.println("    Tamanho de chunk atualizado: " + option);
            }
        } finally {
            client.getPrintLock().unlock();
        }
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
