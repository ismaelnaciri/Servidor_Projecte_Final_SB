package cat.insvidreres.imp.m13projecte.entities;

//import org.springframework.cloud.gcp.data.firestore.Document;

import java.util.ArrayList;
import java.util.List;

//@Document(collectionName = "users")
public class User {

    //For Spring Boot name of variables, getters and setters has to be the same that in firestore
    private String id = "";
    private String email;
    private String password;
    private String firstName;
    private String lastName = "";
    private int age = 0;
    private String phoneNumber = "";
    private String img = "https://firebasestorage.googleapis.com/v0/b/social-post-m13.appspot.com/o/placeholder_pfp.jpg?alt=media&token=4cf013bf-1afd-4c5a-8a4e-7248b5016feb";
    private List<User> friends;
    private List<User> followers;
    private List<User> following;

    public User() {

    }

    public User(String id, String email, String password, String firstName,
                String lastName, int age, String phoneNumber, String img,
                List<User> friends, List<User> followers, List<User> following) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.phoneNumber = phoneNumber;
        this.img = img;
        this.friends = friends != null ? friends : new ArrayList<>();
        this.followers = followers != null ? followers : new ArrayList<>();
        this.following = following != null ? following : new ArrayList<>();
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public List<User> getFriends() {
        return friends;
    }

    public void setFriends(List<User> friends) {
        this.friends = friends;
    }

    public List<User> getFollowers() {
        return followers;
    }

    public void setFollowers(List<User> followers) {
        this.followers = followers;
    }

    public List<User> getFollowing() {
        return following;
    }

    public void setFollowing(List<User> following) {
        this.following = following;
    }
}
