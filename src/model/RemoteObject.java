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
    public String[] getClients() throws RemoteException;
    public HashSet<RemoteObject> getClientsSet() throws RemoteException;
    public void setClients(HashSet<RemoteObject> clients) throws RemoteException;
    public RemoteObject getNeighbour() throws RemoteException;
    public String getUsername() throws RemoteException;
    public boolean isServer() throws RemoteException;
    public RemoteObject getServer() throws RemoteException;
    public void setAsServer(boolean bool) throws RemoteException;
    public void registerAsServer() throws RemoteException;
    public int generatePort() throws RemoteException;
    public int getPort() throws RemoteException;
    public void setPort(int port) throws RemoteException;
    public void addPortToClient(RemoteObject client) throws RemoteException;
    public HashMap<String, Integer> getPorts() throws RemoteException;
    public void setPorts(HashMap<String, Integer> ports) throws RemoteException;
    public void setElectionParticipant(boolean isParticipant) throws RemoteException;
    public boolean isElectionParticipant() throws RemoteException;
    public void setLeaderID(String leaderID) throws RemoteException;
    public String getLeaderID() throws RemoteException;
}
