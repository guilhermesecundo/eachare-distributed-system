package network;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import models.*;

public class MessageHandler implements Runnable {
    private final Client client;
    
    public MessageHandler(Client client) {
        this.client = client;
    }

    @Override
    public void run(){
        try {
            String address = client.getAddress();
            int port = client.getPort();
            
            while (true) {
                Message message = client.getMessageList().take();
                int clock;

                client.getPrintLock().lock();
                client.getClock().getClockLock().lock();

                
                clock = client.getClock().updateClock();
                System.out.println("   => Atualizando relógio para " + clock);
                System.out.println(message.messageToString(address, port, clock));

                Peer p = message.getAddressPeer();

                try {
                    if ( p.getSocket() == null ) { 
                        try {
                            Socket socket = new Socket(p.getAddress(), p.getPort());
                            p.setSocket(socket);
                        } catch (IOException ex) {
                            //Couldnt connect to the server
                            verifyMessageStatus(message.getMessageType(), false, p);
                            continue;
                        }
                    }
    
                    try {
                        PrintStream outPrintStream = new PrintStream(p.getSocket().getOutputStream());
                        outPrintStream.print(message.messageToSendFormat(address, port, clock));
                        verifyMessageStatus(message.getMessageType(), true, p);
                    } catch (IOException e) {
                        verifyMessageStatus(message.getMessageType(), false, p);
                    }
                }finally {
                    client.getPrintLock().unlock();
                    client.getClock().getClockLock().unlock();
                }
            }
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    }

    private void verifyMessageStatus(String type, boolean status, Peer p){
        switch (type) {
            case "HELLO" -> {
                String peerStatus = p.getStatus();
                //Change the status
                if (("ONLINE".equals(peerStatus) && !status) || "OFFLINE".equals(peerStatus) && status) {
                    p.changeStatus();
                    System.out.println(String.format("   Atualizando peer %s:%d status %s", p.getAddress(), p.getPort(), p.getStatus()));
                }
            }
            case "GET_PEERS" -> {

            }
        }
    }
}