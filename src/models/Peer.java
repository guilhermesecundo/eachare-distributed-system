package models;

import java.net.Socket;

public class Peer {
    private String address; 
    private int port;
    private String status;
    private Socket socket; 
    
    public Peer(String address, int port) {
        this.address = address;
        this.port = port;
        this.status = "OFFLINE";
    }

    //getters e setter criados SEM EU PEDIR mas alguns deles vao ser necessarios entao deixarei todos por ora
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
    
}
