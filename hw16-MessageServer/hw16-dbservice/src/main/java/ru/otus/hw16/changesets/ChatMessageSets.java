package ru.otus.hw16.changesets;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import ru.otus.hw16.shared.domain.ChatMessage;

@ChangeLog
public class ChatMessageSets {

    private static final String COLLECTION_NAME = "chatMessage";

    @ChangeSet(id= "withMongoDatabase04", order = "001", author = "Mongock")
    public void changeSet1(MongoDatabase mongoDatabase) {
        mongoDatabase.getCollection(COLLECTION_NAME).insertOne(createMongoDocument(getChatMessage("Welcome to chat with MS")));
    }

    @ChangeSet(id= "withMongoDatabase05", order = "002", author = "Mongock")
    public void changeSet2(MongoDatabase mongoDatabase) {
        mongoDatabase.getCollection(COLLECTION_NAME).insertOne(createMongoDocument(getChatMessage("These messages are generated by default.")));
    }

    private Document createMongoDocument(ChatMessage chatMessage) {
        return new Document()
                .append("type", chatMessage.getType())
                .append("sender", chatMessage.getSender())
                .append("content", chatMessage.getContent()
                );
    }

    private ChatMessage getChatMessage(String content) {
        return new ChatMessage(ChatMessage.MessageType.CHAT,content, "Mongock");
    }
}
