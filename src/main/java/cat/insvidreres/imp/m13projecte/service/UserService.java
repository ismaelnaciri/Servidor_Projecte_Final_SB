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
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.auth.*;
import com.google.firebase.cloud.FirestoreClient;
//import com.google.firestore.v1.WriteResult;
import com.google.cloud.firestore.*;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Integer.parseInt;
import static java.lang.Integer.sum;

@Service
public class UserService implements Utils {
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

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("email", user.getEmail());
                requestBody.put("password", user.getPassword()); //Password for firebase auth
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
                            System.out.println("User created with token | " + response.getIdToken());
                            currentToken = response.getIdToken();
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
                    String encryptedPassword = encryptPassword(user.getPassword(), SALT);

                    user.setId(currentToken);

                    Map<String, Object> userToInsert = new HashMap<>();
                    userToInsert.put("firstName", user.getFirstName());
                    userToInsert.put("id", user.getId());
                    userToInsert.put("lastName", user.getLastName());
                    userToInsert.put("age", user.getAge());
                    userToInsert.put("password", encryptedPassword);
                    userToInsert.put("email", user.getEmail());
                    userToInsert.put("phoneNumber", user.getPhoneNumber());
                    userToInsert.put("img", user.getImg());
                    userToInsert.put("friends", user.getFriends());

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

        checkIdToken(idToken);

