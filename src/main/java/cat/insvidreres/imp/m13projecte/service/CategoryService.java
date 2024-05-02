package cat.insvidreres.imp.m13projecte.service;

import cat.insvidreres.imp.m13projecte.utils.JSONResponse;
import cat.insvidreres.imp.m13projecte.utils.Utils;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService implements Utils {

    public JSONResponse getAllCategories(String idToken) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        List<Object> dataToShow = new ArrayList<>();

        try {

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
                System.out.println("Error getting token | " + e.getMessage());
                generateResponse(
                        500,
                        LocalDateTime.now().toString(),
                        "Error getting token | " + e.getMessage(),
                        null
                );
            }

            DocumentReference categoriesRef = dbFirestore.collection(CollectionName.CATEGORIES.toString()).document("Type");
            ApiFuture<DocumentSnapshot> future = categoriesRef.get();

            try {
                DocumentSnapshot document = future.get();
                if (document.exists()) {
                    List<String> categories = (List<String>) document.getData().get("categories");
                    if (categories != null) {
                        dataToShow.addAll(categories);
                    }
                    return generateResponse(
                            200,
                            LocalDateTime.now().toString(),
                            "Categories retreived correctly",
                            dataToShow);
                } else {
                    return generateResponse(
                            404,
                            LocalDateTime.now().toString(),
                            "The document does not exist!",
                            null
                    );
                }
            } catch (Exception e) {
                System.out.println("Error | " + e.getMessage());
                return generateResponse(
                        500,
                        LocalDateTime.now().toString(),
                        "ERROR WHILE GETTING POSTS | " + e.getMessage(),
                        null);
            }


        } catch (Exception e) {
            e.printStackTrace();
            return generateResponse(500, LocalDateTime.now().toString(), "ERROR WHILE RETRIEVING POSTS", null);
        }
    }
}
