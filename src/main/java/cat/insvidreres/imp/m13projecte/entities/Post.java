package cat.insvidreres.imp.m13projecte.entities;

import java.util.ArrayList;
import java.util.List;

public class Post {

    private String id;
    private String email;
    private String createdAT;
    private String description;
    private List<String> images;
    private List<String> categories;
    private List<String> likes;
    private List<Comment> comments;

    public Post() {
    }

    public Post(String id, String email, String createdAT, String description, List<String> images, List<String> categories, List<String> likes, List<Comment> comments) {
        this.id = id;
        this.email = email;
        this.createdAT = createdAT;
        this.description = description;
        this.images = images;
        this.categories = categories;
        this.likes = likes;
        this.comments = comments;
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

    public String getCreatedAT() {
        return createdAT;
    }

    public void setCreatedAT(String createdAT) {
        this.createdAT = createdAT;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public List<String> getCategories() {
        if (categories == null) {
            categories = new ArrayList<>();
        }
        return categories;
    }
    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getLikes() {
        return likes;
    }

    public void setLikes(List<String> likes) {
        this.likes = likes;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}