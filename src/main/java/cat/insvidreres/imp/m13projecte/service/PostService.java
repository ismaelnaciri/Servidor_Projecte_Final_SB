package cat.insvidreres.imp.m13projecte.service;


import cat.insvidreres.imp.m13projecte.entities.Comment;
import cat.insvidreres.imp.m13projecte.entities.Post;

import cat.insvidreres.imp.m13projecte.utils.JSONResponse;
import cat.insvidreres.imp.m13projecte.utils.Utils;

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
}




//    private static final String COLLECTION_NAME = "posts";
//
//
//    public String savePost(Post post) throws InterruptedException, ExecutionException {
//        ApiFuture<WriteResult> collectionApiFuture = null;
//
//        try {
//            collectionApiFuture = DB_FIRESTORE.collection(CollectionName.POST.toString()).document(post.getDocName()).set(post);
//
//            return collectionApiFuture.get().getUpdateTime().toString();
//        } catch (Exception e) {
//            System.out.println("ERROR | " + e.getMessage());
//            e.printStackTrace();
//
//            return "Error whilst saving post";
//        }
//    }
//
//    public String deletePost(String docName) throws InterruptedException, ExecutionException {
//        ApiFuture<WriteResult> collectionApiFuture = null;
//
//        try {
//            collectionApiFuture = DB_FIRESTORE.collection(CollectionName.POST.toString()).document(docName).delete();
//
//            return collectionApiFuture.get().getUpdateTime().toString();
//        } catch (Exception e ) {
//            System.out.println("ERROR DELETING POST | " + e.getMessage());
//            e.printStackTrace();
//
//            return "Error whilst deleting post";
//        }
//    }
//
//    public String updateUser(Post post) {
//        ApiFuture<WriteResult> collectionApiFuture = null;
//
//        try {
//            collectionApiFuture = DB_FIRESTORE.collection(CollectionName.POST.toString()).document(post.getDocName()).set(post);
//
//            return collectionApiFuture.get().getUpdateTime().toString();
//        } catch (ExecutionException | InterruptedException e) {
//            System.out.println("ERROR | " + e.getMessage());
//            e.printStackTrace();
//
//            return "Error whilst updating post";
//        }
//    }