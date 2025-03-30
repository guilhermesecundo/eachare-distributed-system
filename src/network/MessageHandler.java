package network;

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
            while (true) {
                LinkedBlockingQueue<Message> list = client.getMessageList();
                Message message = list.take();

                //TODO:Tratamento para envio
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}