package ru.zaza.game.crocodiletelegrambot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.zaza.game.crocodiletelegrambot.entities.Chat;
import ru.zaza.game.crocodiletelegrambot.repositories.ChatsRepository;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ChatService {

    private final ChatsRepository chatsRepository;

    @Autowired
    public ChatService(ChatsRepository chatsRepository) {
        this.chatsRepository = chatsRepository;
    }

    public Chat findOne(long id) {
        Optional<Chat> foundChat = chatsRepository.findById(id);
        return foundChat.orElse(new Chat(0));
    }

    @Transactional
    public void save(Chat chat) {
        chatsRepository.save(chat);
    }
}
