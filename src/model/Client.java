package model;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Client extends UnicastRemoteObject implements RemoteObject  {
    private final int RMI_PORT = 1099;
    private Registry registry;
    private boolean isServer;
    private String username;
    private RemoteObject server;
    private HashMap<String, LinkedList<Message>> messages;
    private HashSet<RemoteObject> clients;
    private RemoteObject neighbour;
    private String uniqueID; // The client's unique identifier (UUID).
    
    // Chang-Roberts data
    boolean participant; // Whether this client is participating in an election
    String leaderID; // The current leader's (server) unique identifier (UUID).
    
    public Client() throws RemoteException {
        
        participant = false; // Defaults to not participating in a leader election
        
        setupRegistry();
        getServer();
        generateID();

        
        if(isServer) {
            messages = new HashMap<String, LinkedList<Message>>();
            clients = new HashSet<>();
        }
        
        server.addClient(this);
        
        leaderID = server.getUniqueID();
    }
    
    public void sendMessage(Message message) {
        if(message.getType() == Message.BROADCAST) {
            for (Map.Entry<String, LinkedList<Message>> entrySet : messages.entrySet()) {
                LinkedList<Message> value = entrySet.getValue();
                value.push(message);
            }
        }
    }
    
    public String[] getClients() throws RemoteException {
        String[] clientsArr = new String[clients.size()];
        Iterator<RemoteObject> ita = clients.iterator();
        for(int i = 0; i < clients.size(); i++) {
            clientsArr[i] = ita.next().getUniqueID();
        }
        return clientsArr;
    }
    
    public void generateID() {
        uniqueID = UUID.randomUUID().toString();
    }
    
    public String getUniqueID() {
        return uniqueID;
    }

    /**
     * Gets this Clients neighbour.
     * @return The remote object representing this Client's neighbour
     */
    public RemoteObject getNeighbour()
    {
        return neighbour;
    }
    
    public void setNeighbour(RemoteObject neighbour) {
        this.neighbour = neighbour;
    }
    
    /**
     * Adds the specified Client to the set of Clients, adds an entry 
     * for the specified Client in the messages hashmap and sets the neighbour of
     * the specified client, forming a logical Ring topography of Clients.
     * @param newClient The Client to add to the set of Clients.
     * @throws RemoteException If there is an error accessing the Client Object
     */
    @Override
    public void addClient(RemoteObject newClient) throws RemoteException {
        // Add the specified Client to the Set of Clients.
        clients.add(newClient);
        
        // Add entry for the specified Client
        messages.put(newClient.getUniqueID(), new LinkedList<>());
        
        // Check if this new client is the only one in the set
        if(clients.size() == 1)
            // Only one Client in set, so no neighbour
            neighbour = null;
        // Check if the new Client is the second in the set
        // i.e. a small Ring, where each Client is a neighbour of the other.
        else if (clients.size() == 2) {
            // Make this Client the neighbour of the new Client
            newClient.setNeighbour(this);
            // Set the new Client as this Clients neighbour
            neighbour = newClient;
        }
        // Otherwise new client is being added to an exisiting ring
        // Insert the new Client next to this Client
        else {
            // Make the neighbour of this Client the neighbour of the new Client.
            newClient.setNeighbour(neighbour);
            // Make this new Client this Client's neighbour
            neighbour = newClient;
        }
    }
    
    @Override
    public void removeClient(RemoteObject client) throws RemoteException {
        clients.remove(client);
        messages.remove(client.getUniqueID());
        // TODO: fix neighbour allocation
    }
    
    @Override
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
    
    public RemoteObject getServer() {
        try {
            server = (RemoteObject) registry.lookup("server");
        } catch (RemoteException | NotBoundException ex) {
            // none
        }
        return server;
    }
    
    public void registerAsServer() {
        try {
            registry.rebind("server", this);
        } catch (RemoteException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Gets the latest message sent to this Client.
     * @param uniqueID The unique ID of this Client.
     * @return The latest message sent to this Client or null if none.
     * @throws RemoteException Error if RMI Server unavailable.
     */
    @Override
    public Message getLastMessage(String uniqueID) throws RemoteException {
        if(!messages.get(uniqueID).isEmpty())
        {
            return messages.get(uniqueID).pop();          
        }
        else
        {
            return null;
        }
    }

    /**
     * Gets the election participant boolean of this Client.
     * @return Is election participant true or false.
     */
    public boolean isParticipant()
    {
        return participant;
    }

    /**
     * Sets the election participant boolean value.
     * @param isParticipant Has this Client participated in an election true or false
     */
    public void setParticipant(boolean isParticipant)
    {
        this.participant = isParticipant;
    }

    /**
     * Gets the unique ID (UUID) of the leader (Server) of this Client.
     * @return the unique ID (UUID) String of the leader (Server)
     */
    public String getLeaderID()
    {
        return leaderID;
    }

    /**
     * Sets the unique ID (UUID) of the leader (Server) of this Client.
     * @param leaderID This Client's Server UUID 
     */
    public void setLeaderID(String leaderID)
    {
        this.leaderID = leaderID;
    }
   
    public static void main(String[] args) throws RemoteException {
        new Client();
    }
}
