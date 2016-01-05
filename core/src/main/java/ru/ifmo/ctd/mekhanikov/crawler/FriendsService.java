package ru.ifmo.ctd.mekhanikov.crawler;

import java.util.List;

public interface FriendsService {
    List<Long> getFriends(long userId) throws Exception;
    int getRequestsLeft();
    int getSecondsUntilReset();
}
