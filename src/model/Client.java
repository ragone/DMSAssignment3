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
    private String uniqueID; // The client's unique identifier (UUID).
    private RemoteObject neighbour;
    private int port;
    private boolean hasToken;
    private boolean wantToken;

    // For Chang-Roberts data
    boolean electionParticipant; // Whether this client is participating in an election
    String leaderID; // The current leader's (server) unique identifier (UUID).
    private boolean waitingForToken;

    public Client() throws RemoteException {

        electionParticipant = false; // Defaults to not participating in a leader election

        setupRegistry();
        getServer();
        generateID(); // Generate a unique UUID for this client

        port = getServer().generatePort();
        hasToken = false;

        if (isServer) {
            // messages = new HashMap<String, LinkedList<Message>>();
            ports = new HashMap<>();
            clients = new HashSet<>();
            hasToken = true;
        }

        // Add this Client to the Server's Map of <ClientID, Port number> key pairs
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

        if (oldServer.hasToken()) {
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

    /**
     * Prints the specified message to message window of the specified Client.
     * Client is specified as a port and the message is sent via TCP/IP.
     *
     * @param message The message to send
     * @param port The TCP/IP port to send the message to
     */
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

    /**
     * Sends the specified Message to the recipient Clients specified in the
     * message via TCP/IP.
     *
     * @param message The Message to send
     * @throws RemoteException Throws error if RMI server methods can not be
     * reached.
     */
    @Override
    public void sendMessage(Message message) throws RemoteException {
        if (message.getType() == Message.BROADCAST) {
            for (Map.Entry<String, Integer> entrySet : getPorts().entrySet()) {
                sendViaTcp(message.getTime() + getClientByID(message.getSender()).getUsername() + message.getContent(), entrySet.getValue());
            }
        } else if (message.getType() == Message.PRIVATE_MESSAGE) {
            List receiversList = message.getReceivers();
            List<String> receiversIDList = new LinkedList();
            for (Object resceiver : receiversList) {
                receiversIDList.add(getClientByUsername((String) resceiver).getUniqueID());
            }

            for (Map.Entry<String, Integer> entrySet : getPorts().entrySet()) {
                for (String receiver : receiversIDList) {
                    if (entrySet.getKey().equals(receiver)) {
                        sendViaTcp(message.getTime() + getClientByID(message.getSender()).getUsername() + ": " + message.getContent(), entrySet.getValue());
                    }
                }
            }
        } 
    }

    /**
     * Gets a String array of the Client's UUIDs.
     *
     * @return The String array of the Client's UUIDs
     * @throws RemoteException
     */
    public HashMap getClients() throws RemoteException {

        HashMap<String, String> clientsMap = new HashMap();
        Iterator<RemoteObject> ita = clients.iterator();
        for (int i = 0; i < clients.size(); i++) {
            RemoteObject nextClient = ita.next();
            clientsMap.put(nextClient.getUniqueID(), nextClient.getUsername());
        }
        return clientsMap;
    }

    /**
     * Gets the HashSet of Client Remote Objects.
     *
     * @return the HashSet of Client Remote Objects
     * @throws java.rmi.RemoteException
     */
    @Override
    public HashSet<RemoteObject> getClientsSet() throws RemoteException {
        return clients;
    }

    /**
     * Sets the HashSet of Client Remote Objects.
     *
     * @param clients The set of Client Remote Objects.
     * @throws java.rmi.RemoteException
     */
    @Override
    public void setClients(HashSet<RemoteObject> clients) throws RemoteException {
        this.clients = clients;
    }

    public RemoteObject getClientByUsername(String username) throws RemoteException {
        for (Iterator<RemoteObject> iterator = clients.iterator(); iterator.hasNext();) {
            RemoteObject client = iterator.next();
            if (client.getUsername().equals(username)) {
                return client;
            }
        }
        return null;
    }

    /**
     * Returns an incremented port number.
     *
     * @return An incremented port number
     * @throws RemoteException
     */
    @Override
    public int generatePort() throws RemoteException {
        startingPort++;
        return startingPort;
    }

    @Override
    public int getPort() throws RemoteException {
        return port;
    }

    @Override
    public void setPort(int port) throws RemoteException {
        this.port = port;
    }

    /**
     * Gets the Server's list of clients and their associated ports.
     *
     * @return The list of clients and their associated ports
     * @throws java.rmi.RemoteException
     */
    @Override
    public HashMap<String, Integer> getPorts() throws RemoteException {
        return ports;
    }

    /**
     * Sets the Map of the Server's list of clients and their associated ports.
     *
     * @param ports The list of clients and their associated ports
     * @throws java.rmi.RemoteException
     */
    @Override
    public void setPorts(HashMap<String, Integer> ports) throws RemoteException {
        this.ports = ports;
    }

    public void generateID() {
        uniqueID = UUID.randomUUID().toString();
    }

    @Override
    public String getUniqueID() throws RemoteException {
        return uniqueID;
    }

    /**
     * Gets this Clients neighbour.
     *
     * @return The remote object representing this Client's neighbour.
     * @throws java.rmi.RemoteException
     */
    @Override
    public RemoteObject getNeighbour() throws RemoteException {
        return neighbour;
    }

    @Override
    public void setNeighbour(RemoteObject neighbour) {
        this.neighbour = neighbour;
    }

    /**
     * Assigns the specified Client's UUID and port key pair to the
     * <Client UUID, Port> map.
     *
     * @param client The Client to store in the map
     * @throws RemoteException
     */
    @Override
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
        addPortToClient(newClient);

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
        ports.remove(client);
        client.setHasToken(false);
        if (clients.size() == 1) {
            server.setNeighbour(null);
        } else {
            for (RemoteObject otherClient : clients) {
                if (otherClient.getNeighbour().getUniqueID().equals(client.getUniqueID())) {
                    otherClient.setNeighbour(client.getNeighbour());
                }
            }
        }
    }

    @Override
    public RemoteObject getClientByID(String uniqueID) throws RemoteException {
        if (uniqueID.equals(this.uniqueID)) {
            return this;
        } else if (uniqueID.equals(server.getUniqueID())) {
            return null;
        } else if(getNeighbour() != null) {
            return getNeighbour().getClientByID(uniqueID);
        }
        return null;
    }

    /**
     * Connects to an RMI registry. If an RMI registry does not exist, it is
     * created and this client is registered as the Server. Otherwise, the RMI
     * register is located.
     *
     * @throws RemoteException
     */
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

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Checks if this Client is the Server.
     *
     * @return True if this Client is a Server, False otherwise.
     * @throws java.rmi.RemoteException
     */
    @Override
    public boolean isServer() throws RemoteException {
        return isServer;
    }

    /**
     * Sets the Server flag Boolean for this Client.
     *
     * @param bool Server flag True or False
     * @throws java.rmi.RemoteException
     */
    @Override
    public void setAsServer(boolean bool) throws RemoteException {
        this.isServer = bool;
    }

    /**
     * Tries to retrieve the Server (Client) RemoteObject from the registry.
     *
     * @return The server RemoteObject
     * @throws java.rmi.RemoteException
     */
    @Override
    public RemoteObject getServer() throws RemoteException {
        try {
            server = (RemoteObject) registry.lookup("server");
        } catch (RemoteException | NotBoundException ex) {
            System.out.println("Oh-Oh the following error occured when trying "
                    + "to get the server from getServer() : " + ex);
        }
        return server;
    }

    @Override
    public void registerAsServer() {
        try {
            registry.rebind("server", this);
        } catch (RemoteException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Gets the election electionParticipant boolean of this Client.
     *
     * @return Is election electionParticipant true or false.
     * @throws java.rmi.RemoteException
     */
    @Override
    public boolean isElectionParticipant() throws RemoteException {
        return electionParticipant;
    }

    /**
     * Sets the election electionParticipant boolean value.
     *
     * @param isParticipant Is this Client participating in an election true or
     * false
     * @throws java.rmi.RemoteException
     */
    @Override
    public void setElectionParticipant(boolean isParticipant) throws RemoteException {
        this.electionParticipant = isParticipant;
    }

    /**
     * Gets the unique ID (UUID) of the leader (Server) of this Client.
     *
     * @return the unique ID (UUID) String of the leader (Server)
     * @throws java.rmi.RemoteException
     */
    @Override
    public String getLeaderID() throws RemoteException {
        return leaderID;
    }

    /**
     * Sets the unique ID (UUID) of the leader (Server) of this Client.
     *
     * @param leaderID This Client's Server UUID
     * @throws java.rmi.RemoteException
     */
    @Override
    public void setLeaderID(String leaderID) throws RemoteException {
        this.leaderID = leaderID;
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
