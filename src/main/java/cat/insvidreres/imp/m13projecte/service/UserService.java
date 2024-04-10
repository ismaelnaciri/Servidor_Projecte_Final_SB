package cat.insvidreres.imp.m13projecte.service;

import cat.insvidreres.imp.m13projecte.entities.GoogleLoginResponse;
import cat.insvidreres.imp.m13projecte.entities.User;
import cat.insvidreres.imp.m13projecte.utils.JSONResponse;
import cat.insvidreres.imp.m13projecte.utils.Utils;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;
//import com.google.firestore.v1.WriteResult;
import com.google.cloud.firestore.*;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class UserService implements Utils {
    private static final String COLLECTION_NAME = "users";
    private static String currentToken = "";

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

            System.out.println("password  | " + user.getPassword() + "  |  email  |  " + user.getEmail());

            String jwtToFirebase = JWTService.generateToken(user);

            if (jwtToFirebase != null || jwtToFirebase != "") {

                try {
                    URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=AIzaSyB6sjfyGU9KgP_olEaTYAJ6UmmbceWmgGs");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

//            String encryptedPassword = encryptPassword(user.getPassword(), SALT);
                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("email", user.getEmail());
                    requestBody.put("password", user.getPassword());
                    requestBody.put("displayName", user.getFirstName());
//                    requestBody.put("idToken", jwtToFirebase);
                    requestBody.put("emailVerified", false);
                    requestBody.put("disabled", false);
                    if (!Objects.equals(user.getPhoneNumber(), "")) {
                        requestBody.put("phoneNumber", user.getPhoneNumber());
                    }

                    String jsonBody = new Gson().toJson(requestBody);

                    try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
                        writer.write(jsonBody);
                        writer.flush();
                    }

                    int responseCode = conn.getResponseCode(); // Get response code
                    if (responseCode == HttpURLConnection.HTTP_OK) { // Check if response is OK
                        Gson gson = new Gson();
                        GoogleLoginResponse response = null;
                        List<String> temp = new ArrayList<>();


                        try (Scanner scanner = new Scanner(conn.getInputStream())) {
                            while (scanner.hasNextLine()) {
                                temp.add(scanner.nextLine());
                            }
                            String jsonResponse = String.join("\n", temp);
                            response = gson.fromJson(jsonResponse, GoogleLoginResponse.class);
                        }

                        //https://firebase.google.com/docs/auth/admin/verify-id-tokens#java
                        //In client getCurrentUser id, if both have the same then proceed
                        try {
                            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(response.getIdToken());
                            if (FirebaseAuth.getInstance().getUser(decodedToken.getUid()) != null) {
                                dataToShow.add(response);
                                currentToken = response.getIdToken();
                                System.out.println("CURRENT TOKEN: " + currentToken);
                                System.out.println("User created in Auth correctly !!");
                            }
                        } catch (Exception e) {
                            System.out.println("Error sussy | " + e.getMessage());

                            return generateResponse(
                                    401,
                                    LocalDateTime.now().toString(),
                                    e.getMessage(),
                                    null
                            );
                        }

                    } else {

                        System.out.println("Error: |  " + conn.getResponseCode());
                        System.out.println("Error: |  " + conn.getResponseMessage());
                        return generateResponse(
                                responseCode,
                                LocalDateTime.now().toString(),
                                "Error: " + conn.getResponseMessage(),
                                null
                        );
                    }
                } catch (Exception e) {
                    return generateResponse(
                            500,
                            LocalDateTime.now().toString(),
                            e.getMessage(),
                            null
                    );
                }
            }

//            UserRecord.CreateRequest request = new UserRecord.CreateRequest();
//
//            if (user.getPhoneNumber() != null && user.getFirstName() != null) {
//                request.setEmail(user.getEmail())
//                        .setUid(user.getEmail())
//                        .setEmailVerified(false)
//                        .setPassword(user.getPassword())
//                        .setPhoneNumber(!Objects.equals(user.getPhoneNumber(), "") ? user.getPhoneNumber() : "")
//                        .setDisplayName(user.getFirstName());
//            } else {
//                request.setEmail(user.getEmail())
//                        .setUid(user.getEmail())
//                        .setEmailVerified(false)
//                        .setPassword(
//                                encryptPassword(
//                                        user.getPassword(),
//                                        Utils.SALT
//                                )
//                        );
//            }
//
//            user.setPassword(
//                    encryptPassword(
//                            user.getPassword(),
//                            Utils.SALT
//                    )
//            );

