package view;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Client;
import model.Message;

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
    private final ClientGUI gui;
    private final Client model;
    
    public ClientThread(Client model, ClientGUI gui) {
        this.gui = gui;
        this.model = model;
    }
    
    /**
     * Tries to get the latest message sent to this Client (aka model)
     * Adds message content to the GUI text field if there is a BROADCAST message.
     * Passes on leader election messages.
     */
    @Override
    public void run() {
        while(true) {
            try {
                gui.getClientsList().setListData(model.getServer().getClients());
                Message message = model.getServer().getLastMessage(model.getUniqueID());
                if (message != null)
                {           
                    // Ordinary BROADCAST message received... move along
                    gui.getMainTextArea().append(message.getContent());
                }
            }
            catch (RemoteException ex)
            {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
