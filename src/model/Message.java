package model;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message implements Serializable {
    public static int BROADCAST = 1;
    public static int ELECTION = 2; // for Chang Roberts leader election
    public static int LEADER = 3; // for Chang Roberts leader election
    
    private int type;
    private String content;
    private String timestamp;
    
    

    public Message(String message, int type) {
        content = message;
        this.type = type;
        
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        timestamp = dateFormat.format(date);
    }
    
    public String getContent() {
        return content;
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
}
