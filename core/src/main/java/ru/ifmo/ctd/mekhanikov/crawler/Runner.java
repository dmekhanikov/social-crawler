package ru.ifmo.ctd.mekhanikov.crawler;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Runner {

    private static final Logger LOG = LoggerFactory.getLogger(Runner.class);

    @Option(name = "-names", usage = "set if input data contains names of users in CSV format")
    private boolean withNames;

    @Argument
    private File inputFile;

    private Class<?> findTarget() throws TargetCreationException {
        Reflections reflections = new Reflections();
        Set<Class<?>> result = reflections.getTypesAnnotatedWith(Target.class);
        if (result.isEmpty()) {
            throw new TargetCreationException("No targets found");
        } else if (result.size() > 1) {
            throw new TargetCreationException(result.size() + " targets found");
        } else {
            return result.iterator().next();
        }
    }

    private FriendsService createFriendsService(Class<?> targetClass) throws TargetCreationException {
        checkClassIsFriendsService(targetClass);
        Constructor<?> constructor;
        try {
            constructor = targetClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new TargetCreationException("Found target doesn't have an empty constructor", e);
        }
        try {
            return (FriendsService) constructor.newInstance();
        } catch (Exception e) {
            throw new TargetCreationException("Failed to instantiate the target", e);
        }
    }

    private FriendsService createFriendsService(Class<?> targetClass, NamesService namesService) throws TargetCreationException {
        checkClassIsFriendsService(targetClass);
        Constructor<?> constructor;
        try {
            constructor = targetClass.getConstructor(NamesService.class);
        } catch (NoSuchMethodException e) {
            throw new TargetCreationException("Found target doesn't have a constructor of a NamesService", e);
        }
        try {
            return (FriendsService) constructor.newInstance(namesService);
        } catch (Exception e) {
            throw new TargetCreationException("Failed to instantiate the target", e);
        }
    }

    private void checkClassIsFriendsService(Class<?> clazz) throws TargetCreationException {
        if (!FriendsService.class.isAssignableFrom(clazz)) {
            throw new TargetCreationException("Target should implement FriendsService class");
        }
    }

    private void parseArguments(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            System.err.println("java SampleMain [options...] arguments...");
            parser.printUsage(System.err);
            System.err.println();
            System.exit(1);
        }
    }

    private List<Long> parseUsers() throws IOException {
        List<Long> users = new ArrayList<>();
        try (Reader reader = new BufferedReader(new InputStreamReader(getInputStream()))) {
            try (CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT)) {
                for (CSVRecord record : parser) {
                    users.add(Long.parseLong(record.get(0)));
                }
            }
        }
        return users;
    }

    private InputStream getInputStream() throws FileNotFoundException {
        if (inputFile != null) {
            return new FileInputStream(inputFile);
        } else {
            return System.in;
        }
    }

    private void run(String[] args) throws TargetCreationException, IOException {
        parseArguments(args);
        Config config = Config.getInstance();
        config.load();
        Class<?> targetClass = findTarget();
        LOG.info("Using " + targetClass + " as a FriendsService");
        FriendsService friendsService;
        if (withNames) {
            NamesService namesService = new NamesService(getInputStream());
            friendsService = createFriendsService(targetClass, namesService);
        } else {
            friendsService = createFriendsService(targetClass);
        }
        String outputCollection = targetClass.getAnnotation(Target.class).value();
        Crawler crawler = new Crawler(friendsService);
        crawler.crawl(parseUsers(), outputCollection);
    }

    public static void main(String... args) throws Exception {
        new Runner().run(args);
    }
}
