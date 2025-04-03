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
    
    //Retorna no formato: Encaminhando mensagem <endereço>:<porta> <clock> <tipo> para <endereco:porta destino>
    public String messageToString(String address, int port, int clock){
        String message = String.format(
            "   Encaminhando mensagem \"%s:%d %d %s\" para %s:%d",
            address,
            port,
            clock, 
            this.messageType, 
            addressPeer.getAddress(), 
            addressPeer.getPort()
        );
        return message;
    }
    
    //Retorna String formato para envio da mensagem
    public String messageToSendFormat(String address, int port, int clock){
        String message = String.format(
            "%s:%d %d %s",
            address,
            port,
            clock, 
            this.messageType
        );

        if (extraArgs != null) {
            message += "[ ";
            for (String arg : extraArgs) {
                message += arg + " "; 
            }
            message += "]";
        }
        message += "\n";
        return message;
    }

    public Peer getAddressPeer() {
        return this.addressPeer;
    }

    public void setAddressPeer(Peer addressPeer) {
        this.addressPeer = addressPeer;
    }

    public String getMessageType(){
        return this.messageType;
    }
}
