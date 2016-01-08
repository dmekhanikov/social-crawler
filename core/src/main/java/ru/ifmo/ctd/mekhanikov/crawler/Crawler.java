package ru.ifmo.ctd.mekhanikov.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

public class Crawler {

    private static final Logger LOG = LoggerFactory.getLogger(Crawler.class);
    private static final int MIN_SLEEP_TIME = 5;

    private final FriendsService friendsService;
    private final MongoDAO dao;

    public Crawler(FriendsService friendsService) throws IOException {
        this.friendsService = friendsService;
        this.dao = MongoDAO.getInstance();
    }

    public void crawl(File inputFile, String outputCollection) throws IOException {
        Reader reader;
        if (inputFile != null) {
            reader = new FileReader(inputFile);
        } else {
            reader = new InputStreamReader(System.in);
        }
        try (BufferedReader br = new BufferedReader(reader)) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                long userId = Long.parseLong(line);
                if (dao.contains(outputCollection, userId)) {
                    LOG.info("User " + userId + " is already in the database");
                } else {
                    processUser(userId, outputCollection);
                }
            }
        }
    }

    private void processUser(long userId, String outputCollection) {
        boolean rateError = false;
        do {
            try {
                List<Long> friends = friendsService.getFriends(userId);
                LOG.info("Storing result into the database");
                dao.insert(outputCollection, userId, friends);
            } catch (Exception e) {
                if (friendsService.getRequestsLeft() == 0) {
                    int waitingTime = Math.max(friendsService.getSecondsUntilReset() + 1, MIN_SLEEP_TIME);
                    LOG.info("No requests left. Waiting " + waitingTime + " seconds");
                    try {
                        Thread.sleep(waitingTime * 1000);
                    } catch (InterruptedException ignore) {}
                    rateError = true;
                } else {
                    LOG.info("Failed to get friends of user " + userId + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } while (rateError);
    }
}
