package model;

import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a Client object in the distributed messaging system.
 *
 * @author Alex
 * @modified jaimes 20150520 Refactored sendMessage() to postMessage(). Modified
 * postMessage to handle ELECTION and LEADER messages.
 * @TODO Trigger Leader Election algorithm when Server needs to be chosen
 */
public final class Client extends UnicastRemoteObject implements RemoteObject {

    private final int RMI_PORT = 1099;
    private int startingPort = 49152;
    private Registry registry;
    private boolean isServer;
    private String username;
    private RemoteObject server;
    private HashMap<String, Integer> ports;
    private HashSet<RemoteObject> clients;
    private RemoteObject neighbour;
    private int port;
    private boolean hasToken;
    private boolean wantToken;

    private String uniqueID; // The client's unique identifier (UUID).

    // For Chang-Roberts data
    boolean participant; // Whether this client is participating in an election
    String leaderID; // The current leader's (server) unique identifier (UUID).
    private boolean waitingForToken;

    public Client() throws RemoteException {

        participant = false; // Defaults to not participating in a leader election

        setupRegistry();
        getServer();
        generateID();
        
        port = getServer().generatePort();
        hasToken = false;

        if (isServer) {
            // messages = new HashMap<String, LinkedList<Message>>();
            ports = new HashMap<>();
            clients = new HashSet<>();
            hasToken = true;
        }

        getServer().addPortToClient(this);
    }
    
    public void setWantToken(boolean b) {
        wantToken = b;
    }
    
    public boolean wantToken() {
        return wantToken;
    }
    
    public boolean waitingForToken() {
        return waitingForToken;
    }
    
    public void setWaitingForToken(boolean b) {
        waitingForToken = b;
    }

    // Gets called from new server
    public void setNewServer() throws RemoteException {
        RemoteObject oldServer = getServer();
        ports = new HashMap<>();
        clients = new HashSet<>();

        ports = oldServer.getPorts();
        clients = oldServer.getConnectedClients();
        oldServer.setServer(false);

        if(oldServer.hasToken()) {
            hasToken = true;
        }
        
        isServer = true;
        registerAsServer();
    }

    public boolean hasToken() throws RemoteException {
        return hasToken;
    }

    public void setServer(boolean b) throws RemoteException {
        this.isServer = b;
    }

