package model;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
    public static int PRIVATE_MESSAGE = 4;
    
    private int type;
    private String content;
    private String timestamp;
    private List receivers;
    private String sender;
    private String receiver;

    

    //private Date date;

    /**
     * Constructor for a message object which can be distributed around
     * the system.
     * @param senderUUID A String identifying the sender of the message.
     * @param message A String with the message's content.
     * @param type An integer which distinguishes the purpose of the message.
     */
    public Message(String senderUUID, String receiverUUID, String message, int type) {
        this.sender = senderUUID;
        this.receiver = receiverUUID;
        this.content = message;
    }
    
    public Message(String message, int type, String sender) {
        content = message;
        this.sender = sender;
        this.type = type;
        
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        timestamp = dateFormat.format(date);
    }
    
    public Message(String message, List receivers, String sender) {
        content = message;
        this.sender = sender;
        this.type = Message.PRIVATE_MESSAGE;
        this.receivers = receivers;
        
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        timestamp = dateFormat.format(date);
    }

    /**
     * Gets the UUID of the Client which sent this message.
     * @return String of the UUID which identifies the client that sent this message.
     */
    public String getSenderID()
    {
        return sender;
    }  

    /**
     * Gets the ID of the intended recipient of this message.
     * @return The UUID of the message recipient
     */
    public String getReceiverID()
    {
        return receiver;
    }

    /**
     * Sets the UUID of the intended recipient of the message.
     * @param receiverID The UUID of the receiving Client to set.
     */
    public void setReceiverID(String receiverID)
    {
        this.receivers.add(receiverID);
    }
    
    
    
    /**
     * Gets the content of this message.
     * @return The content of this message.
     */
    public String getContent() {
        return content;
    }
    
    public String print() {
        return getTime() + content;
    }

    /**
     * Gets the type of this message.
     * @return the type, an integer which distinguishes the purpose of the message.
     */
    public int getType() {
        return type; 
    }
    
    public String getTime() {
        return "<" + timestamp + "> ";
    }

    /**
     * @return the receiver
     */
    public List getReceivers() {
        return receivers;
    }

    /**
     * @return the sender
     */
    public String getSender() {
        return sender;
    }
}
