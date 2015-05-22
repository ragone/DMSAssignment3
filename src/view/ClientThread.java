package view;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import model.Client;
import model.Message;
import model.RemoteObject;

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
    private boolean isRunning;

    public ClientThread(Client model, ClientGUI gui) {
        this.gui = gui;
        this.client = model;
        isRunning = false;
    }

    public void setRunning(boolean b) {
        isRunning = b;
    }

    
    /**
     * Tries to get the latest message sent to this Client (aka model)
     * Adds message content to the GUI text field if there is a BROADCAST message.
     * Passes on leader election messages.
     */
    @Override
    public void run() {
        while (true) {
            if (client.getUsername() != null) {
                try {
                    int[] i = gui.getClientsList().getSelectedIndices();
                    String[] clients = client.getServer().getClients();
                    gui.getClientsList().setListData(clients);
                    gui.getClientsList().setSelectedIndices(i);
                    
                    Message message = client.getServer().getLastMessage(client.getUniqueID());
                    if (message != null) {
                        RemoteObject sender = client.getServer().getClientByID(message.getSender());
                        gui.getMainTextArea().append(message.getTime() + sender.getUsername() + ": " + message.getContent() + "\n");
                    }
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
