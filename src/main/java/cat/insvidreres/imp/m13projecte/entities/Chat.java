package cat.insvidreres.imp.m13projecte.entities;

import java.util.ArrayList;
import java.util.List;

public class Chat {
    private String id;
    private List<String> users;
    private List<String> userIds;
    private String lastMessage;
    private String lastMessageDate;


    public Chat() {
    }

    public Chat(String id, List<String> users, List<String> userIds, String lastMessage, String lastMessageDate) {
        this.id = id;
        this.users = users;
        this.userIds = userIds;
        this.lastMessage = lastMessage;
        this.lastMessageDate = lastMessageDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
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
