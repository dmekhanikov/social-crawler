package ru.ifmo.ctd.mekhanikov.crawler;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class MongoDAO {

    private static final String HOST_PROPERTY = "mongo.host";
    private static final String PORT_PROPERTY = "mongo.port";
    private static final String DATABASE_PROPERTY = "mongo.database";
    private static final String USERNAME_PROPERTY = "mongo.username";
    private static final String PASSWORD_PROPERTY = "mongo.password";

    private static final Logger LOG = LoggerFactory.getLogger(MongoDAO.class);
    private static MongoDAO INSTANCE;

    private final MongoDatabase db;

    private MongoDAO() {
        String host = System.getProperty(HOST_PROPERTY);
        int port = Integer.parseInt(System.getProperty(PORT_PROPERTY));
        String database = System.getProperty(DATABASE_PROPERTY);
        String username = System.getProperty(USERNAME_PROPERTY);
        String password = System.getProperty(PASSWORD_PROPERTY);
        MongoClient mongoClient;
        if (!username.isEmpty() && !password.isEmpty()) {
            MongoCredential credential = MongoCredential.createCredential(username, database, password.toCharArray());
            mongoClient = new MongoClient(new ServerAddress(host, port), Collections.singletonList(credential));
        } else {
            mongoClient = new MongoClient(host, port);
        }
        this.db = mongoClient.getDatabase(database);
        LOG.info("Connected to the database");
    }

    public static MongoDAO getInstance() throws IOException {
        if (INSTANCE == null) {
            INSTANCE = new MongoDAO();
        }
        return INSTANCE;
    }

    public void insert(String collection, long userId, List<Long> friends) {
        db.getCollection(collection).insertOne(new Document("userId", userId).append("friends", friends));
    }

    public boolean contains(String collection, long userId) {
        return db.getCollection(collection).find(new Document("userId", userId)).first() != null;
    }
}
