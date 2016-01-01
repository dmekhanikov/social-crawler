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
    private static final Config INSTANCE = new Config();

    private Properties properties;

    private Config() {}

    public static Config getInstance() {
        return INSTANCE;
    }

    public Properties load() throws IOException {
        if (properties != null) {
            return properties;
        }
        LOG.info("Loading config from file " + CONFIG_FILE);
        this.properties= new Properties();
        this.properties.load(new FileReader(CONFIG_FILE));
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            System.setProperty(key, value);
        }
        return properties;
    }

    public String getInputFile() {
        return System.getProperty(INPUT_FILE_PROPERTY, "input.txt");
    }

    public String getProperty(String key) {
        return (String) properties.get(key);
    }
}
