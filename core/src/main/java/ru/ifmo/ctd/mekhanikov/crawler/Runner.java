package ru.ifmo.ctd.mekhanikov.crawler;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Set;

public class Runner {

    private static final Logger LOG = LoggerFactory.getLogger(Runner.class);

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
        if (!FriendsService.class.isAssignableFrom(targetClass)) {
            throw new TargetCreationException("Target should implement FriendsService class");
        }
        Constructor<?> constructor;
        try {
            constructor = targetClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new TargetCreationException("Target should have an empty constructor", e);
        }
        try {
            return (FriendsService) constructor.newInstance();
        } catch (Exception e) {
            throw new TargetCreationException("Failed to instantiate the target", e);
        }
    }

    private File getInputFile(String... args) {
        if (args.length > 1) {
            System.err.println("Usage:\n\tjava -jar <jar file> [input file]");
            System.exit(1);
        }
        if (args.length == 1) {
            return new File(args[0]);
        } else {
            return null;
        }
    }

    private void run(String... args) throws TargetCreationException, IOException {
        Config config = Config.getInstance();
        config.load();
        Class<?> targetClass = findTarget();
        LOG.info("Using " + targetClass + " as a FriendsService");
        FriendsService friendsService = createFriendsService(targetClass);
        String outputCollection = targetClass.getAnnotation(Target.class).value();
        Crawler crawler = new Crawler(friendsService);
        crawler.crawl(getInputFile(args), outputCollection);
    }

    public static void main(String... args) throws Exception {
        new Runner().run(args);
    }
}
