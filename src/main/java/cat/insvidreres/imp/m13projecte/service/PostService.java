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
//                Collections.sort(post.getCategories());
                Map<String, Object> postData = new HashMap<>();
                postData.put("id", post.getId());
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

            System.out.println("categories received | " + categoriesArr);

            future = dbFirestore.collection(CollectionName.POST.toString()).get();

            Set<String> postIdsAdded = new HashSet<>();

            future.get().forEach((doc) -> {
                List<String> postCategories = (List<String>) doc.getData().get("categories");
                categoriesArr.forEach((item) -> {
                    for (String category : postCategories) {
                        if (category.trim().equals(item.trim())) {
                            Post post = doc.toObject(Post.class);
                            String postIdFirebase = (String) doc.getData().get("id");
                            // Only add the post if its ID has not been added yet
                            if (!postIdsAdded.contains(postIdFirebase)) {
                                dataToShow.add(post);
                                postIdsAdded.add(postIdFirebase);
                                System.out.println("added " + post.getId() + " | " + post.getDescription() + " correctly!");
                            }
                        }
                    }
                });
            });

            dataToShow.forEach((post) -> {
                System.out.println("Post email | " + ((Post) post).getEmail() + " | Post description | " + ((Post) post).getDescription());
            });

            return generateResponse(
                    200,
                    LocalDateTime.now().toString(),
                    "Retreived posts correctly",
                    dataToShow
            );

        } catch (Exception e) {
            System.out.println("Error | " + e.getMessage());
            e.printStackTrace();
            return generateResponse(
                    404,
                    LocalDateTime.now().toString(),
                    "No posts found with those categories | " + e.getMessage(),
                    null
            );
        }
    }

    public JSONResponse addCommentPost(Comment comment, String idToken, String idPost) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
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

            DocumentReference postRef = dbFirestore.collection("posts").document(idPost);
            DocumentSnapshot postSnapshot = postRef.get().get();

            if (postSnapshot.exists()) {
                Map<String, Object> postData = postSnapshot.getData();

                List<Map<String, Object>> commentsList = (List<Map<String, Object>>) postData.getOrDefault("comments", new ArrayList<>());

                Map<String, Object> commentData = new HashMap<>();
                commentData.put("email", comment.getEmail());
                commentData.put("comment", comment.getComment());
                commentData.put("commentAt", comment.getCommentAt());
                commentData.put("likes", comment.getLikes());
                commentData.put("id", comment.getId());
                commentsList.add(commentData);

                postData.put("comments", commentsList);
                postRef.set(postData);

                dataToShow.add(postData);

                System.out.println("COMMENT ADDED SUCCESSFULLY");
                return generateResponse(200, LocalDateTime.now().toString(), "Comment added successfully", dataToShow);
            } else {
                return generateResponse(404, LocalDateTime.now().toString(), "Post with ID " + idPost + " not found", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return generateResponse(500, LocalDateTime.now().toString(), "ERROR WHILE ADDING COMMENT | " + e.getMessage(), null);
        }
    }


    public JSONResponse addLikePost(String idToken, String idPost, String email) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        List<Object> dataToShow = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = null;

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
            future = dbFirestore.collection("posts").whereEqualTo("id", idPost).get();

            future.get().forEach((doc) -> {
                Map<String, Object> postData = doc.getData();

                List<String> likesList = (List<String>) postData.getOrDefault("likes", new ArrayList<>());

                if (!likesList.contains(email.split("\"")[1])) {
                    System.out.println("email | " + email.split("\"")[1]);
                    likesList.add(email.split("\"")[1]);
                }
                System.out.println(likesList);

                dbFirestore.collection("posts").document(doc.getId()).update("likes", likesList);
                dataToShow.add(postData);
                System.out.println("LIKE INSERTED SUCCESSFULLY");

            });

            return generateResponse(200, LocalDateTime.now().toString(), "Like inserted successfully", dataToShow);

        } catch (Exception e) {
            e.printStackTrace();
            return generateResponse(500, LocalDateTime.now().toString(), "ERROR WHILE INSERTING LIKE | " + e.getMessage(), null);
        }
    }


    public JSONResponse deleteLikePost(String idToken, String idPost, String email) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        List<Object> dataToShow = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = null;

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
            future = dbFirestore.collection("posts").whereEqualTo("id", idPost).get();

            future.get().forEach((doc) -> {
                Map<String, Object> postData = doc.getData();

                List<String> likesList = (List<String>) postData.getOrDefault("likes", new ArrayList<>());
                likesList.remove(email);
                System.out.println(likesList);

                dbFirestore.collection("posts").document(doc.getId()).update("likes", likesList);
                dataToShow.add(postData);
                System.out.println("LIKE REMOVED SUCCESSFULLY");

            });

            return generateResponse(200, LocalDateTime.now().toString(), "Like removed successfully", dataToShow);

        } catch (Exception e) {
            e.printStackTrace();
            return generateResponse(500, LocalDateTime.now().toString(), "ERROR WHILE REMOVING LIKE | " + e.getMessage(), null);
        }
    }


    public JSONResponse addLikeCommentPost(String idToken, String idPost, String idComment, String email) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        List<Object> dataToShow = new ArrayList<>();

        try {
            FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(idToken);
            if (token == null) {
                return generateResponse(401, LocalDateTime.now().toString(), "User token not found!", null);
            }
        } catch (FirebaseAuthException e) {
            System.out.println("Error getting token | " + e.getMessage());
            return generateResponse(500, LocalDateTime.now().toString(), "Error getting token | " + e.getMessage(), null);
        }

        try {
            DocumentReference postRef = dbFirestore.collection("posts").document(idPost);
            DocumentSnapshot postSnapshot = postRef.get().get();

            if (postSnapshot.exists()) {
                Map<String, Object> postData = postSnapshot.getData();

                List<Map<String, Object>> commentsList = (List<Map<String, Object>>) postData.getOrDefault("comments", new ArrayList<>());


                for (Map<String, Object> comment : commentsList) {
                    if (idComment.equals(comment.get("id"))) {
                        List<String> likesList = (List<String>) comment.getOrDefault("likes", new ArrayList<>());
                        likesList.add(email.trim());
                        comment.put("likes", likesList);
                        break;
                    }
                }

                postData.put("comments", commentsList);
                postRef.set(postData);

                dataToShow.add(postData);

                System.out.println("COMMENT LIKE ADDED SUCCESSFULLY");
                return generateResponse(200, LocalDateTime.now().toString(), "Like added successfully", dataToShow);
            } else {
                return generateResponse(404, LocalDateTime.now().toString(), "Post with ID " + idPost + " not found", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return generateResponse(500, LocalDateTime.now().toString(), "ERROR WHILE ADDING LIKE | " + e.getMessage(), null);
        }
    }


    public JSONResponse deleteLikeCommentPost(String idToken, String idPost, String idComment, String email) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        List<Object> dataToShow = new ArrayList<>();

        try {
            FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(idToken);
            if (token == null) {
                return generateResponse(401, LocalDateTime.now().toString(), "User token not found!", null);
            }
        } catch (FirebaseAuthException e) {
            System.out.println("Error getting token | " + e.getMessage());
            return generateResponse(500, LocalDateTime.now().toString(), "Error getting token | " + e.getMessage(), null);
        }

        try {
            DocumentReference postRef = dbFirestore.collection("posts").document(idPost);
            DocumentSnapshot postSnapshot = postRef.get().get();

            if (postSnapshot.exists()) {
                Map<String, Object> postData = postSnapshot.getData();

                List<Map<String, Object>> commentsList = (List<Map<String, Object>>) postData.getOrDefault("comments", new ArrayList<>());


                for (Map<String, Object> comment : commentsList) {
                    if (idComment.equals(comment.get("id"))) {
                        List<String> likesList = (List<String>) comment.getOrDefault("likes", new ArrayList<>());
                        likesList.remove(email.trim());
                        comment.put("likes", likesList);
                        break;
                    }
                }

                postData.put("comments", commentsList);
                postRef.set(postData);

                dataToShow.add(postData);

                System.out.println("DELETE LIKE ADDED SUCCESSFULLY");
                return generateResponse(200, LocalDateTime.now().toString(), "Like added successfully", dataToShow);
            } else {
                return generateResponse(404, LocalDateTime.now().toString(), "Post with ID " + idPost + " not found", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return generateResponse(500, LocalDateTime.now().toString(), "ERROR WHILE ADDING LIKE | " + e.getMessage(), null);
        }
    }

    public JSONResponse getCategories(String idToken) {
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

            DocumentReference typeDocRef = dbFirestore.collection("categories").document("Type");
            ApiFuture<DocumentSnapshot> typeDocFuture = typeDocRef.get();
            DocumentSnapshot typeDocSnapshot = typeDocFuture.get();

            if (typeDocSnapshot.exists() && typeDocSnapshot.contains("categories")) {
                List<String> categories = (List<String>) typeDocSnapshot.get("categories");

                dataToShow.addAll(categories);
            } else {
                return generateResponse(
                        404,
                        LocalDateTime.now().toString(),
                        "Categories not found!",
                        null
                );
            }


            return generateResponse(200, LocalDateTime.now().toString(), "Posts retrieved", dataToShow);
        } catch (Exception e) {
            e.printStackTrace();
            return generateResponse(500, LocalDateTime.now().toString(), "ERROR WHILE RETRIEVING POSTS", null);
        }
    }


}





