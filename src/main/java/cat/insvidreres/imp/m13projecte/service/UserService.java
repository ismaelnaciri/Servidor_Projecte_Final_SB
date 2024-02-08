package cat.insvidreres.imp.m13projecte.service;

import cat.insvidreres.imp.m13projecte.entities.User;
import cat.insvidreres.imp.m13projecte.utils.Utils;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;
//import com.google.firestore.v1.WriteResult;
import com.google.cloud.firestore.*;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Service
public class UserService implements Utils {
    private static final String COLLECTION_NAME = "users";

    public String saveUser(User user) throws InterruptedException, ExecutionException {
        ApiFuture<WriteResult> collectionApiFuture = null;

        try {

            user.setSalt(generateRandomSalt());  //Generates salt and puts it in salt field

            //Auth fields
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(user.getEmail())
                    .setUid(user.getEmail())
                    .setEmailVerified(false)
                    .setPassword(                    //Hash + Salt
                            encryptPassword(
                                    user.getPassword(),
                                    user.getSalt()
                            )
                    )
                    .setPhoneNumber(user.getPhoneNumber())
                    .setDisplayName(user.getFirstName());

            //Firestore pw field encrypted
            user.setPassword(
                    encryptPassword(
                            user.getPassword(),
                            user.getSalt()
                    )
            );


            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
            System.out.println("Successfully created new user: " + userRecord.getUid());


            Firestore dbFirestore = FirestoreClient.getFirestore();
            collectionApiFuture = dbFirestore.collection(CollectionName.USER.toString()).document(user.getEmail()).set(user);

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
            collectionApiFuture = dbFirestore.collection(CollectionName.USER.toString()).document(user.getEmail()).set(user);

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

    public User testSaltHashGet(String docName, String password) throws ExecutionException, InterruptedException, NoSuchAlgorithmException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection(CollectionName.USER.toString()).document(docName);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot doc = future.get();

        String salt = (String) doc.get("salt");

        password = encryptPassword(password, salt);

        if (doc.exists()) {
            System.out.println("doc EXISTS in test :)");

            String pw = Objects.requireNonNull(doc.get("password")).toString();
            pw = encryptPassword(pw, salt);

            System.out.println("pw from doc  |  " + pw);
            if (Objects.equals(pw, password))
                return doc.toObject(User.class);
            else return null;
        } else return null;
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
