package ru.otus.hw13.services;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.stereotype.Service;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Service
public class DBManagerImpl implements DBManager{

    private MongoDatabase mongoDatabase;

    public DBManagerImpl() {
        initMongoDB();
    }

    private void initMongoDB() {
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(), fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoClient mongoClient = new MongoClient("localhost", MongoClientOptions.builder().codecRegistry(pojoCodecRegistry).build());
        mongoDatabase = mongoClient.getDatabase("hw13");
    }

    @Override
    public  MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }

}
