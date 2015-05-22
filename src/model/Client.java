package model;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
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
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Client extends UnicastRemoteObject implements RemoteObject {

    private final int RMI_PORT = 1099;
    private int startingPort = 49152;
    private Registry registry;
    private boolean isServer;
    private String username;
    private RemoteObject server;
    //private HashMap<String, LinkedList<Message>> messages;
    private HashMap<String, Integer> ports;
    private HashSet<RemoteObject> clients;
    private RemoteObject neighbour;
    private String uniqueID;
    private int port;

    public Client() throws RemoteException {

        setupRegistry();
        getServer();
        generateID();
        
        port = getServer().generatePort();
        
        if (isServer) {
            // messages = new HashMap<String, LinkedList<Message>>();
            ports = new HashMap<String, Integer>();
            clients = new HashSet<>();
        }
    }

    public void sendViaTcp(String message, int port) {
        try {
        Socket sock = new Socket("localhost", port);
        PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
        
        out.println(message);
        out.flush();

        out.close();
        sock.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Message message) throws RemoteException {
        if (message.getType() == Message.BROADCAST) {
            for (Map.Entry<String, Integer> entrySet : ports.entrySet()) {
                sendViaTcp(message.getTime() + getClientByID(message.getSender()).getUsername() + ": " + message.getContent() + "\n", entrySet.getValue());
//                LinkedList<Message> value = entrySet.getValue();
//                value.push(message);
            }
        } else if (message.getType() == Message.PRIVATE_MESSAGE) {
            List receivers = message.getReceivers();

//            for (Map.Entry<String, LinkedList<Message>> entrySet : messages.entrySet()) {
//                for (Object obj : receivers) {
//                    String receiver = (String) obj;
//                    if (entrySet.getKey().equals(receiver)) {
//                        messages.get(entrySet.getKey()).push(message);
//                    }
//                }
//            }
        }
    }

    public String[] getClients() throws RemoteException {
        String[] clientsArr = new String[clients.size()];
        Iterator<RemoteObject> ita = clients.iterator();
        for (int i = 0; i < clients.size(); i++) {
            RemoteObject nextClient = ita.next();
            clientsArr[i] = nextClient.getUniqueID();
        }
        return clientsArr;
    }
    
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

    public void setNeighbour(RemoteObject neighbour) {
        this.neighbour = neighbour;
    }

    @Override
    public void addClient(RemoteObject client) throws RemoteException {
        clients.add(client);
        ports.put(client.getUniqueID(), client.getPort());
        if (clients.size() == 1) {
            neighbour = null;
        } else if (clients.size() == 2) {
            client.setNeighbour(this);
            neighbour = client;
        } else {
            client.setNeighbour(getNeighbour());
            neighbour = client;
        }
    }

    public void removeClient(RemoteObject client) throws RemoteException {
        clients.remove(client);
        ports.remove(client.getUniqueID());
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

    public static void main(String[] args) throws RemoteException {
        new Client();
    }

//    @Override
//    public Message getLastMessage(String uniqueID) throws RemoteException {
//        if (messages.containsKey(uniqueID) && !messages.get(uniqueID).isEmpty()) {
//            return messages.get(uniqueID).pop();
//        } else {
//            return null;
//        }
//    }

    @Override
    public RemoteObject getNeighbour() throws RemoteException {
        return neighbour;
    }
}
