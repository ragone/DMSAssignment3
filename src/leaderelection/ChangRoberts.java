package leaderelection;

import model.Message;

/**
 * Chang-Roberts leader election algorithm for a distributed system where the
 * processes are arranged logically in a circular ring and each process has a
 * unique identifier. Ensures the client with the highest identifier is elected
 * leader.
 *
 * @author jaimesbooth 20150516
 * @modified jaimes 20150517 Able to access sender's UUID
 */
public class ChangRoberts
{

    boolean participant; // Whether this client is participating in an election
    String ownID; // The client's unique identifier (UUID).
    String leaderID; // The current leader's unique identifier (UUID).

    /**
     * Checks the received message for leader election type messages and handles
     * the message accordingly.
     *
     * @param changRobertsMessage An election or leader Message which enables
     * leader election
     */
    public void changRobertsReceiveMessage(Message changRobertsMessage)
    {

        if (changRobertsMessage.getType() == Message.ELECTION)
        {
            // Election in progress so vote

            // Identify UUID of client sending election message
            String messageID = changRobertsMessage.getSenderID();

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
            leaderID = changRobertsMessage.getSenderID();
            participant = false;

            if (leaderID.equals(ownID))
            {
                // @TODO Send leader message with leader ID to next client in ring
            }
        }
        else
        {
            // Ordinary BROADCAST message received

            // @TODO Process the message as normal: i.e Client.sendMessage()
            /*
            if (changRobertsMessage.getType() == Message.BROADCAST)
            {
                for (Map.Entry<String, LinkedList<Message>> entrySet : changRobertsMessage.entrySet())
                {
                    LinkedList<Message> value = entrySet.getValue();
                    value.push(changRobertsMessage);
                }
            }
            */
        }

    }

    /**
     * Starts the Chang-Roberts leader election algorithm from a Client.
     */
    public void changRobertsStartElection()
    {
        participant = true;

        // @TODO Send election message with own ID to next process in ring system
    }
}
