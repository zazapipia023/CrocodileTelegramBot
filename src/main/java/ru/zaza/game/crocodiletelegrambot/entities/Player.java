package ru.zaza.game.crocodiletelegrambot.entities;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "Player")
@Getter
@Setter
public class Player {

    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "username")
    private String username;

    @Column(name = "points")
    private int points;

    @ManyToOne
    @JoinColumn(name = "chat_id", referencedColumnName = "chat_id")
    private Chat chat;

    public Player(long id, String username) {
        this.id = id;
        this.username = username;
    }

    public Player() {

    }

    public void addPoint() {
        points += 1;
    }
}
