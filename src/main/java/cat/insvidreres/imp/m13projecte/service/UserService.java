package cat.insvidreres.imp.m13projecte.service;

import cat.insvidreres.imp.m13projecte.entities.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
//import com.google.firestore.v1.WriteResult;
import com.google.cloud.firestore.*;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    enum CollectionName {
        USER("users"),
        POST("posts"),
        LIKE("likes"),
        COMMENT("comments");

        private final String TEXT;

        CollectionName(final String TEXT) {
            this.TEXT = TEXT;
        }

        @Override
        public String toString() {
            return TEXT;
        }
    }
    private static final String COLLECTION_NAME = "users";

    public String saveUser(User user) throws InterruptedException, ExecutionException {
        ApiFuture<WriteResult> collectionApiFuture = null;

        try {
            Firestore dbFirestore = FirestoreClient.getFirestore();
            collectionApiFuture = dbFirestore.collection(CollectionName.USER.toString()).document(user.getFirstName()).set(user);

            return collectionApiFuture.get().getUpdateTime().toString();
            // return generateResponse(200,
            // collectionApiFuture.get().getUpdateTime().toString(), "User created
            // correctly");
        } catch (Exception e) {
            System.out.println("ERROR | " + e.getMessage());
            e.printStackTrace();

            return "Error whilst saving user";
            // return generateResponse
            // (500,
            // collectionApiFuture.get().getUpdateTime().toString(),
            // "ERROR WHILST CREATING USER")
            // .asMap();
        }

    }

    public String deleteUser(String docName) throws InterruptedException, ExecutionException {
        ApiFuture<WriteResult> collectionApiFuture = null;

        try {
            Firestore dbFirestore = FirestoreClient.getFirestore();
            collectionApiFuture = dbFirestore.collection(CollectionName.USER.toString()).document(docName).delete();

            return collectionApiFuture.get().getUpdateTime().toString();
        } catch (Exception e) {
            System.out.println("ERROR DELETING USER | " + e.getMessage());
            e.printStackTrace();

            return "Error whilst deleting user";
        }
    }

    public String updateUser(User user) {
        ApiFuture<WriteResult> collectionApiFuture = null;

        try {
            Firestore dbFirestore = FirestoreClient.getFirestore();
            collectionApiFuture = dbFirestore.collection(CollectionName.USER.toString()).document(user.getFirstName()).set(user);

            return collectionApiFuture.get().getUpdateTime().toString();
            // return generateResponse(200,
            // collectionApiFuture.get().getUpdateTime().toString(),
            // "User updated correctly");
        } catch (ExecutionException | InterruptedException e) {
            System.out.println("ERROR | " + e.getMessage());
            e.printStackTrace();

            return "Error whilst updating user";
            // return generateResponse(500,
            // collectionApiFuture.get().getUpdateTime().toString(),
            // "ERROR WHILST UPDATING USER");
        }
    }

    public User getUserDetails(String docName) throws ExecutionException, InterruptedException {

        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection(CollectionName.USER.toString()).document(docName);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot doc = future.get();

        User user = null;
        if (doc.exists()) {
            user = doc.toObject(User.class);
            return user;
        } else {
            return null;
        }
    }

    public List<User> getUsers() throws ExecutionException, InterruptedException {

        List<User> users = new ArrayList<>();
        Firestore dbFirestore = FirestoreClient.getFirestore();
        Iterable<DocumentReference> documentReference = dbFirestore.collection(COLLECTION_NAME).listDocuments();
        Iterator<DocumentReference> iterator = documentReference.iterator();

        if (iterator != null) {

            while (iterator.hasNext()) {
                DocumentReference dr = iterator.next();
                ApiFuture<DocumentSnapshot> future = dr.get();
                DocumentSnapshot doc = future.get();

                User user = doc.toObject(User.class);
                users.add(user);
            }

            return users;
        } else {
            return null;
        }
    }


    public JsonObject generateResponse(int code, String date, String message) {
        JsonObject response = new JsonObject();

        System.out.println("DATE GIVEN IN PARAMETERS : " + date);

        response.addProperty("code", code);
        System.out.println("code added | " + response.get("code"));
        response.addProperty("date", date);
        System.out.println("date added | " + response.get("date"));
        response.addProperty("message", message);
        System.out.println("message added | " + response.get("message"));

        return response;
    }
}
