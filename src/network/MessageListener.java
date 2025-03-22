package network;

import models.*;

public class MessageListener implements Runnable {
    private final Client client;

    public MessageListener(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        
    }
}

