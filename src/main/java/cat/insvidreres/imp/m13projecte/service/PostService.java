package cat.insvidreres.imp.m13projecte.service;


import cat.insvidreres.imp.m13projecte.entities.Comment;
import cat.insvidreres.imp.m13projecte.entities.Post;

import cat.insvidreres.imp.m13projecte.utils.JSONResponse;
import cat.insvidreres.imp.m13projecte.utils.Utils;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;


@Service
public class PostService implements Utils {

    public JSONResponse createPost(Map<String, Object> payload, String idToken, String category) throws IOException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        List<Object> dataToShow = new ArrayList<>();

        checkIdToken(idToken);

        Map<String, Object> innerPost = (Map<String, Object>) payload.get("post");

        List<String> categories = Arrays.stream(category.split(",")).toList();


        ArrayList<ArrayList<Integer>> imgDataList = (ArrayList<ArrayList<Integer>>) payload.get("postImages");
        List<byte[]> resultBytesArray = new ArrayList<>();

        for (ArrayList<Integer> integers : imgDataList) {
            byte[] temp = new byte[integers.size()];

            for (int j = 0; j < integers.size(); j++) {
                temp[j] = integers.get(j).byteValue();
            }
            resultBytesArray.add(temp);
        }


        List<String> photoStorageUrlList = new ArrayList<>();

        System.out.println("imgData length: " + imgDataList.size());

        for (int i = 0; i < resultBytesArray.size(); i++) {
            String customFileName = "Post/" + innerPost.get("email") + "/" + innerPost.get("id") + "-" + i + ".jpg";

            FileInputStream serviceAccount = new FileInputStream("src/main/resources/social-post-m13-firebase-adminsdk-jh74w-641114c269.json");

            Storage storage = StorageOptions.newBuilder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build()
                    .getService();

            BlobId blobId = BlobId.of("social-post-m13.appspot.com", customFileName);

            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType("image/jpeg")
                    .build();

            storage.create(blobInfo, resultBytesArray.get(i));

            String fileUrl = getDownloadUrl(storage, blobId);
            photoStorageUrlList.add(fileUrl);
            System.out.println("Link of new profile pfp: " + fileUrl);
        }