//            try {
//                UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
//                System.out.println("Successfully created new user: " + userRecord.getUid());
//
//                String userCustomToken = FirebaseAuth.getInstance().createCustomToken(user.getEmail());
//                dataToShow.add(userCustomToken);
//
//            } catch (Exception e) {
//                return generateResponse(401,
//                        LocalDate.now().toString(),
//                        "Error in creating user!: " + e.getMessage(),
//                        null);
//            }

            collectionApiFuture = dbFirestore.collection(CollectionName.USER.toString()).whereEqualTo("email", user.getEmail()).get();

            System.out.println("FIRST STATEMENT | " + collectionApiFuture.isDone() + " |     SECOND STATEMENT   | " + !collectionApiFuture.get().isEmpty() + " |    THIRD STATEMENT   |  " + currentToken != null + " |");
            if (collectionApiFuture.isDone() && !collectionApiFuture.get().isEmpty() && currentToken != null) {

                collectionApiFuture.get().forEach((doc) -> {
                    if (Objects.equals(doc.get("email"), user.getEmail())) {
                        dataToShow.add(user);

                        System.out.println("INSIDE email equals for each statement");
                        try {
                            user.setPassword(
                                    encryptPassword(
                                            user.getPassword(),
                                            Utils.SALT
                                    )
                            );

                            user.setId(currentToken);
                            updateUser(user);
                        } catch (NoSuchAlgorithmException e) {
                            errorEncrypting.set(true);
                        }
                    }
                });
            } else {
                System.out.println("INSIDE ELSE for each statement");


                try {
                    user.setPassword(
                            encryptPassword(
                                    user.getPassword(),
                                    Utils.SALT
                            )
                    );

                    user.setId(currentToken);

                    Map<String, Object> userToInsert = new HashMap<>();
                    userToInsert.put("firstName", user.getFirstName());
                    userToInsert.put("id", user.getId());
                    userToInsert.put("lastName", user.getLastName());
                    userToInsert.put("age", user.getAge());
                    userToInsert.put("password", user.getPassword());
                    userToInsert.put("email", user.getEmail());
                    userToInsert.put("phoneNumber", user.getPhoneNumber());

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

    public JSONResponse login(User user) {
        try {
            return signInWithEmailAndPassword(user);

        } catch (Exception e) {
            return generateResponse(
                    500,
                    LocalDateTime.now().toString(),
                    e.getMessage(),
                    null
            );
        }
    }

    public JSONResponse signInWithEmailAndPassword(User user) {
        List<Object> dataToShow = new ArrayList<>();
        try {
            UserRecord userAuth = FirebaseAuth.getInstance().getUser(user.getEmail());

            if (userAuth == null) {
                return generateResponse(
                        401,
                        LocalDateTime.now().toString(),
                        "Wrong credentials.",
                        null
                );
            }

            String customUserToken = FirebaseAuth.getInstance().createCustomToken(user.getEmail());
            FirebaseToken userToken = FirebaseAuth.getInstance().verifyIdToken(customUserToken);
            currentToken = customUserToken;

            //TODO Change sign in to client side and send user token verify it with FirebaseAuth.getInstance().verifyToken()
            //https://firebase.google.com/docs/auth/admin/verify-id-tokens#java
            if (Objects.equals(userToken.getEmail(), user.getEmail())) {
                dataToShow.add(userToken);
                dataToShow.add(user);
                generateResponse(
                        200,
                        LocalDateTime.now().toString(),
                        "User logged in successfully!",
                        dataToShow
                );
            }
        } catch (Exception e) {
            return generateResponse(
                    500,
                    LocalDateTime.now().toString(),
                    e.getMessage(),
                    null
            );
        }
        return null;
    }

}
