package network;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.Scanner;
import models.*;

public class ClientSender implements Runnable {
    private final Client client;
    private final Scanner scanner;

    public ClientSender(Client client) {
        this.client = client;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        int option;
        do {
            System.out.println("Escolha um comando:");
            System.out.println("    [1] Listar peers");
            System.out.println("    [2] Obter peers");
            System.out.println("    [3] Listar arquivos locais");
            System.out.println("    [4] Buscar arquivos");
            System.out.println("    [5] Exibir estatísticas");
            System.out.println("    [6] Alterar tamanho de chunk");
            System.out.println("    [9] Sair");
            System.out.print(">");

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
        System.out.println("Lista de peers:");
        System.out.println("    [0] voltar para o menu anterior");

        Iterator<Peer> iterator = client.getNeighborList().iterator();

        int counter = 0;
        while (iterator.hasNext()) {
            Peer p = iterator.next();
            //  [1] 255.255.255.255:8000 ONLINE
            System.out.println(String.format("    [%d] %s:%d %s", counter + 1, p.getAddress(), p.getPort(), p.getStatus())); // esse counter+1 funciona? -- Provavelmente
            counter++;
        }
        System.out.print(">");
        int option = scanner.nextInt();

        if (option == 0) {
            return;
        }
        
        if (option > 0 && option <= counter) {
            // Envia mensagem para endereço encontrado em option (algo me parece estranho) 
            //FIXME: race condition quando as opçoes sao mostradas e se depois disso mas antes de selecionar uma opçao, a lista de peers ser alterada, a option pode nao ser mais o mesmo

            Peer p = client.getNeighborList().get(counter); 
            client.sendMessage(p, "HELLO", "");
        }
    }

    private void obterPeers() {
 
        for (Peer peer : client.getNeighborList()) {
            try {
                client.sendMessage(peer, "GET_PEERS");

                peer.setStatus("ONLINE");
                System.out.println("Atualizando peer " + peer.getAddress() + ":" + peer.getPort() + " status ONLINE");

            } catch (Exception e) {
                peer.setStatus("OFFLINE");
                System.out.println("Atualizando peer " + peer.getAddress() + ":" + peer.getPort() + " status OFFLINE");
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
                    client.sendMessage(peer, "BYE");

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
    