        try {

//            System.out.println("gnrsngvfsoa | " + post.getId());
            DocumentReference postRef = dbFirestore.collection("posts").document();
            DocumentSnapshot documentFirebaseExisist = postRef.get().get();

            if (documentFirebaseExisist.exists()) {
                return generateResponse(400, LocalDateTime.now().toString(), "A post with the same ID already exists", null);
            } else {
                Map<String, Object> postData = new HashMap<>();
                postData.put("id", innerPost.get("id"));
                postData.put("email", innerPost.get("email"));
                postData.put("createdAT", innerPost.get("createdAT"));
                postData.put("description", innerPost.get("description"));

                postData.put("images", photoStorageUrlList);

                postData.put("categories", categories);
                postData.put("likes", new ArrayList<>());
                postData.put("comments", new ArrayList<>());


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


    public JSONResponse createPostAndorid(String idToken, Map<String , Object> body) throws IOException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        List<Object> dataToShow = new ArrayList<>();

        Map<String, Object> innerPost = (Map<String, Object>) body.get("post");

        checkIdToken(idToken);

        ArrayList<ArrayList<Integer>> imgDataList = (ArrayList<ArrayList<Integer>>) body.get("imgData");
        List<byte[]> resultBytesArray = new ArrayList<>();

        for (ArrayList<Integer> integers : imgDataList) {
            byte[] temp = new byte[integers.size()];

            for (int j = 0; j < integers.size(); j++) {
                temp[j] = integers.get(j).byteValue();
            }
            resultBytesArray.add(temp);
        }


//        Post postToFirebase = (Post) body.get("post");
        List<String> photoStorageUrlList = new ArrayList<>();

        System.out.println("imgData length: " + imgDataList.size());

        for (int i = 0; i < resultBytesArray.size(); i++) {
            String customFileName = "Post/" + innerPost.get("email") + "/" + innerPost.get("id") + "-" + i + ".jpg";

            FileInputStream serviceAccount = new FileInputStream("src/main/resources/social-post-m13-firebase-adminsdk-jh74w-641114c269.json");

            Storage storage = StorageOptions.newBuilder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build()
                    .getService();

            BlobId blobId = BlobId.of("social-post-m13.appspot.com", customFileName);

            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType("image/jpeg")
                    .build();

            storage.create(blobInfo, resultBytesArray.get(i));

            String fileUrl = getDownloadUrl(storage, blobId);
            photoStorageUrlList.add(fileUrl);
            System.out.println("Link of new profile pfp: " + fileUrl);
        }


        try {
            DocumentReference postRef = dbFirestore.collection("posts").document();
            DocumentSnapshot documentFirebaseExisist = postRef.get().get();

            if (documentFirebaseExisist.exists()) {
                return generateResponse(400, LocalDateTime.now().toString(), "A post with the same ID already exists", null);
            } else {
                Map<String, Object> postData = new HashMap<>();
                postData.put("id", innerPost.get("id"));
                postData.put("email", innerPost.get("email"));
                postData.put("createdAT", innerPost.get("createdAT"));
                postData.put("description", innerPost.get("description"));

                postData.put("images", photoStorageUrlList);

                postData.put("categories", innerPost.get("categories"));
                postData.put("likes", innerPost.get("likes"));
                postData.put("comments", innerPost.get("comments"));

//               List<Map<String, Object>> commentsList = new ArrayList<>();
//                if (postData != null) {
//                    for (Comment comment : ) {
//                        Map<String, Object> commentData = new HashMap<>();
//                        commentData.put("email", comment.getEmail());
//                        commentData.put("comment", comment.getComment());
//                        commentData.put("commentAt", comment.getCommentAt());
//
//                        // Convert array of likes to list
//                        List<String> likesList = comment.getLikes();
//                        commentData.put("likes", likesList);
//
//                        commentsList.add(commentData);
//                    }
//                }

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

        checkIdToken(idToken);


        try {
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

        checkIdToken(idToken);

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

        checkIdToken(idToken);

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
        ApiFuture<QuerySnapshot> future = null;


        checkIdToken(idToken);

        try {

            future = dbFirestore.collection("posts").whereEqualTo("id", idPost).get();
            future.get().forEach((doc) -> {
                if (Objects.equals(doc.get("id"), idPost)) {
                    List<Map<String, Object>> commentsList = (List<Map<String, Object>>) doc.get("comments");

                    Map<String, Object> commentData = new HashMap<>();
                    commentData.put("email", comment.getEmail());
                    commentData.put("comment", comment.getComment());
                    commentData.put("commentAt", comment.getCommentAt());
                    commentData.put("likes", comment.getLikes());
                    commentData.put("id", comment.getId());

                    assert commentsList != null;
                    commentsList.add(commentData);

                    dbFirestore.collection("posts").document(doc.getId()).update("comments", commentsList);

                    dataToShow.add(comment);
                    System.out.println("COMMENT ADDED SUCCESSFULLY");

                }
            });

            if (!dataToShow.isEmpty()) {
                return generateResponse(
                        200,
                        LocalDateTime.now().toString(),
                        "Comment added successfully",
                        dataToShow
                );
            } else {
                return generateResponse(
                        404,
                        LocalDateTime.now().toString(),
                        "No post found with id " + idPost,
                        null
                );
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

        checkIdToken(idToken);

        try {
            future = dbFirestore.collection("posts").whereEqualTo("id", idPost).get();

            future.get().forEach((doc) -> {
                Map<String, Object> postData = doc.getData();

                List<String> likesList = (List<String>) postData.getOrDefault("likes", new ArrayList<>());

                System.out.println("Email received from client " + email);
//                if (!likesList.contains(email.split("\"")[1])) {
//                    System.out.println("email | " + email.split("\"")[1]);
//                    likesList.add(email.split("\"")[1]);
//                }
                likesList.add(email);
                System.out.println(likesList);

                dbFirestore.collection("posts").document(doc.getId()).update("likes", likesList);
                dataToShow.add(likesList);
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

        checkIdToken(idToken);

        try {
            future = dbFirestore.collection("posts").whereEqualTo("id", idPost).get();

            future.get().forEach((doc) -> {
                Map<String, Object> postData = doc.getData();

                List<String> likesList = (List<String>) postData.getOrDefault("likes", new ArrayList<>());
                likesList.remove(email);
                System.out.println(likesList);

                dbFirestore.collection("posts").document(doc.getId()).update("likes", likesList);
                dataToShow.add(likesList);
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
        ApiFuture<QuerySnapshot> future = null;


        System.out.println("idToken received | " + idToken);

        checkIdToken(idToken);

        try {
            future = dbFirestore.collection("posts").whereEqualTo("id", idPost).get();

            future.get().forEach((doc) -> {
                Map<String, Object> postData = doc.getData();

                List<Map<String, Object>> commentsList = (List<Map<String, Object>>) postData.getOrDefault("comments", new ArrayList<>());

                for (Map<String, Object> comment : commentsList) {
                    if (idComment.equals(comment.get("id"))) {
                        List<String> likesList = (List<String>) comment.getOrDefault("likes", new ArrayList<>());
//                        likesList.add(email.split("\"")[1]);
                        likesList.add(email);
                        comment.remove("likes");
                        comment.put("likes", likesList);
                        break;
                    }
                }

                dbFirestore.collection("posts").document(doc.getId()).update("comments", commentsList);
                dataToShow.add(commentsList);

                System.out.println("COMMENT LIKE ADDED SUCCESSFULLY");
            });

            return generateResponse(200, LocalDateTime.now().toString(), "Like added successfully", dataToShow);
        } catch (Exception e) {
            e.printStackTrace();
            return generateResponse(500, LocalDateTime.now().toString(), "ERROR WHILE ADDING LIKE | " + e.getMessage(), null);
        }
    }


    public JSONResponse deleteLikeCommentPost(String idToken, String idPost, String idComment, String email) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        List<Object> dataToShow = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = null;

        checkIdToken(idToken);

        try {
            future = dbFirestore.collection("posts").whereEqualTo("id", idPost).get();

            future.get().forEach((doc) -> {
                Map<String, Object> postData = doc.getData();

                List<Map<String, Object>> commentsList = (List<Map<String, Object>>) postData.getOrDefault("comments", new ArrayList<>());

                for (Map<String, Object> comment : commentsList) {
                    if (idComment.equals(comment.get("id"))) {
                        List<String> likesList = (List<String>) comment.getOrDefault("likes", new ArrayList<>());
                        likesList.remove(email);
                        comment.remove("likes");
                        comment.put("likes", likesList);
                        break;
                    }
                }

                dbFirestore.collection("posts").document(doc.getId()).update("comments", commentsList);
                dataToShow.add(commentsList);

                System.out.println("COMMENT LIKE DELETED SUCCESSFULLY");
            });

            return generateResponse(200, LocalDateTime.now().toString(), "Like DELETED successfully", dataToShow);
        } catch (Exception e) {
            e.printStackTrace();
            return generateResponse(500, LocalDateTime.now().toString(), "ERROR WHILE DELETING LIKE | " + e.getMessage(), null);
        }

    }

    private String getDownloadUrl(Storage storage, BlobId blobId) {
        Blob blob = storage.get(blobId);
        return blob.signUrl(525_600, java.util.concurrent.TimeUnit.MINUTES).toString();
    }

}





