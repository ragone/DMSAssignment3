package model;

public class Message {
    private String type;
    private String content;

    public Message(String message) {
        content = message;
    }
    
    public String getContent() {
        return content;
    }
}
