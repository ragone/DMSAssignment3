package model;


import java.rmi.Remote;
import java.rmi.RemoteException;


public interface ServerInterface extends Remote {
    public void addClient(String uniqueID) throws RemoteException;
    public String getUniqueID() throws RemoteException;
    public Client getClientByID(String uniqueID) throws RemoteException;
    public void sendMessage(String toUniqueID, Message message) throws RemoteException;
}
