package model;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Client extends UnicastRemoteObject implements ServerInterface  {
    private final int RMI_PORT = 1099;
    private Registry registry;
    private boolean isServer;
    private String username;
    private ServerInterface server;
    private HashMap<String, LinkedList<Message>> messages;
    private HashSet<String> clients;
    private Client neighbour;
    private String uniqueID;
    
    public Client() throws RemoteException {
        
        setupRegistry();
        getServer();
        generateID();
        
        if(isServer) {
            messages = new HashMap<String, LinkedList<Message>>();
            clients = new HashSet<>();
            addClient(uniqueID);
            neighbour = null;
        } else {
            server.addClient(uniqueID);
        }
        
        LinkedList<Message> myMessages = new LinkedList<>();
        messages.put(uniqueID, myMessages);
    }
    
    public void sendMessage(String toUniqueID, Message message) {
        if(messages.containsKey(toUniqueID)) {
            messages.get(toUniqueID).add(message);
        }
    }
    
    public void generateID() {
        uniqueID = UUID.randomUUID().toString();
    }
    
    public String getUniqueID() {
        return uniqueID;
    }
    
    public void setNeighbour(Client neighbour) {
        this.neighbour = neighbour;
    }
    
    @Override
    public void addClient(String uniqueID) throws RemoteException {
        clients.add(uniqueID);
        Client newClient = getClientByID(uniqueID);
        if(clients.size() == 1)
            neighbour = null;
        else if (clients.size() == 2) {
            newClient.setNeighbour(this);
            neighbour = newClient;
        } else {
            newClient.setNeighbour(neighbour);
            neighbour = newClient;
        }
    }
    
    public Client getClientByID(String uniqueID) throws RemoteException {
        if(uniqueID.equals(this.uniqueID)) {
            return this;
        } else if(uniqueID.equals(server.getUniqueID())) {
            return null;
        } else {
            return neighbour.getClientByID(uniqueID);
        }
    }
    
    public void setupRegistry() throws RemoteException {
        try {
            // If the registry can be created it takes on a role as server
            registry = LocateRegistry.createRegistry(RMI_PORT);
            registerAsServer();
            isServer = true;
        } catch (RemoteException ex) {
            // otherwise server must be active and will register as client
            registry = LocateRegistry.getRegistry(RMI_PORT);
            isServer = false;
        }
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public boolean isServer() {
        return isServer;
    }
    
    public void setAsServer(boolean b) {
        this.isServer = b;
    }
    
    public void getServer() {
        try {
            server = (ServerInterface) registry.lookup("server");
        } catch (RemoteException | NotBoundException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void registerAsServer() {
        try {
            registry.rebind("server", this);
        } catch (RemoteException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) throws RemoteException {
        new Client();
    }
}
