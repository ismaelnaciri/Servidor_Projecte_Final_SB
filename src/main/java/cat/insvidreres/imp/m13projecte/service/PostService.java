package cat.insvidreres.imp.m13projecte.service;


import cat.insvidreres.imp.m13projecte.entities.Comment;
import cat.insvidreres.imp.m13projecte.entities.Post;

import cat.insvidreres.imp.m13projecte.utils.JSONResponse;
import cat.insvidreres.imp.m13projecte.utils.Utils;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;


@Service
public class PostService implements Utils {

    public JSONResponse createPost(Post post, String idToken) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        List<Object> dataToShow = new ArrayList<>();

        try {

            FirebaseToken userToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

            //https://firebase.google.com/docs/auth/admin/verify-id-tokens#javaA
            if (userToken == null) {
                return generateResponse(
                        404,
                        LocalDateTime.now().toString(),
                        "User token not found!",
                        null
                );
            }

            System.out.println("gnrsngvfsoa | " + post.getId());
            DocumentReference postRef = dbFirestore.collection("posts").document();
            DocumentSnapshot documentFirebaseExisist = postRef.get().get();

            if (documentFirebaseExisist.exists()) {
                return generateResponse(400, LocalDateTime.now().toString(), "A post with the same ID already exists", null);
            } else {
                Collections.sort(post.getCategories());
                Map<String, Object> postData = new HashMap<>();
                postData.put("email", post.getEmail());
                postData.put("createdAT", post.getCreatedAT());
                postData.put("description", post.getDescription());
                postData.put("images", post.getImages());
                postData.put("categories", post.getCategories());
                postData.put("likes", post.getLikes());

                // Convert array of comments to list
                List<Map<String, Object>> commentsList = new ArrayList<>();
                if (post.getComments() != null) {
                    for (Comment comment : post.getComments()) {
                        Map<String, Object> commentData = new HashMap<>();
                        commentData.put("email", comment.getEmail());
                        commentData.put("comment", comment.getComment());
                        commentData.put("commentAt", comment.getCommentAt());

                        // Convert array of likes to list
                        List<String> likesList = comment.getLikes();
                        commentData.put("likes", likesList);

                        commentsList.add(commentData);
                    }
                }
                postData.put("comments", commentsList);

                postRef.set(postData);

                dataToShow.add(postData);

                System.out.println("POST CREATED SUCCESSFULLY");
                return generateResponse(200, LocalDateTime.now().toString(), "Post created", dataToShow);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return generateResponse(500, LocalDateTime.now().toString(), "ERROR WHILE CREATING POST | " + e.getMessage(), null);
        }
    }

    public JSONResponse getAllPosts(String idToken) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        List<Object> dataToShow = new ArrayList<>();

        try {
            FirebaseToken userToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            if (userToken == null) {
                return generateResponse(
                        401,
                        LocalDateTime.now().toString(),
                        "User token not found!",
                        null
                );
            }

            CollectionReference postsRef = dbFirestore.collection("posts");
            ApiFuture<QuerySnapshot> future = postsRef.get();
            QuerySnapshot querySnapshot = future.get();

            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                Map<String, Object> postData = document.getData();


                String id = (String) postData.get("id");
                String email = (String) postData.get("email");
                String createdAT = (String) postData.get("createdAT");
                String description = (String) postData.get("description");
                List<String> images = (List<String>) postData.get("images");
                List<String> category = (List<String>) postData.get("categories");
                List<String> likes = (List<String>) postData.get("likes");
                List<Comment> comments = (List<Comment>) postData.get("comments");

                Post post = new Post(id, email, createdAT, description, images, category, likes, comments);
                dataToShow.add(post);
            }

            return generateResponse(200, LocalDateTime.now().toString(), "Posts retrieved", dataToShow);
        } catch (Exception e) {
            e.printStackTrace();
            return generateResponse(500, LocalDateTime.now().toString(), "ERROR WHILE RETRIEVING POSTS", null);
        }
    }

    public JSONResponse getUserPosts(String idToken, String email) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = null;
        List<Object> dataToShow = new ArrayList<>();

        try {
            FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(idToken);
            if (token == null) {
                return generateResponse(
                        401,
                        LocalDateTime.now().toString(),
                        "User token not found!",
                        null
                );
            }
        } catch (FirebaseAuthException e) {
            System.out.println("Error getting token | " + e.getMessage());
            generateResponse(
                    500,
                    LocalDateTime.now().toString(),
                    "Error getting token | " + e.getMessage(),
                    null
            );
        }

        try {

            future = dbFirestore.collection(CollectionName.POST.toString()).whereEqualTo("email", email).get();

            future.get().forEach((doc) -> {
                if (Objects.equals(doc.get("email"), email)) {
                    Post post = doc.toObject(Post.class);
                    dataToShow.add(post);
                }
            });

            return generateResponse(
                    200,
                    LocalDateTime.now().toString(),
                    "Posts retrieved",
                    dataToShow
            );


        } catch (Exception e) {
            System.out.println("Error | " + e.getMessage());
            return generateResponse(
                    500,
                    LocalDateTime.now().toString(),
                    "ERROR WHILE GETTING POSTS | " + e.getMessage(),
                    null)
                    ;
        }
    }

    public JSONResponse getPostsWithCategories(String idToken, String categories) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = null;
        List<Object> dataToShow = new ArrayList<>();

        try {
            FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(idToken);
            if (token == null) {
                return generateResponse(
                        401,
                        LocalDateTime.now().toString(),
                        "User token not found!",
                        null
                );
            }
        } catch (FirebaseAuthException e) {
            System.out.println("Error getting token | " + e.getMessage());
            generateResponse(
                    500,
                    LocalDateTime.now().toString(),
                    "Error getting token | " + e.getMessage(),
                    null
            );
        }

        try {
            //Deconcatenate the string
            List<String> categoriesArr = List.of(categories.split(","));
            Collections.sort(categoriesArr);

            future = dbFirestore.collection(CollectionName.POST.toString()).get();

            future.get().forEach((doc) -> {
                List<String> postCategories = (List<String>) doc.getData().get("categories");

                categoriesArr.forEach((item) -> {
                    if (postCategories.contains(item)) {
                        Post post = doc.toObject(Post.class);
                        dataToShow.add(post);
                        dataToShow.add(post);
                    }
                });
            });

            return generateResponse(
                    200,
                    LocalDateTime.now().toString(),
                    "Retreived posts correctly",
                    dataToShow
            );

        } catch (Exception e) {
            System.out.println("Error | " + e.getMessage());
            return generateResponse(
                    404,
                    LocalDateTime.now().toString(),
                    "No posts found with those categories",
                    null
            );
        }
    }
}




