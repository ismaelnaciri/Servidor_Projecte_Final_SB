package cat.insvidreres.imp.m13projecte.service;

import cat.insvidreres.imp.m13projecte.entities.Chat;
import cat.insvidreres.imp.m13projecte.entities.Message;
import cat.insvidreres.imp.m13projecte.utils.CollectionName;
import cat.insvidreres.imp.m13projecte.utils.JSONResponse;
import cat.insvidreres.imp.m13projecte.utils.Utils;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ChatService implements Utils {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public JSONResponse getChatMessages(String chatId) {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        try {
            ApiFuture<QuerySnapshot> chatsCollectionAPIF = dbFirestore.collection(CollectionName.CHATS.toString()).whereEqualTo("id", chatId).get();

            chatsCollectionAPIF.get().forEach((doc) -> {
                System.out.println("Document with chatId: " + chatId + " found");
                System.out.println(doc);
                if (Objects.equals(doc.get("id"), chatId)) {
                    dbFirestore.collection(CollectionName.CHATS.toString()).document(doc.getId()).collection("messages")
                            .addSnapshotListener(((queryDocumentSnapshots, e) -> {
                                if (e != null) {
                                    System.out.println("Listen failed: " + e);
                                    return;
                                }

                                List<Object> dataToShow = new ArrayList<>();
                                for (DocumentSnapshot messageDoc : queryDocumentSnapshots.getDocuments()) {
                                    // Parse the message document to POJO or add to dataToShow as needed
                                    Message message = messageDoc.toObject(Message.class);
//                                    System.out.println(messageDoc);
                                    dataToShow.add(message);
                                }

                                // Send response to client via WebSocket
                                String destination = "/toClient/" + chatId + "/getMessagesToClient";
                                System.out.println("Sending message to client via WebSocket: " + destination);

                                messagingTemplate.convertAndSend(
                                        destination,
                                        generateResponse(
                                                200,
                                                LocalDateTime.now().toString(),
                                                "Gotten Chats",
                                                dataToShow
                                        ));

                            }));
                }
            });

            // Return an empty response here, as the response will be sent via WebSocket asynchronously
            return generateResponse(
                    200,
                    LocalDateTime.now().toString(),
                    "Listening for changes in chat messages...",
                    null
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

            if (!chatsCollectionAPIF.get().isEmpty()) {

                chatsCollectionAPIF.get().forEach((doc) -> {
                    if (Objects.equals(doc.get("id"), chatId)) {
                        try {
                            dbFirestore.collection(CollectionName.CHATS.toString()).document(doc.getId())
                                    .update("lastMessage", message.getMessage(),
                                            "lastMessageDate", message.getSentDate());

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

            } else {
                System.out.println("No chats docs found");
                return generateResponse(
                        404,
                        LocalDateTime.now().toString(),
                        "No chats found for " + chatId,
                        null
                );
            }


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

        try {
            ApiFuture<QuerySnapshot> chatsCollectionAPIF = dbFirestore.collection(CollectionName.CHATS.toString())
                    .whereArrayContains("userIds", chat.getUserIds().get(0))
                    .get();

            QuerySnapshot querySnapshot = chatsCollectionAPIF.get();
            boolean chatRoomExists = false;
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                List<String> userIds = (List<String>) doc.get("userIds");

                if (userIds != null && userIds.containsAll(chat.getUserIds())) {
                    chatRoomExists = true;
                    break;
                }
            }

            if (!chatRoomExists) {
                DocumentReference chatRoomRef = dbFirestore.collection(CollectionName.CHATS.toString()).document();
                chatRoomRef.set(chat);

                CollectionReference messagesCollection = chatRoomRef.collection("messages");

                dataToShow.add(chat);
                System.out.println("Chat added successfully with its messages sub-collection!");

                return generateResponse(
                        200,
                        LocalDateTime.now().toString(),
                        "Chat room created successfully!",
                        dataToShow
                );
            } else {
                dataToShow.add(chat);

                return generateResponse(
                        400,
                        LocalDateTime.now().toString(),
                        "Chat Room already exists!",
                        dataToShow
                );
            }

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



    public JSONResponse getUserChats(String userId) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        List<Object> dataToShow = new ArrayList<>();
        ApiFuture<QuerySnapshot> chatsCollectionAPIF = null;

        System.out.println("User id received | " + userId);

        try {
            chatsCollectionAPIF = dbFirestore.collection(CollectionName.CHATS.toString())
                    .whereArrayContains("userIds", userId)
                    .get();

            if (chatsCollectionAPIF.get().isEmpty()) {
                return generateResponse(
                        404,
                        LocalDateTime.now().toString(),
                        "No chats found!",
                        null
                );
            }

            chatsCollectionAPIF.get().forEach((doc) -> {
                try {
                    List<String> userIds = (List<String>) doc.get("userIds");
                    if (!userIds.isEmpty() || userIds.contains(userId)) {
                        Chat chat = doc.toObject(Chat.class);
                        dataToShow.add(chat);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error inside forEach loop of getUserChats | " + e.getMessage());
                }
            });

            return generateResponse(
                    200,
                    LocalDateTime.now().toString(),
                    "Gotten user chats successfully",
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
}
