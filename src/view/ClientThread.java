package view;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Client;
import model.Message;

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
                        String messageID = message.getSenderID();

                        // Check if messageID > ownID
                        // String compareTo returns positive integer if this String object 
                        // lexicographically follows the argument string 
                        if (messageID.compareTo(model.getUniqueID()) > 0)
                        {
                            // Vote for other client
                            
                            model.setParticipant(true); // This Client has now participated in the election
                            // @TODO Send election message with messageID to next process
                        }
                        // Check if messageID = ownID
                        // String compareTo returns zero if the strings are equal 
                        else if (messageID.compareTo(model.getUniqueID()) == 0)
                        {
                            // This client elected leader
                            // @TODO Send leader message with ownID to next client in ring

                        }
                        // Check if messageID < ownID
                        // String compareTo returns negative int if this String object 
                        // lexicographically precedes the argument string 
                        else if (messageID.compareTo(model.getUniqueID()) < 0)
                        {
                            // This client is better
                            if (model.isParticipant() == false)
                            {
                                changRobertsStartElection();
                            }
                            else
                            {
                                // dont forward message as already voted for better
                                // leader
                            }
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
                            // @TODO Send leader message with leader ID to next client in ring
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
     */
    public void changRobertsStartElection()
    {
        model.setParticipant(true);

        Message electionMessage = new Message(model.getUniqueID(), "request vote for " 
                + model.getUniqueID() , 2);
        
        // @TODO Send election message with own ID to next process in ring system
    }
}
