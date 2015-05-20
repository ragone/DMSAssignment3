package model;

import java.io.Serializable;
//import java.util.Date;

/**
 * Represents a a message object which can be distributed around
 * the system.
 * @author alex
 * @modified 20150517 Jaimes added Docs, fields and getters for Chang-Roberts
 */
public class Message implements Serializable {
    public static int BROADCAST = 1;
    public static int ELECTION = 2; // for Chang Roberts leader election
    public static int LEADER = 3; // for Chang Roberts leader election
    public static int PRIVATE_MESSAGE = 4; // @TODO to be implemented if time allows
    
    private final int type;
    private final String content;
    private final String senderID; // UUID to identify the client that sent this message
    // UUID to identify the client that this message is going to
    private String receiverID;     

    //private Date date;

    /**
     * Constructor for a message object which can be distributed around
     * the system.
     * @param senderUUID A String identifying the sender of the message.
     * @param message A String with the message's content.
     * @param type An integer which distinguishes the purpose of the message.
     */
    public Message(String senderUUID, String receiverUUID, String message, int type) {
        this.senderID = senderUUID;
        this.receiverID = receiverUUID;
        this.content = message;
        this.type = type;
    }

    /**
     * Gets the UUID of the Client which sent this message.
     * @return String of the UUID which identifies the client that sent this message.
     */
    public String getSenderID()
    {
        return senderID;
    }  

    /**
     * Gets the ID of the intended recipient of this message.
     * @return The UUID of the message recipient
     */
    public String getReceiverID()
    {
        return receiverID;
    }

    /**
     * Sets the UUID of the intended recipient of the message.
     * @param receiverID The UUID of the receiving Client to set.
     */
    public void setReceiverID(String receiverID)
    {
        this.receiverID = receiverID;
    }
    
    
    
    /**
     * Gets the content of this message.
     * @return The content of this message.
     */
    public String getContent() {
        return content;
    }

    /**
     * Gets the type of this message.
     * @return the type, an integer which distinguishes the purpose of the message.
     */
    public int getType() {
        return type; 
    }
}
