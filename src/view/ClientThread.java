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
 * @TODO Need to handle null neighbours?
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
                    if (message.getType() == Message.ELECTION)
                    {
                        // Election in progress so vote

                        // Identify UUID of client sending election message
                        String senderID = message.getSenderID();

                        // Check if messageID > ownID
                        // String compareTo returns positive integer if this String object 
                        // lexicographically follows the argument string 
                        if (senderID.compareTo(model.getUniqueID()) > 0)
                        {
                            // Vote for sending client                  
                            model.setParticipant(true); // This Client has now participated in the election
                            
                            //Send election message (containing messageID ie. the sender's ID) to the next Client
                            // @TODO handle if the Client's neighbour is null?
                            // might not be neccessary as election would not be needed for only one Client
                            model.getNeighbour().sendMessage(message);
                        }
                        // Check if messageID = ownID
                        // String compareTo returns zero if the strings are equal 
                        else if (senderID.compareTo(model.getUniqueID()) == 0)
                        {
                            // This client elected leader
                            // Send leader message with ownID to next client in ring
                            senderID = model.getUniqueID();
                            Message leaderMessage = new Message(senderID,
                                    "Leader message from" + senderID, Message.LEADER);
                            model.getNeighbour().sendMessage(leaderMessage);
                            // @TODO handle if the Client's neighbour is null?

                        }
                        // Check if messageID < ownID
                        // String compareTo returns negative int if this String object 
                        // lexicographically precedes the argument string 
                        else if (senderID.compareTo(model.getUniqueID()) < 0)
                        {
                            // This client is a better leader
                            
                            // If this Client hasn't participated in election
                            if (model.isParticipant() == false)
                            {
                                // Send election message requesting this Client
                                // as leader.
                                changRobertsStartElection();
                            }
                            // Otherwise, dont forward message as already voted 
                            // for better leader
                        }
                    }
                    else if (message.getType() == Message.LEADER)
                    {
                        // Leader has been elected

                        // identify UUID of Client sending leader message
                        model.setLeaderID(message.getSenderID());
                        model.setParticipant(false);

                        if (model.getLeaderID().equals(model.getUniqueID()))
                        {
                            // Send leader message (with leader ID) to next client in ring
                            model.getNeighbour().sendMessage(message);
                            // @TODO handle if the Client's neighbour is null?
                        }
                    }
                    else
                    {
                        // Ordinary BROADCAST message received... move along
                        gui.getMainTextArea().append(message.getContent());
                    } 
                }
            }
            catch (RemoteException ex)
            {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Starts the Chang-Roberts leader election process for this Client.
     * @throws java.rmi.RemoteException
     */
    public void changRobertsStartElection() throws RemoteException
    {
        model.setParticipant(true);

        Message electionMessage = new Message(model.getUniqueID(), "request vote for " 
                + model.getUniqueID() , Message.ELECTION);
        
        // Send election message with own ID to next process in ring system
        model.getNeighbour().sendMessage(electionMessage);
        // @TODO handle if the Client's neighbour is null?
    }
}
