package leaderelection;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Message;
import model.RemoteObject;
import view.ClientThread;

/**
 * Chang-Roberts leader election algorithm for a distributed system where the
 * processes are arranged logically in a circular ring and each process has a
 * unique identifier. Ensures the client with the highest identifier is elected
 * leader.
 *
 * @author jaimesbooth 20150516
 * @modified jaimes 20150517 Able to access sender's UUID
 * @modified jaimes 20150522 Moved ChangRoberts logic back here
 * @TODO Handle private messages
 */
public class ChangRoberts
{
    // Client object representing the client calling or receiveing an election message
    RemoteObject client;
    
    /**
     * Constructor for ChangRoberts leader election algorithm.
     * Assigns a Client to participate in the election.
     * @param client 
     */
    public ChangRoberts(RemoteObject client)
    {
        this.client = client;
    }

    /**
     * Elects the Client with the highest Lexicographic 
     * UUID as Server (Leader). Checks the specified message for leader election 
     * messages and handles the message accordingly. 
     *
     * @param message A message which may contain leader election details
     */
    public void changRobertsReceiveMessage(Message message)
    {
        System.out.println("changRobertsReceiveMessage() Entered");
        try
        {
            if (message.getType() == Message.ELECTION)
            {
                // Election in progress so vote

                // Identify UUID of client sending election message
                String senderID = message.getSenderID();

                // Check if messageID > ownID
                // String compareTo returns positive integer if this String object 
                // lexicographically follows the argument string 
                if (senderID.compareTo(client.getUniqueID()) > 0)
                {
                    // Vote for sending client                  
                    client.setElectionParticipant(true); // This Client has now participated in the election

                    // Get neighbours UUID i.e. receiver's ID
                    String receiverID = client.getNeighbour().getUniqueID();

                    // Add neighbour as receiver for this message
                    message.setReceiverID(receiverID);

                    //Send election message (containing messageID ie. the sender's ID) to the next Client
                    client.getServer().sendMessage(message);

                    // @TODO handle if the Client's neighbour is null?
                    // might not be neccessary as election would not be needed for only one Client
                }
                // Check if messageID = ownID
                // String compareTo returns zero if the strings are equal 
                else if (senderID.compareTo(client.getUniqueID()) == 0)
                {
                    // This client elected leader
                    // Send leader message with ownID to next client in ring
                    String newSenderID = client.getUniqueID();

                    // Get neighbours UUID i.e. receiver's ID
                    String receiverID = client.getNeighbour().getUniqueID();

                    Message leaderMessage = new Message(newSenderID, receiverID,
                            "Leader message from" + senderID, Message.LEADER);
                    client.getServer().sendMessage(leaderMessage);
                    // @TODO handle if the Client's neighbour is null?

                }
                // Check if messageID < ownID
                // String compareTo returns negative int if this String object 
                // lexicographically precedes the argument string 
                else if (senderID.compareTo(client.getUniqueID()) < 0)
                {
                    // This client is a better leader

                    // If this Client hasn't participated in election
                    if (client.isElectionParticipant() == false)
                    {
                        // Send election message requesting this Client
                        // as leader.
                        startElection();
                    }
                    // Otherwise, dont forward message as already voted 
                    // for better leader
                }
            }
            else if (message.getType() == Message.LEADER)
            {
                // Leader has been elected

                // identify UUID of Client sending leader message
                client.setLeaderID(message.getSenderID());
                client.setElectionParticipant(false);

                if (client.getLeaderID().equals(client.getUniqueID()))
                {
                    client.setNewServer();
                    
                    System.out.println(client.getUniqueID() + " elected as Server");
                    
                    // Get neighbours UUID i.e. receiver's ID
                    String receiverID = client.getNeighbour().getUniqueID();

                    // Re-address message to next client in ring
                    message.setReceiverID(receiverID);

                    // Post leader message (with leader ID) to next client in ring
                    client.getServer().sendMessage(message);
                    // @TODO handle if the Client's neighbour is null?
                }
            }
            else
            {
                // Ordinary BROADCAST message received... handle as broadcast
                // Already handled in Client class
                // @TODO Remove this block if working
            }
        }
        catch (RemoteException ex)
        {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    /**
     * Starts the Chang-Roberts leader election process for the specified Client.
     * @throws java.rmi.RemoteException
     */
    public void startElection() throws RemoteException
    {
        client.setElectionParticipant(true);
        
        // Get the neighbour of this Client
        String receiverID = client.getNeighbour().getUniqueID();

        Message electionMessage = new Message(client.getUniqueID(), receiverID, "request vote for " 
                + client.getUniqueID() , Message.ELECTION);
        
        // Post election message with own ID to next process in ring system
        client.getServer().sendMessage(electionMessage);
        // @TODO handle if the Client's neighbour is null?
        
        System.out.println("Election started in startElection()");
    }
}
