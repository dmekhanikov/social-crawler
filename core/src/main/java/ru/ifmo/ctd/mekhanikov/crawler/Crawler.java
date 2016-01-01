package ru.ifmo.ctd.mekhanikov.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Crawler {

    private static final Logger LOG = LoggerFactory.getLogger(Crawler.class);

    private final FriendsService friendsService;
    private final MongoDAO dao;

    public Crawler(FriendsService friendsService) throws IOException {
        this.friendsService = friendsService;
        this.dao = MongoDAO.getInstance();
    }

    public void crawl(File inputFile, MongoDAO.Collection outputCollection) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                long userId = Long.parseLong(line);
                if (dao.contains(outputCollection, userId)) {
                    LOG.info("User " + userId + " is already in the database");
                } else {
                    try {
                        List<Long> friends = friendsService.getFriends(userId);
                        LOG.info("Storing result into the database");
                        dao.insert(outputCollection, userId, friends);
                    } catch (Exception e) {
                        LOG.info("Failed to get friends of user " + userId + ": " + e.getMessage());
                    }
                }
            }
        }
    }

}
