package network;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.Semaphore;
import models.*;
  

public class MessageHandler implements Runnable {
    private final Client client;
    private final Semaphore exitSemaphore;
    
    public MessageHandler(Client client, Semaphore exitSemaphore) {
        this.client = client;
        this.exitSemaphore = exitSemaphore;
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
                System.out.println("    => Atualizando relógio para " + clock);
                System.out.println(message.messageToString(address, port, clock));

                Peer p = message.getAddressPeer();

                try {
                    if ( p.getSocket() == null ) { 
                        try {
                            Socket socket = new Socket(p.getAddress(), p.getPort());
                            p.setSocket(socket);
                            MessageListener messageListener = new MessageListener(this.client, socket);
                            Thread messageListenerThread = new Thread(messageListener);
                            messageListenerThread.start();
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
        }
    }

    private void verifyMessageStatus(String type, boolean status, Peer p){
        switch (type) {
            case "HELLO" -> {
                updatePeerStatus(status, p);
            }
            case "GET_PEERS" -> {
                //await response
                if (status) {
                    try {
                        //Pass the lock to listener
                        client.getPrintLock().unlock();
                        client.getClock().getClockLock().unlock();

                        //Awaits the response and lock
                        client.getResponseSemaphore().acquire();
                        client.getPrintLock().lock();
                        client.getClock().getClockLock().lock();
                    } catch (InterruptedException ex) {
                    }
                }
                updatePeerStatus(status, p);
            }
            case "BYE" ->{
                if (client.getMessageList().isEmpty()) {
                    exitSemaphore.release();
                }
            }
        }
    }

    private void updatePeerStatus(boolean status, Peer p){
        String peerStatus = p.getStatus();
        if (("ONLINE".equals(peerStatus) && !status) || "OFFLINE".equals(peerStatus) && status) {
            p.changeStatus();
            System.out.println(String.format("    Atualizando peer %s:%d status %s", p.getAddress(), p.getPort(), p.getStatus()));
        }
    }
}