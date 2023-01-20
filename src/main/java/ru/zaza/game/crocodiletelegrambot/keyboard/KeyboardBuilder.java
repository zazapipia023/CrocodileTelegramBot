package ru.zaza.game.crocodiletelegrambot.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.zaza.game.crocodiletelegrambot.enums.GameState;

import java.util.ArrayList;
import java.util.List;

@Component
public class KeyboardBuilder {

    public InlineKeyboardMarkup makeKeyboard(GameState gameState) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        switch (gameState) {
            case WAITING -> {
                rowInLine.add(makeButton("Я хочу!", "EXPLAIN_BUTTON"));
                rowsInLine.add(rowInLine);
                markup.setKeyboard(rowsInLine);
                return markup;
            }
            case STARTED -> {
                rowInLine.add(makeButton("Посмотреть слово", "CHECK_WORD"));
                rowInLine.add(makeButton("Новое слово", "NEW_WORD"));
                rowsInLine.add(rowInLine);
                markup.setKeyboard(rowsInLine);
                return markup;
            }
        }

        return markup;
    }

    public InlineKeyboardButton makeButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);

        return button;
    }

    public AnswerCallbackQuery makeAnswerCallbackQuery(String id, String word) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(id);
        answerCallbackQuery.setShowAlert(true);
        answerCallbackQuery.setText(word);

        return answerCallbackQuery;
    }
}
