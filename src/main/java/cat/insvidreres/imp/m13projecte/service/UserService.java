package cat.insvidreres.imp.m13projecte.service;

import cat.insvidreres.imp.m13projecte.entities.GoogleLoginResponse;
import cat.insvidreres.imp.m13projecte.entities.User;
import cat.insvidreres.imp.m13projecte.utils.JSONResponse;
import cat.insvidreres.imp.m13projecte.utils.Utils;
import com.google.api.core.ApiFuture;
import com.google.cloud.storage.Storage;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;
//import com.google.firestore.v1.WriteResult;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.StorageClient;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
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
                            updateUser(user, currentToken);
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
                    userToInsert.put("img", user.getImg());

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

    public JSONResponse deleteUser(String email, String idToken) throws InterruptedException, ExecutionException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> collectionApiFuture = null;
        List<Object> dataToShow = new ArrayList<>();

        try {

            collectionApiFuture = dbFirestore.collection(CollectionName.USER.toString()).whereEqualTo("email", email).get();

            if (collectionApiFuture.isDone()) {

                collectionApiFuture.get().forEach((doc) -> {
                    if (Objects.equals(doc.get("email"), email)) {

                        dbFirestore.collection(CollectionName.USER.toString()).document(doc.getId()).delete();
                    }
                });
            }

            FirebaseToken userToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

            if (userToken != null) {
                dataToShow.add(userToken);
                return generateResponse(
                        200,
                        LocalDateTime.now().toString(),
                        "User deleted successfully!",
                        dataToShow
                );
            } else {
                return generateResponse(
                        404,
                        LocalDateTime.now().toString(),
                        "User token not found!",
                        null
                );
            }

        } catch (Exception e) {
            System.out.println("ERROR DELETING USER | " + e.getMessage());

            return generateResponse(500,
                    LocalDateTime.now().toString(),
                    "ERROR whilst deleting user",
                    null);
        }
    }

    public JSONResponse updateUser(User user, String idToken) {
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
                            //Nbgdsvjjcxvh asidh<vbs
                        }

                        if (!Objects.equals(doc.get("email"), user.getEmail())) {
                            updates.put("email", user.getEmail());
                        }

                        dbFirestore.collection(CollectionName.USER.toString()).document(doc.getId()).update(updates);
                    }
                });
                FirebaseToken userToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

                //https://firebase.google.com/docs/auth/admin/verify-id-tokens#java
                if (userToken != null) {
                    dataToShow.add(userToken);
                    return generateResponse(
                            200,
                            LocalDateTime.now().toString(),
                            "User logged in successfully!",
                            dataToShow
                    );
                } else {
                    return generateResponse(
                            404,
                            LocalDateTime.now().toString(),
                            "User token not found!",
                            null
                    );
                }

            } else {
                return generateResponse(
                        404,
                        LocalDateTime.now().toString(),
                        "User token not found!",
                        null
                );
            }

        } catch (Exception e) {
            System.out.println("ERROR | " + e.getMessage());

            return generateResponse(500,
                    LocalDateTime.now().toString(),
                    "ERROR WHILST UPDATING USER",
                    null);
        }
    }

    public JSONResponse getUserDetails(String email, String idToken) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> collectionApiFuture = null;
        List<Object> dataToShow = new ArrayList<>();

        System.out.println("Email: " + email);


        try {
            FirebaseToken userToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

            collectionApiFuture = dbFirestore.collection(CollectionName.USER.toString()).whereEqualTo("email", email).get();

//            if (collectionApiFuture.isDone()) {
            collectionApiFuture.get().forEach((doc) -> {
                if (Objects.equals(doc.get("email"), email)) {

                    User userToShow = doc.toObject(User.class);
                    dataToShow.add(userToShow);
                }
            });


            //https://firebase.google.com/docs/auth/admin/verify-id-tokens#java
            if (userToken != null) {

                return generateResponse(
                        200,
                        LocalDateTime.now().toString(),
                        "User data gotten successfully!",
                        dataToShow
                );
            } else {
                return generateResponse(403,
                        LocalDateTime.now().toString(),
                        "Wrong email. Check again.",
                        null
                );
            }

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

    public JSONResponse getUsers(String idToken) throws ExecutionException, InterruptedException {
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
                FirebaseToken userToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

                //https://firebase.google.com/docs/auth/admin/verify-id-tokens#java
                if (userToken != null) {
                    dataToShow.add(userToken);
                    return generateResponse(200,
                            LocalDateTime.now().toString(),
                            "Users retreived correctly.",
                            dataToShow);
                } else {
                    return generateResponse(
                            403,
                            LocalDateTime.now().toString(),
                            "Error verifying user Token",
                            null
                    );
                }

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

    public JSONResponse login(String idToken) {
        try {
            return signInWithEmailAndPassword(idToken);

        } catch (Exception e) {
            return generateResponse(
                    500,
                    LocalDateTime.now().toString(),
                    e.getMessage(),
                    null
            );
        }
    }

    public JSONResponse signInWithEmailAndPassword(String idToken) {
        List<Object> dataToShow = new ArrayList<>();
        try {

            if (idToken == null || idToken.isEmpty()) {
                return generateResponse(
                        401,
                        LocalDateTime.now().toString(),
                        "Wrong credentials.",
                        null
                );
            }

            System.out.println("TOKEN   |  " + idToken);
            FirebaseToken userToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

            //https://firebase.google.com/docs/auth/admin/verify-id-tokens#java
            if (userToken != null) {
                currentToken = idToken;
                dataToShow.add(userToken);
                return generateResponse(
                        200,
                        LocalDateTime.now().toString(),
                        "User logged in successfully!",
                        dataToShow
                );
            } else {
                return generateResponse(
                        404,
                        LocalDateTime.now().toString(),
                        "Error getting user token ",
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


    public JSONResponse updateUserPFP(String idToken, Map<String, Object> body) {
        List<Object> dataToShow = new ArrayList<>();
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future;

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
            e.printStackTrace();
            return generateResponse(
                    500,
                    LocalDateTime.now().toString(),
                    "Error getting token | " + e.getMessage(),
                    null
            );
        }

        try {
            String email = (String) body.get("email");
            ArrayList<Integer> imgDataList = (ArrayList<Integer>) body.get("imgData");
            byte[] imgDataArray = new byte[imgDataList.size()];
            for (int i = 0; i < imgDataList.size(); i++) {
                imgDataArray[i] = imgDataList.get(i).byteValue();
            }

            System.out.println("email: " + email);
            System.out.println("imgData length: " + imgDataArray.length);

            String customFileName = "Users_PFP/" + email + "-pfp.jpg";

            FileInputStream serviceAccount = new FileInputStream("src/main/resources/social-post-m13-firebase-adminsdk-jh74w-641114c269.json");

            Storage storage = StorageOptions.newBuilder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build()
                    .getService();

            BlobId blobId = BlobId.of("social-post-m13.appspot.com", customFileName);

            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType("image/jpeg")
                    .build();

            storage.create(blobInfo, imgDataArray);

            String fileUrl = getDownloadUrl(storage, blobId);
            System.out.println("Link of new profile pfp: " + fileUrl);

            try {
                future = dbFirestore.collection(CollectionName.USER.toString()).whereEqualTo("email", email).get();
                future.get();

                if (future.isDone()) {
                    System.out.println("inside first if");
                    future.get().forEach((doc) -> {
                        if (Objects.equals(doc.get("email"), email)) {
                            System.out.println("HELLO?");
                            Map<String, Object> updates = new HashMap<>();

                            if (!Objects.equals(doc.get("img"), fileUrl)) {
                                updates.put("img", fileUrl);
                                dataToShow.add(fileUrl);
                            }

                            dbFirestore.collection(CollectionName.USER.toString()).document(doc.getId()).update(updates);

                            System.out.println("user " + email + " img field updated correctly");
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error | " + e.getMessage());
                return generateResponse(
                        500,
                        LocalDateTime.now().toString(),
                        e.getMessage(),
                        null
                );
            }

            return generateResponse(
                    200,
                    LocalDateTime.now().toString(),
                    "User pfp correctly updated!",
                    dataToShow);
        } catch (Exception e) {
            e.printStackTrace();
            return generateResponse(
                    500,
                    LocalDateTime.now().toString(),
                    e.getMessage(),
                    null
            );
        }
    }

    private String getDownloadUrl(Storage storage, BlobId blobId) {
        Blob blob = storage.get(blobId);
        //1 year duration of url
        return blob.signUrl(525_600, java.util.concurrent.TimeUnit.MINUTES).toString();
    }

}
