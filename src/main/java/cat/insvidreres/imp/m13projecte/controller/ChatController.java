package cat.insvidreres.imp.m13projecte.controller;

import cat.insvidreres.imp.m13projecte.entities.Chat;
import cat.insvidreres.imp.m13projecte.entities.Message;
import cat.insvidreres.imp.m13projecte.service.ChatService;
import cat.insvidreres.imp.m13projecte.utils.JSONResponse;
import cat.insvidreres.imp.m13projecte.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@Controller
//@CrossOrigin(origins = "*")
public class ChatController implements Utils {

    @Autowired
    private ChatService chatService;

//    @Autowired
//    private SimpMessagingTemplate messagingTemplate;

    //Write
    @MessageMapping("/chat/{chatId}/sendToServer")
    @SendTo("/toClient/{chatId}/sendResponseToClient")
    public JSONResponse sendChatMessage(@Payload Map<String, Object> payload, @DestinationVariable String chatId) {
        Message message = convertPayloadToMessage(payload);
        return chatService.sendMessage(chatId, message);
    }


    //Read
    @MessageMapping("/chat/{chatId}/getMessagesToServer")
    @SendTo("/toClient/{chatId}/getMessagesToClient")
    public JSONResponse getMessagesToServer(@DestinationVariable String chatId) {
        return chatService.getChatMessages(chatId);
    }


    //Check - Add chat doc to collection
    @MessageMapping("/chat/checkChatRoomToServer")
    @SendTo("/toClient/checkChatRoomToClient")
    public JSONResponse checkChatRoomToServer(@Payload Map<String, Object> payload) {
        Chat chat = convertPayloadToChat(payload);
        return chatService.createChatRoom(chat);
    }


    //Get User Chats
    @MessageMapping("/chat/myChatsToServer")
    @SendTo("/toClient/myChatsToClient")
    public JSONResponse getMyChats(@Payload Map<String, String> payload) {
        String userId = payload.get("userId");
        return chatService.getUserChats(userId);
    }


//    public void sendMessageToClient(String destination, Object payload) {
//        messagingTemplate.convertAndSend(destination, payload);
//    }
}

