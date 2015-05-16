package model;


import java.rmi.Remote;
import java.rmi.RemoteException;


public interface RemoteObject extends Remote {
    public void addClient(RemoteObject client) throws RemoteException;
    public void removeClient(RemoteObject client) throws RemoteException;
    public String getUniqueID() throws RemoteException;
    public Client getClientByID(String uniqueID) throws RemoteException;
    public void sendMessage(String toUniqueID, Message message) throws RemoteException;
    public void setNeighbour(RemoteObject neighbour) throws RemoteException;
    public String[] getClients() throws RemoteException;
}
