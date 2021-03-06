package ru.otus.hw15.db;



import ru.otus.hw15.domain.ChatMessage;
import ru.otus.hw15.domain.User;

import java.util.List;
import java.util.Optional;

public interface DBService {

    Optional<User> findByUserLogin(String value);

    List<User> getAllUsers();

    boolean saveUser(User userDao);

    boolean saveChatMessage(ChatMessage chatMessage);

    List<ChatMessage> getHistoryChatMessage();

}
