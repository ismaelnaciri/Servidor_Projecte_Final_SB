package cat.insvidreres.imp.m13projecte.entities;

public class Message {

    private String senderId;
    private String sentDate;
    private String message;


    public Message(String senderId, String sentDate, String message) {
        this.senderId = senderId;
        this.sentDate = sentDate;
        this.message = message;
    }

    public Message() {
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSentDate() {
        return sentDate;
    }

    public void setSentDate(String sentDate) {
        this.sentDate = sentDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
