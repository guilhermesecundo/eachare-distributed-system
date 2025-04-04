package models;

import java.util.LinkedList;

public class Message {
    private Peer addressPeer;
    private final String messageType;
    private final LinkedList<String> extraArgs;

    public Message(Peer addressPeer, String messageType, LinkedList<String> extraArgs) {
        this.addressPeer = addressPeer;
        this.messageType = messageType;
        this.extraArgs = extraArgs;
    }
    
    //Retorna no formato: Encaminhando mensagem <endereço>:<porta> <clock> <tipo> para <endereco:porta destino>
    public String messageToString(String address, int port, int clock){
        String message = String.format(
            "    Encaminhando mensagem \"%s:%d %d %s",
            address,
            port,
            clock, 
            this.messageType
        );
        if (extraArgs != null) {
            for (String arg : extraArgs) {
                message += " " +arg;
            }
        }
        message += String.format("\" para %s:%d", addressPeer.getAddress(), addressPeer.getPort());
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
            for (String arg : extraArgs) {
                message += " " + arg; 
            }
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