    public void sendViaTcp(String message, int port) {
        try {
            Socket sock = new Socket("localhost", port);
            PrintWriter out = new PrintWriter(sock.getOutputStream(), true);

            out.println(message);
            out.flush();

            out.close();
            sock.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Message message) throws RemoteException {
            if (message.getType() == Message.BROADCAST) {
                for (Map.Entry<String, Integer> entrySet : getPorts().entrySet()) {
                    sendViaTcp(message.getTime() + getClientByID(message.getSender()).getUsername() + ": " + message.getContent(), entrySet.getValue());
                }
            } else if (message.getType() == Message.PRIVATE_MESSAGE) {
                List receiversList = message.getReceivers();
                List<String> receiversIDList = new LinkedList();
                for(Object resceiver : receiversList) {
                    receiversIDList.add(getClientByUsername((String) resceiver).getUniqueID());
                }
                
                

                for (Map.Entry<String, Integer> entrySet : getPorts().entrySet()) {
                    for (String receiver : receiversIDList) {
                        if (entrySet.getKey().equals(receiver)) {
                            sendViaTcp(message.getTime() + getClientByID(message.getSender()).getUsername() + ": " + message.getContent(), entrySet.getValue());
                        }
                    }
                }
            } // Otherwise ELECTION or LEADER message, so post message to 
            // adressees (i.e. neighbours) mailbox
            else if (message.getType() == Message.ELECTION || message.getType() == Message.LEADER) {
                // Who is the message addressed to?
                String receiverID = message.getReceiverID();

            // Add the message to the adressee's mailbox
                //messages.get(receiverID).push(message);   
            }
    }

    public HashMap getClients() throws RemoteException {
        
        HashMap<String, String> clientsMap = new HashMap();
        Iterator<RemoteObject> ita = clients.iterator();
        for (int i = 0; i < clients.size(); i++) {
            RemoteObject nextClient = ita.next();
            clientsMap.put(nextClient.getUniqueID(), nextClient.getUsername());
        }
        return clientsMap;
    }
    
    public RemoteObject getClientByUsername(String username) throws RemoteException {
        for (Iterator<RemoteObject> iterator = clients.iterator(); iterator.hasNext();) {
            RemoteObject client = iterator.next();
            if(client.getUsername().equals(username)) {
                return client;
            }
        }
        return null;
    }

    @Override
    public int generatePort() throws RemoteException {
        startingPort++;
        return startingPort;
    }

    public int getPort() throws RemoteException {
        return port;
    }

    public void setPort(int port) throws RemoteException {
        this.port = port;
    }

    public void generateID() {
        uniqueID = UUID.randomUUID().toString();
    }

    public String getUniqueID() {
        return uniqueID;
    }

    /**
     * Gets this Clients neighbour.
     *
     * @return The remote object representing this Client's neighbour
     */
    @Override
    public RemoteObject getNeighbour() throws RemoteException {
        return neighbour;
    }

    public void setNeighbour(RemoteObject neighbour) {
        this.neighbour = neighbour;
    }

    public void addPortToClient(RemoteObject client) throws RemoteException {
        getPorts().put(client.getUniqueID(), client.getPort());
    }

    /**
     * Adds the specified Client to the set of Clients, adds an entry for the
     * specified Client in the messages hashmap and sets the neighbour of the
     * specified client, forming a logical Ring topography of Clients.
     *
     * @param newClient The Client to add to the set of Clients.
     * @throws RemoteException If there is an error accessing the Client Object
     */
    @Override
    public void addClient(RemoteObject newClient) throws RemoteException {
        // Add the specified Client to the Set of Clients.
        clients.add(newClient);

        // Check if this new client is the only one in the set
        if (clients.size() == 1) // Only one Client in set, so no neighbour
        {
            neighbour = null;
        } // Check if the new Client is the second in the set
        // i.e. a small Ring, where each Client is a neighbour of the other.
        else if (clients.size() == 2) {
            // Make this Client the neighbour of the new Client
            newClient.setNeighbour(this);
            // Set the new Client as this Clients neighbour
            neighbour = newClient;
        } // Otherwise new client is being added to an exisiting ring
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
        if (clients.size() == 1) {
            server.setNeighbour(null);
        } else {
            for (RemoteObject otherClient : clients) {
                if (otherClient.getNeighbour() == client) {
                    otherClient.setNeighbour(client.getNeighbour());
                }
            }
        }
    }

    public RemoteObject getClientByID(String uniqueID) throws RemoteException {
        if (uniqueID.equals(this.uniqueID)) {
            return this;
        } else if (uniqueID.equals(server.getUniqueID())) {
            return null;
        } else {
            return getNeighbour().getClientByID(uniqueID);
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
     * Gets the election participant boolean of this Client.
     *
     * @return Is election participant true or false.
     */
    public boolean isParticipant() {
        return participant;
    }

    /**
     * Sets the election participant boolean value.
     *
     * @param isParticipant Has this Client participated in an election true or
     * false
     */
    public void setParticipant(boolean isParticipant) {
        this.participant = isParticipant;
    }

    /**
     * Gets the unique ID (UUID) of the leader (Server) of this Client.
     *
     * @return the unique ID (UUID) String of the leader (Server)
     */
    public String getLeaderID() {
        return leaderID;
    }

    /**
     * Sets the unique ID (UUID) of the leader (Server) of this Client.
     *
     * @param leaderID This Client's Server UUID
     */
    public void setLeaderID(String leaderID) {
        this.leaderID = leaderID;
    }

    /**
     * @return the ports
     */
    public HashMap<String, Integer> getPorts() {
        return ports;
    }

    @Override
    public HashSet<RemoteObject> getConnectedClients() throws RemoteException {
        return clients;
    }

    /**
     * @param hasToken the hasToken to set
     */
    public void setHasToken(boolean hasToken) {
        this.hasToken = hasToken;
    }
    
    public void startTokenRing() {
        
    }

}
