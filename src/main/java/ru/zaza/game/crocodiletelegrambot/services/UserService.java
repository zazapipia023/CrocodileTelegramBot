package ru.zaza.game.crocodiletelegrambot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.zaza.game.crocodiletelegrambot.entities.Chat;
import ru.zaza.game.crocodiletelegrambot.entities.Player;
import ru.zaza.game.crocodiletelegrambot.repositories.UsersRepository;

import java.util.List;
import java.util.Optional;


@Service
@Transactional(readOnly = true)
public class UserService {

    private final UsersRepository usersRepository;

    @Autowired
    public UserService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public Player findOne(long id) {
        Optional<Player> foundUser = usersRepository.findById(id);
        return foundUser.orElse(new Player(0, ""));
    }

    public String findTop(Chat chat) {
        List<Player> players = usersRepository.findByChatOrderByPoints(chat);
        if (players.size() > 20) players = players.subList(0, 20);
        StringBuilder sb = new StringBuilder();
        sb.append("Топ игроков беседы: \n\n");
        for (Player player:
             players) {
            sb.append("@" + player.getUsername() + " - " + player.getPoints() + " угадано\n");
        }

        return sb.toString();
    }

    @Transactional
    public void save(Player player) {
        usersRepository.save(player);
    }

}
