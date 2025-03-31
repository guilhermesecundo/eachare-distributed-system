package network;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.LinkedBlockingQueue;
import models.*;

public class MessageHandler implements Runnable {
    private final Client client;
    
    public MessageHandler(Client client) {
        this.client = client;
    }

    @Override
    public void run(){
        try {
            LinkedBlockingQueue<Message> list = client.getMessageList();
            String address = client.getAddress();
            int port = client.getPort();
            
            while (true) {
                Message message = list.take();
                int clock = client.getClock().updateClock();

                System.out.println("=> Atualizando relógio para " + clock);
                System.out.println(message.messageToString(address, port, clock));

                Peer p = message.getAddressPeer();

                try {
                    PrintStream outPrintStream = new PrintStream(p.getSocket().getOutputStream());
                    outPrintStream.print(message.messageToSendFormat(address, port, clock));
                } catch (IOException e) {
                    System.out.println("Nao enviou "); // TODO:Retirar dps  
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}