package cat.insvidreres.imp.m13projecte.service;

import cat.insvidreres.imp.m13projecte.entities.Post;
import cat.insvidreres.imp.m13projecte.entities.User;
import cat.insvidreres.imp.m13projecte.utils.JSONResponse;
import cat.insvidreres.imp.m13projecte.utils.Utils;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Filter;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.auth.*;
import com.google.firebase.cloud.FirestoreClient;
import com.google.protobuf.Api;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Service
public class AdminService implements Utils {

    private static String currentToken = "";

    public JSONResponse adminGetAllAuthUsers(String idToken) {
        List<Object> dataToShow = new ArrayList<>();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> collectionApiFuture;

        checkIdToken(idToken);

        try {
            ListUsersPage listUsersPage = firebaseAuth.listUsers(null);
            for (UserRecord userRecord : listUsersPage.iterateAll()) {

                collectionApiFuture = dbFirestore.collection(Utils.CollectionName.USER.toString()).get();

                collectionApiFuture.get().forEach((doc) -> {
                    User user = new User(
                            userRecord.getUid(),
                            userRecord.getEmail(),
                            Objects.requireNonNull(doc.get("password")).toString(),
                            Objects.requireNonNull(doc.get("firstName")).toString(),
                            Objects.requireNonNull(doc.get("lastName")).toString(),
                            Integer.parseInt(Objects.requireNonNull(doc.get("age")).toString()),
                            userRecord.getPhoneNumber(),
                            Objects.requireNonNull(doc.get("img")).toString(),
                            (List<User>) Objects.requireNonNull(doc.get("friends"))
                    );

                    dataToShow.add(user);
//                    user.setId(userRecord.getUid());
//                    user.setEmail(userRecord.getEmail());
                });
            }

            return generateResponse(
                    200,
                    LocalDateTime.now().toString(),
                    "Gotten Users correctly with auth UID",
                    dataToShow
            );
        } catch (FirebaseAuthException | InterruptedException | ExecutionException e) {
            System.out.println("Error in getting the user details | " + e.getMessage());
            e.printStackTrace();
            return generateResponse(
                    500,
                    LocalDateTime.now().toString(),
                    "Error | " + e.getMessage(),
                    null
            );
        }
    }


    public JSONResponse login(String idToken, User user) {
        List<Object> dataToShow = new ArrayList<>();
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> collectionApiFuture;

        checkIdToken(idToken);

        try {
            String encryptedPassword = encryptPassword(user.getPassword(), SALT);
            System.out.println("encrypted password: " + encryptedPassword);

            collectionApiFuture = dbFirestore.collection(CollectionName.ADMINS.toString()).whereEqualTo("email", user.getEmail()).get();
            collectionApiFuture.get().forEach((doc) -> {
                if (Objects.equals(doc.get("email"), user.getEmail())
                || Objects.equals(doc.get("password"), encryptedPassword)) {
                    user.setPassword(encryptedPassword);
                    dataToShow.add(user);
                    System.out.println("found admin!!");
                }
            });

            if (!dataToShow.isEmpty()) {
                return generateResponse(
                        200,
                        LocalDateTime.now().toString(),
                        "Retrieved admins successfully",
                        dataToShow
                );
            } else {
                return generateResponse(
                        404,
                        LocalDateTime.now().toString(),
                        "No admins found with that email and password!",
                        null
                );
            }
        } catch (Exception e) {
            System.out.println("Error in getting admins | " + e.getMessage());
            e.printStackTrace();
            return generateResponse(
                    500,
                    LocalDateTime.now().toString(),
                    "Error | " + e.getMessage(),
                    null
            );
        }
    }


