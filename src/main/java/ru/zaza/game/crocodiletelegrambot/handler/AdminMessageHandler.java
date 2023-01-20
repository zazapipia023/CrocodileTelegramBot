package ru.zaza.game.crocodiletelegrambot.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.zaza.game.crocodiletelegrambot.services.ChatService;

@Component
public class AdminMessageHandler {

    private final ChatService chatService;

    @Autowired
    public AdminMessageHandler(ChatService chatService) {
        this.chatService = chatService;
    }

    public SendMessage handleMessage(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());

        sendMessage.setText("Unknown command");

        return sendMessage;
    }
}
