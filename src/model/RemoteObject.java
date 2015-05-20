package model;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.LinkedList;


public interface RemoteObject extends Remote {
    public void addClient(RemoteObject client) throws RemoteException;
    public void removeClient(RemoteObject client) throws RemoteException;
    public String getUniqueID() throws RemoteException;
    public Client getClientByID(String uniqueID) throws RemoteException;
    public void sendMessage(Message message) throws RemoteException;
    public Message getLastMessage(String uniqueID) throws RemoteException;
    public void setNeighbour(RemoteObject neighbour) throws RemoteException;
    public String[] getClients() throws RemoteException;
    public RemoteObject getNeighbour() throws RemoteException;
    public String getUsername() throws RemoteException;
    public boolean isServer() throws RemoteException;
}
