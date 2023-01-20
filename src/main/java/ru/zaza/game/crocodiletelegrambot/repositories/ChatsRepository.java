package ru.zaza.game.crocodiletelegrambot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.zaza.game.crocodiletelegrambot.entities.Chat;

@Repository
public interface ChatsRepository extends JpaRepository<Chat, Long> {
}
