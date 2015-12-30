package ru.ifmo.ctd.mekhanikov.crawler.twitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.ctd.mekhanikov.crawler.Config;
import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TwitterCrawler {

    private static final Logger LOG = LoggerFactory.getLogger(TwitterCrawler.class);

    private final Twitter twitter;

    private TwitterCrawler() throws IOException {
        Config.load();
        this.twitter = TwitterFactory.getSingleton();
        LOG.info("Twitter service initialized");
    }

    private List<Long> getFriends(long userId) {
        LOG.info("Getting friends list of user " + userId);
        List<Long> friends = new ArrayList<>();
        long cursor = -1;
        while (cursor != 0) {
            try {
                LOG.info(String.format("Sending request. userId=%d, cursor=%d", userId, cursor));
                IDs result = twitter.getFriendsIDs(userId, cursor);
                LOG.info("Response received");
                for (long id : result.getIDs()) {
                    friends.add(id);
                }
                cursor = result.getNextCursor();
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }
        return friends;
    }

    private void doMain() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(Config.getInputFile()))) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                long userId = Long.parseLong(line);
                System.out.println(getFriends(userId));
            }
        }
    }

    public static void main(String ... args) throws IOException {
        new TwitterCrawler().doMain();
    }
}
