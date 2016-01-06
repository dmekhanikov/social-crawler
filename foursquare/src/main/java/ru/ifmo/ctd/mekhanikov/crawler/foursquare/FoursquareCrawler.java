package ru.ifmo.ctd.mekhanikov.crawler.foursquare;

import ru.ifmo.ctd.mekhanikov.crawler.Config;
import ru.ifmo.ctd.mekhanikov.crawler.Crawler;
import ru.ifmo.ctd.mekhanikov.crawler.FriendsService;
import ru.ifmo.ctd.mekhanikov.crawler.MongoDAO;

import java.io.IOException;

public class FoursquareCrawler {
    public static void main(String... args) throws IOException {
        Config config = Config.getInstance();
        config.load();
        FriendsService friendsService = new FoursquareFriendsService();
        Crawler crawler = new Crawler(friendsService);
        crawler.crawl(config.getInputFile(), MongoDAO.Collection.FOURSQUARE);
    }
}
