package cat.insvidreres.imp.m13projecte.service;

import cat.insvidreres.imp.m13projecte.entities.Chat;
import cat.insvidreres.imp.m13projecte.entities.Message;
import cat.insvidreres.imp.m13projecte.utils.CollectionName;
import cat.insvidreres.imp.m13projecte.utils.JSONResponse;
import cat.insvidreres.imp.m13projecte.utils.Utils;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ChatService implements Utils {

    public JSONResponse getChatMessages(String chatId) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        List<Object> dataToShow = new ArrayList<>();
        ApiFuture<QuerySnapshot> chatsCollectionAPIF = null;

        try {
            chatsCollectionAPIF = dbFirestore.collection(CollectionName.CHATS.toString()).whereEqualTo("id", chatId).get();

            chatsCollectionAPIF.get().forEach((doc) -> {
                if (Objects.equals(doc.get("id"), chatId)) {
                    dbFirestore.collection(CollectionName.CHATS.toString()).document(doc.getId()).collection("messages")
                            .addSnapshotListener(((queryDocumentSnapshots, e) -> {
                                if (e != null) {
                                    System.out.println("Listen failed: " + e);
                                    return;
                                }

                                dataToShow.clear();

                                for (DocumentSnapshot messageDoc : queryDocumentSnapshots.getDocuments()) {
                                    // Parse the message document to POJO or add to dataToShow as needed
                                    Message message = messageDoc.toObject(Message.class);
                                    dataToShow.add(message);
                                }
                            }));
                }
            });

            return generateResponse(
                    200,
                    LocalDateTime.now().toString(),
                    "Gotten Chats",
                    dataToShow
            );

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error | " + e.getMessage());
            return generateResponse(
                    500,
                    LocalDateTime.now().toString(),
                    "Error | " + e.getMessage(),
                    null
            );
        }
    }


    public JSONResponse sendMessage(String chatId, Message message) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> chatsCollectionAPIF = null;

        try {
            chatsCollectionAPIF = dbFirestore.collection(CollectionName.CHATS.toString()).whereEqualTo("id", chatId).get();

            chatsCollectionAPIF.get().forEach((doc) -> {
                if (Objects.equals(doc.get("id"), chatId)) {
                    try {
                        ApiFuture<WriteResult> writeResultApiFuture = dbFirestore.collection(CollectionName.CHATS.toString())
                                .document(doc.getId())
                                .collection("messages")
                                .document()
                                .set(message);

                        writeResultApiFuture.get();

                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Error | " + e.getMessage());
                    }
                }
            });

            return getChatMessages(chatId);


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error | " + e.getMessage());
            return generateResponse(
                    500,
                    LocalDateTime.now().toString(),
                    "Error | " + e.getMessage(),
                    null
            );
        }
    }


    public JSONResponse createChatRoom(Chat chat) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        List<Object> dataToShow = new ArrayList<>();
        ApiFuture<QuerySnapshot> chatsCollectionAPIF = null;

        try {
            chatsCollectionAPIF = dbFirestore.collection(CollectionName.CHATS.toString()).whereEqualTo("id", chat.getId()).get();

            if (chatsCollectionAPIF.get().isEmpty()) {
                dbFirestore.collection(CollectionName.CHATS.toString()).document().set(chat);
                dataToShow.add(chat);

                return generateResponse(
                        200,
                        LocalDateTime.now().toString(),
                        "Chat room created successfully!",
                        dataToShow
                );
            } else {
                return generateResponse(
                        401,
                        LocalDateTime.now().toString(),
                        "Chat Room already exists!",
                        null
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error | " + e.getMessage());
            return generateResponse(
                    500,
                    LocalDateTime.now().toString(),
                    "Created Chat Room!",
                    dataToShow
            );
        }
    }

}
