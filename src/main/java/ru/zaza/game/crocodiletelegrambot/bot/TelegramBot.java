package ru.zaza.game.crocodiletelegrambot.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.zaza.game.crocodiletelegrambot.config.BotConfig;
import ru.zaza.game.crocodiletelegrambot.entities.Chat;
import ru.zaza.game.crocodiletelegrambot.entities.Player;
import ru.zaza.game.crocodiletelegrambot.enums.GameState;
import ru.zaza.game.crocodiletelegrambot.handler.AdminMessageHandler;
import ru.zaza.game.crocodiletelegrambot.handler.UserMessageHandler;
import ru.zaza.game.crocodiletelegrambot.keyboard.KeyboardBuilder;
import ru.zaza.game.crocodiletelegrambot.services.ChatService;
import ru.zaza.game.crocodiletelegrambot.services.UserService;
import ru.zaza.game.crocodiletelegrambot.util.Dictionary;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${admin_id}")
    private long adminId;
    private final BotConfig botConfig;
    private final AdminMessageHandler adminMessageHandler;
    private final UserMessageHandler userMessageHandler;
    private final ChatService chatService;
    private final UserService userService;
    private final KeyboardBuilder keyboardBuilder;
    private final Dictionary dictionary;

    @Autowired
    public TelegramBot(BotConfig botConfig, AdminMessageHandler adminMessageHandler, UserMessageHandler userMessageHandler, ChatService chatService, UserService userService, KeyboardBuilder keyboardBuilder, Dictionary dictionary) {
        this.botConfig = botConfig;
        this.adminMessageHandler = adminMessageHandler;
        this.userMessageHandler = userMessageHandler;
        this.chatService = chatService;
        this.userService = userService;
        this.keyboardBuilder = keyboardBuilder;
        this.dictionary = dictionary;
        dictionary.loadWords();
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            try {
                handleCallbackQuery(callbackQuery);
                log.info("Handled to callbackQuery in chat: " + update.getCallbackQuery().getMessage().getChatId());
            } catch (TelegramApiException e) {
                throw new RuntimeException();
            }
        } else {
            Message message = update.getMessage();
            if(message.hasText()) {
                try {
                    handleMessage(message);
                    log.info("Sent message to chat: " + update.getMessage().getChatId());
                } catch (TelegramApiException e) {
                    throw new RuntimeException();
                }
            }
        }
    }

    public void handleMessage(Message message) throws TelegramApiException {
        if (message.isSuperGroupMessage()) {
            handleGroupMessage(message);
            return;
        }
        if (message.getChatId() == adminId) {
            execute(adminMessageHandler.handleMessage(message));
            return;
        }
        execute(userMessageHandler.handleMessage(message));
    }

    public void handleCallbackQuery(CallbackQuery callbackQuery) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(callbackQuery.getMessage().getChatId());
        User person = callbackQuery.getFrom();
        Chat currentChat = chatService.findOne(callbackQuery.getMessage().getChatId());

        String callbackData = callbackQuery.getData();

        switch (callbackData) {
            case "EXPLAIN_BUTTON" -> {
                currentChat.setWord(dictionary.loadWord());
                currentChat.setExplainingPerson(person.getId());
                chatService.save(currentChat);

                sendMessage.setText(person.getFirstName() + " объясняет слово, ждите");
                sendMessage.setReplyMarkup(keyboardBuilder.makeKeyboard(GameState.STARTED));
                log.info("Setting explaining person and word to explain");
                execute(deleteMessage(callbackQuery));
                execute(sendMessage);
                break;
            }
            case "CHECK_WORD" -> {
                if (person.getId().equals(currentChat.getExplainingPerson())) {
                    log.info("Player checked word");
                    execute(makeAnswerCallbackQuery(callbackQuery.getId(), currentChat.getWord()));
                }
                break;
            }
            case "NEW_WORD" -> {
                if (person.getId().equals(currentChat.getExplainingPerson())) {
                    currentChat.setWord(dictionary.loadWord());
                    chatService.save(currentChat);
                    log.info("Player changed word");
                    execute(makeAnswerCallbackQuery(callbackQuery.getId(), currentChat.getWord()));
                }
                break;
            }
        }
    }

    public void handleGroupMessage(Message message) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());

        Chat currentChat = chatService.findOne(message.getChatId());

        if (currentChat.getChatId() == 0) {
            chatService.save(new Chat(message.getChatId()));
            sendMessage.setText("Воздух мимо, можете играть");
            log.info("Registered chat. Chat id: " + message.getChatId());
            execute(sendMessage);
            return;
        }

        if (currentChat.isStarted()) {
            if (message.getText().toLowerCase().equals(currentChat.getWord())
                    && !message.getFrom().getId().equals(currentChat.getExplainingPerson())) {

                currentChat.setWord(null);
                currentChat.setExplainingPerson(null);
                chatService.save(currentChat);

                Player player = userService.findOne(message.getFrom().getId());
                if (player.getId() == 0) {
                    player.setId(message.getFrom().getId());
                    player.setUsername(message.getFrom().getUserName());
                    player.addPoint();
                    player.setChat(currentChat);
                    userService.save(player);
                    log.info("Registered player in chat and added point.");
                } else {
                    player.addPoint();
                    log.info("Added point to player");
                    userService.save(player);
                }

                sendMessage.setText(message.getFrom().getFirstName() + " угадывает слово, ахуеть...\n" +
                        "Кто хочет объяснять слово?");
                sendMessage.setReplyMarkup(keyboardBuilder.makeKeyboard(GameState.WAITING));
                execute(sendMessage);
                return;
            }
        }

        if (message.getText().equals("/stop@normCrocoGame_bot") && currentChat.isStarted()) {
            currentChat.setStarted(false);
            currentChat.setExplainingPerson(null);
            currentChat.setWord(null);
            chatService.save(currentChat);

            sendMessage.setText("Игра остановлена, расходимся");
            log.info("Stopped game in chat. Chat id: " + message.getChatId());
            execute(sendMessage);
            return;
        }

        if (message.getText().equals("/start@normCrocoGame_bot") && !currentChat.isStarted()) {
            currentChat.setStarted(true);
            chatService.save(currentChat);


            sendMessage.setText("Кто хочет объяснять слово?");
            sendMessage.setReplyMarkup(keyboardBuilder.makeKeyboard(GameState.WAITING));
            log.info("Started game in chat. Chat id: " + message.getChatId());
            execute(sendMessage);
            return;
        }

        if (message.getText().equals("/top@normCrocoGame_bot")) {
            sendMessage.setText(userService.findTop(currentChat));
            execute(sendMessage);
            log.info("Made top in chat. Chat id: " + message.getChatId());
            return;
        }
    }

    private AnswerCallbackQuery makeAnswerCallbackQuery(String id, String word) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(id);
        answerCallbackQuery.setShowAlert(true);
        answerCallbackQuery.setText(word);

        return answerCallbackQuery;
    }

    private DeleteMessage deleteMessage(CallbackQuery callbackQuery) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setMessageId(callbackQuery.getMessage().getMessageId());
        deleteMessage.setChatId(callbackQuery.getMessage().getChatId());

        return deleteMessage;
    }
}
