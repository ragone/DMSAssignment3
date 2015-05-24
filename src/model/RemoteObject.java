package model;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;


public interface RemoteObject extends Remote {
    public void addClient(RemoteObject client) throws RemoteException;
    public void removeClient(RemoteObject client) throws RemoteException;
    public String getUniqueID() throws RemoteException;
    public RemoteObject getClientByID(String uniqueID) throws RemoteException;
    public void sendMessage(Message message) throws RemoteException;
    public void setNeighbour(RemoteObject neighbour) throws RemoteException;
    public HashMap getClients() throws RemoteException;
    public RemoteObject getNeighbour() throws RemoteException;
    public String getUsername() throws RemoteException;
    public boolean isServer() throws RemoteException;
    public int generatePort() throws RemoteException;
    public int getPort() throws RemoteException;
    public void setPort(int port) throws RemoteException;
    public void addPortToClient(RemoteObject client) throws RemoteException;
    public HashMap<String, Integer> getPorts() throws RemoteException;
    public HashSet<RemoteObject> getConnectedClients() throws RemoteException;
    public void setServer(boolean b) throws RemoteException;
    public void setNewServer() throws RemoteException;
    public boolean hasToken() throws RemoteException;
    public void setHasToken(boolean b) throws RemoteException;
    public RemoteObject getClientByUsername(String username) throws RemoteException;
}
