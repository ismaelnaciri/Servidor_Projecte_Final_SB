package cat.insvidreres.imp.m13projecte.service;


import cat.insvidreres.imp.m13projecte.entities.Post;
import cat.insvidreres.imp.m13projecte.utils.CollectionName;
import cat.insvidreres.imp.m13projecte.utils.Utils;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class PostService implements Utils {

    private static final String COLLECTION_NAME = "posts";

    public String savePost(Post post) throws InterruptedException, ExecutionException {
        ApiFuture<WriteResult> collectionApiFuture = null;

        try {
            Firestore dbFirestore = FirestoreClient.getFirestore();
            collectionApiFuture = dbFirestore.collection(CollectionName.POST.toString()).document(post.getDocName()).set(post);

            return collectionApiFuture.get().getUpdateTime().toString();
        } catch (Exception e) {
            System.out.println("ERROR | " + e.getMessage());
            e.printStackTrace();

            return "Error whilst saving post";
        }
    }
}
