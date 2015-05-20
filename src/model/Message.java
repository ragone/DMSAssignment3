package model;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
    
    public String getContent() {
        return content;
    }
    
    public String print() {
        return getTime() + content;
    }

    /**
     * @return the type
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