    public JSONResponse deleteUser(String idToken, User user) {
        List<Object> dataToShow = new ArrayList<>();
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> collectionApiFuture;

        checkIdToken(idToken);

        try {
            collectionApiFuture = dbFirestore.collection(Utils.CollectionName.USER.toString()).whereEqualTo("id", user.getId()).get();
            collectionApiFuture.get().forEach((doc) -> {
               if (Objects.equals(doc.get("id"), user.getId())) {
                   dbFirestore.collection(Utils.CollectionName.USER.toString()).document(doc.getId()).delete();

                   dataToShow.add(doc);
               }
            });

            if (!dataToShow.isEmpty()) {
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
                        "User not found",
                        null
                );
            }
        } catch (Exception e) {
            System.out.println("Error deleting user | " + e.getMessage());
            e.printStackTrace();
            return generateResponse(
                    500,
                    LocalDateTime.now().toString(),
                    e.getMessage(),
                    null
            );
        }
    }

    public JSONResponse addCategory(String idToken, String category) {
        List<Object> dataToShow = new ArrayList<>();
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<DocumentSnapshot> documentSnapshotApiFuture;

        checkIdToken(idToken);

        try {
            documentSnapshotApiFuture = dbFirestore.collection(CollectionName.CATEGORIES.toString()).document("Type").get();
            List<String> categories = (List<String>) documentSnapshotApiFuture.get().get("categories");
            if (categories != null || !categories.isEmpty()) {
                categories.add(category);
                dataToShow.add(categories);

                dbFirestore.collection(CollectionName.CATEGORIES.toString()).document("Type").update("categories", categories);

                return generateResponse(
                        200,
                        LocalDateTime.now().toString(),
                        "Category inserted correctly!",
                        dataToShow
                );
            } else {
                return generateResponse(
                        404,
                        LocalDateTime.now().toString(),
                        "Could not get categories. Please contact admin.",
                        null
                );
            }

        } catch (Exception e) {
            System.out.println("Error creating category | " + e.getMessage());
            e.printStackTrace();
            return generateResponse(
                    500,
                    LocalDateTime.now().toString(),
                    e.getMessage(),
                    null
            );
        }
    }


    public JSONResponse deleteCategory(String idToken, String category) {
        List<Object> dataToShow = new ArrayList<>();
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<DocumentSnapshot> documentSnapshotApiFuture;

        checkIdToken(idToken);

        try {
            documentSnapshotApiFuture = dbFirestore.collection(CollectionName.CATEGORIES.toString()).document("Type").get();
            List<String> categories = (List<String>) documentSnapshotApiFuture.get().get("categories");
            if (categories != null || !categories.isEmpty()) {
                categories.remove(category);
                dataToShow.add(categories);

                dbFirestore.collection(CollectionName.CATEGORIES.toString()).document("Type").update("categories", categories);

                return generateResponse(
                        200,
                        LocalDateTime.now().toString(),
                        "Category deleted correctly!",
                        dataToShow
                );
            } else {
                return generateResponse(
                        404,
                        LocalDateTime.now().toString(),
                        "Could not delete category. Please contact admin.",
                        null
                );
            }

        } catch (Exception e) {
            System.out.println("Error creating category | " + e.getMessage());
            e.printStackTrace();
            return generateResponse(
                    500,
                    LocalDateTime.now().toString(),
                    e.getMessage(),
                    null
            );
        }
    }


    public JSONResponse deletePost(String idToken, String idPost) {
        List<Object> dataToShow = new ArrayList<>();
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> collectionApiFuture;

        checkIdToken(idToken);

        try {
            collectionApiFuture = dbFirestore.collection(CollectionName.POST.toString()).get();

            collectionApiFuture.get().forEach((doc) -> {
               if (Objects.equals(doc.get("id").toString().trim(), idPost.trim())) {
                   System.out.println("Found post to delete.");
                   dataToShow.add(doc.toObject(Post.class));
                   dbFirestore.collection(CollectionName.POST.toString()).document(doc.getId()).delete();
                   System.out.println("Post deleted successfully!");
               }
            });

            if (!dataToShow.isEmpty()) {
                return generateResponse(
                        200,
                        LocalDateTime.now().toString(),
                        "Post deleted successfully!",
                        dataToShow
                );
            } else {
                return generateResponse(
                        404,
                        LocalDateTime.now().toString(),
                        "Post not found!",
                        null
                );
            }

        } catch (Exception e) {
            System.out.println("Error deleting post | " + e.getMessage());
            e.printStackTrace();
            return generateResponse(
                    500,
                    LocalDateTime.now().toString(),
                    "Error: " + e.getMessage(),
                    null
            );
        }
    }

}
