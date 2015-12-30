package ru.ifmo.ctd.mekhanikov.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class Config {
    private static final String CONFIG_FILE = "crawler.properties";
    private static final String INPUT_FILE_PROPERTY = "input";

    private static final Logger LOG = LoggerFactory.getLogger(Config.class);

    private Config() {}

    public static Properties load() throws IOException {
        LOG.info("Loading config from file " + CONFIG_FILE);
        Properties properties = new Properties();
        properties.load(new FileReader(CONFIG_FILE));
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            System.setProperty(key, value);
        }
        return properties;
    }

    public static String getInputFile() {
        return System.getProperty(INPUT_FILE_PROPERTY, "input.txt");
    }
}
