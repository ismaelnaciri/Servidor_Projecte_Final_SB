package cat.insvidreres.imp.m13projecte.service;


import cat.insvidreres.imp.m13projecte.entities.Comment;
import cat.insvidreres.imp.m13projecte.entities.Post;

import cat.insvidreres.imp.m13projecte.utils.JSONResponse;
import cat.insvidreres.imp.m13projecte.utils.Utils;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
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
            DocumentReference postRef = dbFirestore.collection("posts").document(post.getId());
            DocumentSnapshot documentFirebaseExisist = postRef.get().get();

            if (documentFirebaseExisist.exists()) {
                return generateResponse(400, LocalDateTime.now().toString(), "A post with the same ID already exists", null);
            } else {
                Map<String, Object> postData = new HashMap<>();
                postData.put("email", post.getEmail());
                postData.put("createdAT", post.getCreatedAT());
                postData.put("description", post.getDescription());
                postData.put("images", Arrays.asList(post.getImages()));
                postData.put("category", Arrays.asList(post.getCategory()));
                postData.put("likes", Arrays.asList(post.getLikes()));
                postData.put("comments", Collections.emptyList());

                postRef.set(postData);

                dataToShow.add(postData);

                return generateResponse(200, LocalDateTime.now().toString(), "Post created", dataToShow);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return generateResponse(500, LocalDateTime.now().toString(), "ERROR WHILE CREATING POST", null);
        }
    }

    public JSONResponse getAllPosts(String idToken) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        List<Object> dataToShow = new ArrayList<>();

        try {
            FirebaseToken userToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            if (userToken == null) {
                return generateResponse(
                        404,
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
                String[] images = ((List<String>) postData.get("images")).toArray(new String[0]);
                String[] category = ((List<String>) postData.get("category")).toArray(new String[0]);
                String[] likes = ((List<String>) postData.get("likes")).toArray(new String[0]);

                Post post = new Post(id, email, createdAT, description, images, category, likes, null);
                dataToShow.add(post);
            }

            return generateResponse(200, LocalDateTime.now().toString(), "Posts retrieved", dataToShow);
        } catch (Exception e) {
            e.printStackTrace();
            return generateResponse(500, LocalDateTime.now().toString(), "ERROR WHILE RETRIEVING POSTS", null);
        }
    }
}