        try {

            collectionApiFuture = dbFirestore.collection(CollectionName.USER.toString()).whereEqualTo("email", email).get();

            if (collectionApiFuture.isDone()) {

                collectionApiFuture.get().forEach((doc) -> {
                    if (Objects.equals(doc.get("email"), email)) {

                        dbFirestore.collection(CollectionName.USER.toString()).document(doc.getId()).delete();
                    }
                });
            }

            return generateResponse(
                    200,
                    LocalDateTime.now().toString(),
                    "User deleted successfully!",
                    dataToShow
            );

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

        checkIdToken(idToken);


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

        } catch (Exception e) {
            System.out.println("ERROR | " + e.getMessage());

            return generateResponse(500,
                    LocalDateTime.now().toString(),
                    "ERROR WHILST UPDATING USER",
                    null);
        }
    }

    public JSONResponse addFollowerToUser(String idToken, User user, String email) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> collectionApiFuture = null;
        List<Object> dataToShow = new ArrayList<>();

        checkIdToken(idToken);

        //First add user into following list
        try {

            collectionApiFuture = dbFirestore.collection(CollectionName.USER.toString()).whereEqualTo("email", email).get();

            collectionApiFuture.get().forEach((doc) -> {
                if (Objects.equals(doc.get("email"), email)) {
                    User docUser = doc.toObject(User.class);

                    List<User> tempList = docUser.getFollowing();
                    tempList.add(user);

                    dbFirestore.collection(CollectionName.USER.toString()).document(doc.getId()).update("following", tempList);
                    System.out.println("User added to following correctly");
                }
            });


            //Then get user object with getDetails
            //Finally get doc where email == user.getEmail()
            JSONResponse userDetailsJSON = getUserDetails(idToken, email);
            User tempUser = (User) userDetailsJSON.getData().get(0);

            collectionApiFuture = dbFirestore.collection(CollectionName.USER.toString()).whereEqualTo("email", user.getEmail()).get();
            collectionApiFuture.get().forEach((doc) -> {
                if (Objects.equals(doc.get("email"), user.getEmail())) {
                    User docUser = doc.toObject(User.class);

                    List<User> tempList = docUser.getFollowers();
                    tempList.add(tempUser);

                    dataToShow.add(tempList);

                    dbFirestore.collection(CollectionName.USER.toString()).document(doc.getId()).update("followers", tempList);
                    System.out.println("User added to follower correctly");
                }
            });

            return generateResponse(
                    200,
                    LocalDateTime.now().toString(),
                    "Followed Correctly!",
                    dataToShow
            );

        } catch (Exception e) {
            System.out.println("Error | " + e.getMessage());
            e.printStackTrace();
            return generateResponse(500,
                    LocalDateTime.now().toString(),
                    "Error in adding the friend. Please contact support for further infromation.",
                    null);
        }
    }


    public JSONResponse deleteFollowerToUser(String idToken, String email, String userEmail) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> collectionApiFuture = null;
        List<Object> dataToShow = new ArrayList<>();

        checkIdToken(idToken);

        //First add user into following list
        try {

            collectionApiFuture = dbFirestore.collection(CollectionName.USER.toString()).whereEqualTo("email", email).get();

            collectionApiFuture.get().forEach((doc) -> {
                if (Objects.equals(doc.get("email"), email)) {
                    User docUser = doc.toObject(User.class);

                    List<User> tempList = docUser.getFollowing();
                    for (User user : tempList) {
                        if (Objects.equals(user.getEmail(), userEmail)) {
                            tempList.remove(user);
                            System.out.println("hi?");
                            break;
                        }
                    }

                    dbFirestore.collection(CollectionName.USER.toString()).document(doc.getId()).update("following", tempList);
                    System.out.println("Unfollowed correctly");
                }
            });


            //Then get user object with getDetails
            //Finally get doc where email == user.getEmail()

            collectionApiFuture = dbFirestore.collection(CollectionName.USER.toString()).whereEqualTo("email", userEmail).get();
            collectionApiFuture.get().forEach((doc) -> {
                if (Objects.equals(doc.get("email"), userEmail)) {
                    User docUser = doc.toObject(User.class);

                    List<User> tempList = docUser.getFollowers();

                    for (User user : tempList) {
                        if (Objects.equals(user.getEmail(), email)) {
                            tempList.remove(user);
                            System.out.println("bomba?");
                            break;
                        }
                    }

                    dataToShow.add(tempList);

                    dbFirestore.collection(CollectionName.USER.toString()).document(doc.getId()).update("followers", tempList);
                    System.out.println("User removed follower correctly");
                }
            });

            return generateResponse(
                    200,
                    LocalDateTime.now().toString(),
                    "Followed Correctly!",
                    dataToShow
            );

        } catch (Exception e) {
            System.out.println("Error | " + e.getMessage());
            e.printStackTrace();
            return generateResponse(500,
                    LocalDateTime.now().toString(),
                    "Error in adding the friend. Please contact support for further infromation.",
                    null);
        }
    }


    public JSONResponse addUserFriend(String idToken, String email, User userToAdd) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> collectionApiFuture = null;
        List<Object> dataToShow = new ArrayList<>();

        checkIdToken(idToken);

        System.out.println("Email: " + email);

        try {
            collectionApiFuture = dbFirestore.collection(CollectionName.USER.toString()).whereEqualTo("email", email).get();

            collectionApiFuture.get().forEach((doc) -> {
                if (Objects.equals(doc.get("email"), email)) {
                    User userToShow = doc.toObject(User.class);

                    List<User> tempList = userToShow.getFriends();
                    tempList.add(userToAdd);

                    System.out.println("Added to " + email + " | " + userToAdd.getEmail());
                    System.out.println(tempList);
                    dataToShow.add(tempList);


                    dbFirestore.collection(CollectionName.USER.toString()).document(doc.getId()).update("friends", tempList);
                    System.out.printf("\nFriend added correctly to %s!", email);
                }
            });

            JSONResponse userDetailsJSON = getUserDetails(idToken, email);
            User tempUser = (User) userDetailsJSON.getData().get(0);
            System.out.println("User to also add gotten? " + tempUser.getEmail());

            collectionApiFuture = dbFirestore.collection(CollectionName.USER.toString()).whereEqualTo("email", userToAdd.getEmail()).get();
            collectionApiFuture.get().forEach((doc) -> {
                if (Objects.equals(doc.get("email"), userToAdd.getEmail())) {
                    User userToShow = doc.toObject(User.class);

                    List<User> tempList = userToShow.getFriends();
                    tempList.add(tempUser);

                    System.out.println("Added to " + userToAdd.getEmail() + " | " + tempUser.getEmail());
                    System.out.println(tempList);

                    dbFirestore.collection(CollectionName.USER.toString()).document(doc.getId()).update("friends", tempList);
                    System.out.printf("\nFriend also added correctly to %s!", tempUser.getEmail());
                }
            });

            return generateResponse(
                    200,
                    LocalDateTime.now().toString(),
                    "Friends added successfully!",
                    dataToShow
            );
            //https://firebase.google.com/docs/auth/admin/verify-id-tokens#java

        } catch (Exception e) {
            System.out.println("Error | " + e.getMessage());
            e.printStackTrace();
            return generateResponse(500,
                    LocalDateTime.now().toString(),
                    "Error in adding the friend. Please contact support for further infromation.",
                    null);
        }
    }


    public JSONResponse deleteUserFriend(String idToken, String email, String friendEmail) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> collectionApiFuture = null;
        List<Object> dataToShow = new ArrayList<>();

        checkIdToken(idToken);

        System.out.println("Email: " + email);

        try {
            collectionApiFuture = dbFirestore.collection(CollectionName.USER.toString()).whereEqualTo("email", email).get();

            collectionApiFuture.get().forEach((doc) -> {
                if (Objects.equals(doc.get("email"), email)) {
                    User userToShow = doc.toObject(User.class);
                    List<User> tempList = userToShow.getFriends();

                    for (User user : tempList) {
                        if (Objects.equals(friendEmail, user.getEmail())) {
                            tempList.remove(user);
                            break;
                        }
                    }

                    dataToShow.add(tempList);

                    dbFirestore.collection(CollectionName.USER.toString()).document(doc.getId()).update("friends", tempList);
                    System.out.printf("\nFriend eliminated correctly to %s !", email);
                }
            });

            JSONResponse userDetailsJSON = getUserDetails(idToken, email);
            User tempUser = (User) userDetailsJSON.getData().get(0);

            collectionApiFuture = dbFirestore.collection(CollectionName.USER.toString()).whereEqualTo("email", friendEmail).get();
            collectionApiFuture.get().forEach((doc) -> {
                if (Objects.equals(doc.get("email"), friendEmail)) {
                    User userToShow = doc.toObject(User.class);

                    List<User> tempList = userToShow.getFriends();
                    for (User user : tempList) {
                        if (Objects.equals(tempUser.getEmail(), user.getEmail())) {
                            tempList.remove(user);
                            break;
                        }
                    }

                    dbFirestore.collection(CollectionName.USER.toString()).document(doc.getId()).update("friends", tempList);
                    System.out.printf("\nFriend also deleted correctly to %s!", friendEmail);
                }
            });

            return generateResponse(
                    200,
                    LocalDateTime.now().toString(),
                    "Friends deleted successfully!",
                    dataToShow
            );
            //https://firebase.google.com/docs/auth/admin/verify-id-tokens#java

        } catch (Exception e) {
            System.out.println("Error | " + e.getMessage());
            e.printStackTrace();
            return generateResponse(500,
                    LocalDateTime.now().toString(),
                    "Error in deleting the friend. Please contact support for further infromation.",
                    null);
        }
    }


    public JSONResponse getUserDetails(String idToken, String email) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> collectionApiFuture = null;
        List<Object> dataToShow = new ArrayList<>();

        checkIdToken(idToken);

        System.out.println("Email: " + email);

        try {
            collectionApiFuture = dbFirestore.collection(CollectionName.USER.toString()).whereEqualTo("email", email).get();

            collectionApiFuture.get().forEach((doc) -> {
                if (Objects.equals(doc.get("email"), email)) {
                    User userToShow = doc.toObject(User.class);
                    dataToShow.add(userToShow);
                }
            });

            return generateResponse(
                    200,
                    LocalDateTime.now().toString(),
                    "User data gotten successfully!",
                    dataToShow
            );
            //https://firebase.google.com/docs/auth/admin/verify-id-tokens#java

        } catch (Exception e) {
            return generateResponse(500,
                    LocalDateTime.now().toString(),
                    "Error in getting the user details. Please contact support for further infromation.",
                    null);
        }
    }


    public JSONResponse getUsers(String idToken) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        List<Object> dataToShow = new ArrayList<>();

        checkIdToken(idToken);

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

                return generateResponse(200,
                        LocalDateTime.now().toString(),
                        "Users retreived correctly.",
                        dataToShow);
                //https://firebase.google.com/docs/auth/admin/verify-id-tokens#java

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


    public JSONResponse login(String idToken, User user) throws ExecutionException, InterruptedException {
        List<Object> dataToShow = new ArrayList<>();
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> collectionApiFuture;

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
            String encryptedPassword = encryptPassword(user.getPassword(), SALT);

            collectionApiFuture = dbFirestore.collection(CollectionName.USER.toString()).whereEqualTo("email", user.getEmail()).get();
            collectionApiFuture.get().forEach((doc) -> {
                if (Objects.equals(doc.get("email"), user.getEmail())
                        || Objects.equals(doc.get("password"), encryptedPassword)) {

                    dataToShow.add(doc.toObject(User.class));
                    System.out.println("found user!!");
                }
            });

            if (!dataToShow.isEmpty() && user != null) {
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
                        "Error getting user token",
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

        checkIdToken(idToken);

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
