package ru.otus.hw15.front;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.hw15.messagesystem.Message;
import ru.otus.hw15.messagesystem.MessageType;
import ru.otus.hw15.messagesystem.MsClient;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;


public class FrontendServiceImpl implements FrontendService {
  private static final Logger logger = LoggerFactory.getLogger(FrontendServiceImpl.class);

  private final Map<UUID, Consumer<?>> consumerMap = new ConcurrentHashMap<>();
  private final MsClient msClient;
  private final String databaseServiceClientName;

  public FrontendServiceImpl(MsClient msClient, String databaseServiceClientName) {
    this.msClient = msClient;
    this.databaseServiceClientName = databaseServiceClientName;
  }

  @Override
  public void getUserData(String userLogin, Consumer<String> dataConsumer) {
    Message outMsg = msClient.produceMessage(databaseServiceClientName,"findByUserLogin", userLogin);
    consumerMap.put(outMsg.getId(), dataConsumer);
    msClient.sendMessage(outMsg);
  }

  @Override
  public <T> Optional<Consumer<T>> takeConsumer(UUID sourceMessageId, Class<T> tClass) {
    Consumer<T> consumer = (Consumer<T>) consumerMap.remove(sourceMessageId);
    if (consumer == null) {
      logger.warn("consumer not found for:{}", sourceMessageId);
      return Optional.empty();
    }
    return Optional.of(consumer);
  }
}
