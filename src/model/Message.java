package model;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
    public static int BROADCAST = 1;
    public static int ELECTION = 2; // for Chang Roberts leader election
    public static int LEADER = 3; // for Chang Roberts leader election
    
    private int type;
    private String content;
    private Date date;
    

    public Message(String message, int type) {
        content = message;
        this.type = type;
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
}
