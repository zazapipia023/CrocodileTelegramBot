package ru.zaza.game.crocodiletelegrambot.util;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

@Component
@Getter
public class Dictionary {

    private final int NUMBER_OF_WORDS = 16071;
    private final String FILE_NAME = "words.txt";
    private List<String> words = null;

    public void loadWords() {
        try {
            words = Files.readAllLines(Paths.get(FILE_NAME).toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String loadWord() {
        int randomIndex = new Random().nextInt(NUMBER_OF_WORDS);

        return words.get(randomIndex);
    }

}
