package ru.ifmo.ctd.mekhanikov.crawler.twitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.ctd.mekhanikov.crawler.FriendsService;
import ru.ifmo.ctd.mekhanikov.crawler.Target;
import twitter4j.*;

import java.util.ArrayList;
import java.util.List;

@Target("twitter")
public class TwitterFriendsService implements FriendsService {

    private static final Logger LOG = LoggerFactory.getLogger(TwitterFriendsService.class);
    private static final String FRIENDS_ENDPOINT = "/friends/ids";

    private final Twitter twitter;

    public TwitterFriendsService() {
        this.twitter = TwitterFactory.getSingleton();
        LOG.info("Twitter service initialized");
    }

    @Override
    public List<Long> getFriends(long userId) throws Exception {
        LOG.info("Getting friends list of user " + userId);
        List<Long> friends = new ArrayList<>();
        long cursor = -1;
        while (cursor != 0) {
            LOG.info(String.format("Sending request. userId=%d, cursor=%d", userId, cursor));
            IDs result = twitter.getFriendsIDs(userId, cursor);
            LOG.info("Response received");
            for (long id : result.getIDs()) {
                friends.add(id);
            }
            cursor = result.getNextCursor();
        }
        return friends;
    }

    @Override
    public int getRequestsLeft() {
        try {
            return getRateLimitStatus().getRemaining();
        } catch (TwitterException e) {
            LOG.info("Failed to get rate limit status: " + e.getMessage());
            return -1;
        }
    }

    @Override
    public int getSecondsUntilReset() {
        try {
            return getRateLimitStatus().getSecondsUntilReset();
        } catch (TwitterException e) {
            LOG.info("Failed to get rate limit status: " + e.getMessage());
            return -1;
        }
    }

    private RateLimitStatus getRateLimitStatus() throws TwitterException {
        return twitter.getRateLimitStatus().get(FRIENDS_ENDPOINT);
    }
}
