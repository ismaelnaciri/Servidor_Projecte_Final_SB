package cat.insvidreres.imp.m13projecte.entities;

import java.util.ArrayList;
import java.util.List;

public class Chat {
    private String id;
    private List<User> users;
    private String lastMessage;
    private String lastMessageDate;


    public Chat() {
    }

    public Chat(String id, List<User> users, String lastMessage, String lastMessageDate) {
        this.id = id;
        this.users = users != null ? users : new ArrayList<>();
        this.lastMessage = lastMessage;
        this.lastMessageDate = lastMessageDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageDate() {
        return lastMessageDate;
    }

    public void setLastMessageDate(String lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
    }
}
