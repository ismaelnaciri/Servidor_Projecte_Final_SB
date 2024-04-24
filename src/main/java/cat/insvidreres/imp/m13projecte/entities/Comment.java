package cat.insvidreres.imp.m13projecte.entities;

import java.util.List;

public class Comment {

    private String email;
    private String comment;
    private String commentAt;
    private List<String> likes;

    public Comment() {

    }

    public Comment(String email, String comment, String commentAt, List<String> likes) {
        this.email = email;
        this.comment = comment;
        this.commentAt = commentAt;
        this.likes = likes;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommentAt() {
        return commentAt;
    }

    public void setCommentAt(String commentAt) {
        this.commentAt = commentAt;
    }

    public List<String> getLikes() {
        return likes;
    }

    public void setLikes(List<String> likes) {
        this.likes = likes;
    }
}