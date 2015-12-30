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

    private MongoDAO() throws IOException {
        Config config = Config.getInstance();
        config.load();
        String host = config.getProperty(HOST_PROPERTY);
        int port = Integer.parseInt(config.getProperty(PORT_PROPERTY));
        String database = config.getProperty(DATABASE_PROPERTY);
        String username = config.getProperty(USERNAME_PROPERTY);
        String password = config.getProperty(PASSWORD_PROPERTY);
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

    public void insert(Collection collection, long userId, List<Long> friends) {
        db.getCollection(collection.toString()).insertOne(new Document("userId", userId).append("friends", friends));
    }

    public boolean contains(Collection collection, long userId) {
        return db.getCollection(collection.toString()).find(new Document("userId", userId)).first() != null;
    }

    public enum Collection {
        TWITTER("twitter"),
        INSTAGRAM("instagram"),
        FOURSQUARE("foursquare");

        private String name;

        Collection(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
