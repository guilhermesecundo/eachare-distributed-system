package models;

import java.util.LinkedList;

public class Message {
    private Peer addressPeer;
    private String messageType;
    private LinkedList<String> extraArgs;

    public Message(Peer addresPeer, String messageType, LinkedList<String> extraArgs) {
        this.addressPeer = addresPeer;
        this.messageType = messageType;
        this.extraArgs = extraArgs;
    }

    
}
