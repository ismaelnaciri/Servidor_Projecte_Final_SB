package cat.insvidreres.imp.m13projecte.service;

import cat.insvidreres.imp.m13projecte.entities.User;
import cat.insvidreres.imp.m13projecte.utils.JSONResponse;
import cat.insvidreres.imp.m13projecte.utils.Utils;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;
//import com.google.firestore.v1.WriteResult;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class UserService implements Utils {
    private static final String COLLECTION_NAME = "users";

    public JSONResponse saveUser(User user) throws InterruptedException, ExecutionException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> collectionApiFuture = null;

        List<Object> dataToShow = new ArrayList<>();
        AtomicReference<Boolean> errorEncrypting = new AtomicReference<>(false);


        try {

            if (user.getPassword().contains(":")) {
                Map<String, Object> response = new HashMap<>();
                response.put("password", user.getPassword());

                dataToShow.add(response);
                return generateResponse(401,
                        LocalDateTime.now().toString(),
                        "':' is not allowed in the password",
                        dataToShow);
            }
            //Auth fields
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(user.getEmail())
                    .setUid(user.getEmail())
                    .setEmailVerified(false)
                    .setPassword(                    //Hash + Salt
                            encryptPassword(
                                    user.getPassword(),
                                    Utils.SALT
                            )
                    )
                    .setPhoneNumber(user.getPhoneNumber())
                    .setDisplayName(user.getFirstName());

            //Firestore pw field encrypted
            user.setPassword(
                    encryptPassword(
                            user.getPassword(),
                            Utils.SALT
                    )
            );

            try {
                UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
                System.out.println("Successfully created new user: " + userRecord.getUid());
            } catch (Exception e) {
                return generateResponse(401,
                        LocalDate.now().toString(),
                        "Error in creating user!: " + e.getMessage(),
                        null);
            }

            collectionApiFuture = dbFirestore.collection(CollectionName.USER.toString()).whereEqualTo("email", user.getEmail()).get();

            if (collectionApiFuture.isDone() && !collectionApiFuture.get().isEmpty()) {

                collectionApiFuture.get().forEach((doc) -> {
                    if (Objects.equals(doc.get("email"), user.getEmail())) {
                        dataToShow.add(user);

                        try {
                            user.setPassword(
                                    encryptPassword(
                                            user.getPassword(),
                                            Utils.SALT
                                    )
                            );

                            updateUser(user);
                        } catch (NoSuchAlgorithmException e) {
                            errorEncrypting.set(true);
                        }
                    }
                });
            } else {
                try {
                    user.setPassword(
                            encryptPassword(
                                    user.getPassword(),
                                    Utils.SALT
                            )
                    );

                    Map<String, Object> userToInsert = new HashMap<>();
                    userToInsert.put("firstName", user.getFirstName());
                    userToInsert.put("lastName", user.getLastName());
                    userToInsert.put("age", user.getAge());
                    userToInsert.put("password", user.getPassword());
                    userToInsert.put("email", user.getEmail());

                    dataToShow.add(userToInsert);
                    dbFirestore.collection(CollectionName.USER.toString()).add(userToInsert);
                } catch (NoSuchAlgorithmException e) {
                    errorEncrypting.set(true);
                }
            }

            if (errorEncrypting.get()) {
                return generateResponse(500,
                        LocalDateTime.now().toString(),
                        "Error encrypting the password",
                        dataToShow);
            }

            return generateResponse(200,
                    LocalDateTime.now().toString(),
                    "Successfully created new user",
                    dataToShow);

        } catch (Exception e) {
            System.out.println("ERROR | " + e.getMessage());

            return generateResponse(500,
                    LocalDateTime.now().toString(),
                    "ERROR WHILST CREATING USER",
                    null);
        }

    }

    public JSONResponse deleteUser(String email) throws InterruptedException, ExecutionException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> collectionApiFuture = null;

        try {

            collectionApiFuture = dbFirestore.collection(CollectionName.USER.toString()).whereEqualTo("email", email).get();

            if (collectionApiFuture.isDone()) {

                collectionApiFuture.get().forEach((doc) -> {
                    if (Objects.equals(doc.get("email"), email)) {

                        dbFirestore.collection(CollectionName.USER.toString()).document(doc.getId()).delete();
                    }
                });
            }

            return generateResponse(200,
                    LocalDateTime.now().toString(),
                    "User deleted successfully!",
                    null);
        } catch (Exception e) {
            System.out.println("ERROR DELETING USER | " + e.getMessage());

            return generateResponse(500,
                    LocalDateTime.now().toString(),
                    "ERROR whilst deleting user",
                    null);
        }
    }

    public JSONResponse updateUser(User user) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> collectionApiFuture = null;
        List<Object> dataToShow = new ArrayList<>();


        try {
            collectionApiFuture = dbFirestore.collection(CollectionName.USER.toString()).whereEqualTo("email", user.getEmail()).get();

            if (collectionApiFuture.isDone()) {

                collectionApiFuture.get().forEach((doc) -> {
                    if (Objects.equals(doc.get("email"), user.getEmail())) {
                        dataToShow.add(user);

                        Map<String, Object> updates = new HashMap<>();

                        if (!Objects.equals(doc.get("firstName"), user.getFirstName())) {
                            updates.put("firstName", user.getFirstName());
                        }

                        if (!Objects.equals(doc.get("lastName"), user.getLastName())) {
                            updates.put("lastName", user.getLastName());
                        }

                        if (!Objects.equals(doc.get("age"), user.getAge())) {
                            updates.put("age", user.getAge());
                        }

                        if (doc.get("password") != null) {
                            String fbPw = Objects.requireNonNull(doc.get("password")).toString();
                            fbPw = decodePassword(fbPw);

                            //Remove salt from pw
                            fbPw = fbPw.split(":")[0];

                            if (!Objects.equals(fbPw, user.getPassword())) {
                                updates.put("password", user.getPassword());
                            }
                        }

                        if (!Objects.equals(doc.get("email"), user.getEmail())) {
                            updates.put("email", user.getEmail());
                        }

                        dbFirestore.collection(CollectionName.USER.toString()).document(doc.getId()).update(updates);
                    }
                });
            }

            return generateResponse(200,
                    LocalDateTime.now().toString(),
                    "User updated successfully!",
                    dataToShow
            );
        } catch (ExecutionException | InterruptedException e) {
            System.out.println("ERROR | " + e.getMessage());

            return generateResponse(500,
                    LocalDateTime.now().toString(),
                    "ERROR WHILST UPDATING USER",
                    null);
        }
    }

    public JSONResponse getUserDetails(String email) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> collectionApiFuture = null;
        List<Object> dataToShow = new ArrayList<>();

        try {
            collectionApiFuture = dbFirestore.collection(CollectionName.USER.toString()).whereEqualTo("email", email).get();

            if (collectionApiFuture.isDone()) {
                collectionApiFuture.get().forEach((doc) -> {
                    if (Objects.equals(doc.get("email"), email)) {

                        String fbPw = Objects.requireNonNull(doc.get("password")).toString();
                        fbPw = decodePassword(fbPw);
                        User userToShow = doc.toObject(User.class);
                        userToShow.setPassword(fbPw);
                        dataToShow.add(userToShow);
                    }
                });
            }

            return generateResponse(403,
                    LocalDateTime.now().toString(),
                    "Wrong email. Check again.",
                    dataToShow);

        } catch (Exception e) {
            return generateResponse(500,
                    LocalDateTime.now().toString(),
                    "Error in getting the user details. Please contact support for further infromation.",
                    null);
        }
    }

    public JSONResponse testSaltHashGet(String email, String password) throws ExecutionException, InterruptedException, NoSuchAlgorithmException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> collectionApiFuture = null;
        List<Object> dataToShow = new ArrayList<>();

        AtomicReference<String> paramPW = new AtomicReference<>("");


        try {
            collectionApiFuture = dbFirestore.collection(CollectionName.USER.toString()).whereEqualTo("email", email).get();

            if (collectionApiFuture.isDone()) {
                collectionApiFuture.get().forEach((doc) -> {
                    if (Objects.equals(doc.get("email"), email)) {
                        String salt = Utils.SALT;

                        try {
                            paramPW.set(encryptPassword(password, salt));

                            System.out.println("doc EXISTS in test :)");

                            String pw = Objects.requireNonNull(doc.get("password")).toString();
                            pw = encryptPassword(pw, salt);

                            System.out.println("pw from doc  |  " + pw);
                            if (Objects.equals(pw, paramPW.get())) {
                                dataToShow.add(doc.toObject(User.class));

                            }
                        } catch (NoSuchAlgorithmException e) {
                            generateResponse(500,
                                    LocalDateTime.now().toString(),
                                    e.getMessage(),
                                    null);
                        }
                    }
                });

                return generateResponse(200,
                        LocalDateTime.now().toString(),
                        "User gotten with hash correctly",
                        dataToShow);

            }
        } catch (Exception e) {
            return generateResponse(500,
                    LocalDateTime.now().toString(),
                    e.getMessage(),
                    null);
        }


        return generateResponse(420,
                LocalDateTime.now().toString(),
                "Thrown in saltHashGet, look into it",
                null);
    }

    public JSONResponse getUsers() throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        List<Object> dataToShow = new ArrayList<>();

        try {
            Iterable<DocumentReference> documentReference = dbFirestore.collection(CollectionName.USER.toString()).listDocuments();
            Iterator<DocumentReference> iterator = documentReference.iterator();

            if (iterator != null) {

                while (iterator.hasNext()) {
                    DocumentReference dr = iterator.next();
                    ApiFuture<DocumentSnapshot> future = dr.get();
                    DocumentSnapshot doc = future.get();

                    User user = doc.toObject(User.class);
                    dataToShow.add(user);
                }

                return generateResponse(200,
                        LocalDateTime.now().toString(),
                        "Users retreived correctly.",
                        dataToShow);
            }
        } catch (Exception e) {

            return generateResponse(500,
                    LocalDateTime.now().toString(),
                    e.getMessage(),
                    null);
        }

        return generateResponse(500,
                LocalDateTime.now().toString(),
                "ERROR ERROR ERROR",
                null);
    }


    public JSONResponse generateResponse(int code, String date, String message, List<Object> data) {
        if (data == null) {
            return new JSONResponse(code, date, message);
        } else {
            return new JSONResponse(code, date, message, data);
        }
    }
}
