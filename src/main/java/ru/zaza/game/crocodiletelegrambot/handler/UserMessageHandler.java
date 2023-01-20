package ru.zaza.game.crocodiletelegrambot.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class UserMessageHandler {

    // TODO: UserService should be here

    public SendMessage handleMessage(Message message) {
        SendMessage sendMessage = new SendMessage(String.valueOf(message.getChatId()), "Add me in group chat.");
        return sendMessage;
    }
}
