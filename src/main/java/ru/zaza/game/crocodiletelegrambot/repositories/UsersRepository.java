package ru.zaza.game.crocodiletelegrambot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.zaza.game.crocodiletelegrambot.entities.Chat;
import ru.zaza.game.crocodiletelegrambot.entities.Player;

import java.util.List;

@Repository
public interface UsersRepository extends JpaRepository<Player, Long> {

    List<Player> findByChatOrderByPoints(Chat chat);
}
