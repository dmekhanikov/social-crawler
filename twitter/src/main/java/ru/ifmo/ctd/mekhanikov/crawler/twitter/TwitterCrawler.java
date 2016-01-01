package ru.ifmo.ctd.mekhanikov.crawler.twitter;

import ru.ifmo.ctd.mekhanikov.crawler.Config;
import ru.ifmo.ctd.mekhanikov.crawler.Crawler;
import ru.ifmo.ctd.mekhanikov.crawler.MongoDAO;

import java.io.File;
import java.io.IOException;

public class TwitterCrawler {

    public static void main(String ... args) throws IOException {
        Config config = Config.getInstance();
        Config.getInstance().load();
        TwitterFriendsService friendsService = new TwitterFriendsService();
        Crawler crawler = new Crawler(friendsService);
        crawler.crawl(new File(config.getInputFile()), MongoDAO.Collection.TWITTER);
    }
}
