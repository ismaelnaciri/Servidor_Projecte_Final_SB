package cat.insvidreres.imp.m13projecte.entities;

public class Post {

    private String id;
    private String email;
    private String createdAT;
    private String description;
    private String[] images;
    private String[] category;
    private String[] likes;
    private Comment[] comments;

    public Post(String id, String email, String createdAT, String description, String[] images, String[] category, String[] likes, Comment[] comments) {
        this.id = id;
        this.email = email;
        this.createdAT = createdAT;
        this.description = description;
        this.images = images;
        this.category = category;
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

    public String[] getImages() {
        return images;
    }

    public void setImages(String[] images) {
        this.images = images;
    }

    public String[] getCategory() {
        return category;
    }

    public void setCategory(String[] category) {
        this.category = category;
    }

    public String[] getLikes() {
        return likes;
    }

    public void setLikes(String[] likes) {
        this.likes = likes;
    }

    public Comment[] getComments() {
        return comments;
    }

    public void setComments(Comment[] comments) {
        this.comments = comments;
    }
}