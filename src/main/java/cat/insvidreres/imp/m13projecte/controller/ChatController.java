package cat.insvidreres.imp.m13projecte.controller;

import cat.insvidreres.imp.m13projecte.entities.Chat;
import cat.insvidreres.imp.m13projecte.entities.Message;
import cat.insvidreres.imp.m13projecte.service.ChatService;
import cat.insvidreres.imp.m13projecte.utils.JSONResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;

    //Write
    @MessageMapping("/chat/{chatId}/sendToServer")
    @SendTo("/chat/{chatId}/sendResponseToClient")
    public JSONResponse sendChatMessage(@Payload Message message, @DestinationVariable String chatId) {
        return chatService.sendMessage(chatId, message);
    }


    //Read
    @MessageMapping("/chat/{chatId}/getMessagesToServer")
    @SendTo("/chat/{chatId}/getMessagesResponseToClient")
    public JSONResponse getMessagesToServer(@DestinationVariable String chatId) {
        return chatService.getChatMessages(chatId);
    }


    //Check - Add chat doc to collection
    @MessageMapping("/chat/{chatId}/checkChatRoomToServer")
    @SendTo("/chat/{chatId}/checkChatRoomResponseToClient")
    public JSONResponse checkChatRoomToServer(@DestinationVariable String chatId, @Payload Chat chat) {
        return chatService.createChatRoom(chat);
    }
}

