package leaderelection;

import model.Message;

/**
 * Chang-Roberts leader election algorithm for a distributed system where the
 * processes are arranged logically in a circular ring and each process has
 * unique identifier. Ensures the client with the highest identifier is elected
 * leader.
 *
 * @author jaimesbooth 20150516
 */
public class ChangRoberts
{

    boolean participant; // Whether this client is participating in an election
    String ownID; // The client's unique identifier (UUID).
    String leaderID; // The current leader's unique identifier (UUID).

    /**
     * Ensures the client with the highest identifier is elected leader.
     */
    public void changRobertsReceiveMessage(Message changRobertsMessage)
    {

        if (changRobertsMessage.getType() == Message.ELECTION)
        {
            // Election in progress so vote

            // identify UUID of client sending election message
            String messageID = changRobertsMessage.getContent(); // @TODO actually get the UUID

            // Check if messageID > ownID
            // String compareTo returns positive integer if this String object 
            // lexicographically follows the argument string 
            if (messageID.compareTo(ownID) > 0)
            {
                // Vote for other client
                participant = true;
                // @TODO Send election message with messageID to next process
            }
            // Check if messageID = ownID
            // String compareTo returns zero if the strings are equal 
            else if (messageID.compareTo(ownID) == 0)
            {
                // This client elected leader
                // @TODO Send leader message with ownID to next client in ring

            }
            // Check if messageID < ownID
            // String compareTo returns negative int if this String object 
            // lexicographically precedes the argument string 
            else if (messageID.compareTo(ownID) < 0)
            {     
                // This client is better
                if (!participant)
                {
                    changRobertsStartElection();
                }
                else
                {
                    // dont forward as already voted for better
                }

            }

        }       
        else if (changRobertsMessage.getType() == Message.LEADER)
        {
            // Leader has been elected
            
            // identify UUID of client sending leader message
            leaderID = changRobertsMessage.getContent(); // @TODO actually get the UUID
            participant = false;
            
            if (leaderID.equals(ownID))
            {
                // @TODO Send leader message with leader ID to next client in ring
            }
        }
        else
        {
            // Ordinary message received
            
            // @TODO Process the message i.e Client.sendMessage()
        }
        
    }

    /**
     * Starts the Chang-Roberts leader election algorithm from a node.
     */
    public void changRobertsStartElection()
    {
        participant = true;

        // @TODO Send election message with own ID to next process in ring in system
        
    }
}
