package view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Client;

/**
 * Represents a new Client process running on a separate thread on the system.
 * @author Alex
 * @modified jaimes 20150517 Moved Chang-Roberts leader election logic here.
 * @modified jaimes 20150518 Completed Chang-Roberts leader election algorithm.
 * @modified jaimes 20150520 Refactored Chang-Roberts leader election algorithm to
 * post messages to neighbours.
 * @TODO Need to handle null neighbours?
 * @modified jaimes 20150522 Moved Chang-Roberts logic to ChangRoberts class.
 */
public class ClientThread implements Runnable {

    private ClientGUI gui;
    private Client client;
    final int port = 44827;

    public ClientThread(Client model, ClientGUI gui) {
        this.gui = gui;
        this.client = model;
    }
    
    /**
     * Tries to get the latest message sent to this Client (aka model)
     * Adds message content to the GUI text field if there is a BROADCAST message.
     * Passes on leader election messages.
     */
    @Override
    public void run() {
        // Start thread for token
        new Thread(new Runnable() {

            @Override
            public void run() {
                while(true) {
                    try {
                        if(client.hasToken() && !client.wantToken() && client.getNeighbour() != null) {
                            client.getNeighbour().setHasToken(true);
                            client.setHasToken(false);
                        }
                    } catch (RemoteException ex) {
                        Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
        // Start thread for listening to incomming messages
        new Thread(new Runnable() {
            Socket clientSock;
            ServerSocket serverSock;
            BufferedReader br;
            
            @Override
            public void run() {
                try {
                    serverSock = new ServerSocket(client.getPort());
                } catch (IOException ex) {
                    Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                while (true) {
                        try {
                            clientSock = serverSock.accept();
                            br = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
                            String result = br.readLine();
                            gui.getMainTextArea().append(result + "\n");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }
            }
        }).start();
        
        // update gui
        while (true) {
            if (client.getUsername() != null) {
                try {
                    int[] i = gui.getClientsList().getSelectedIndices();
                    HashMap clients = client.getServer().getClients();
                    gui.getClientsList().setListData(clients.values().toArray());
                    gui.getClientsList().setSelectedIndices(i);
                } catch (RemoteException ex) {
                    Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
