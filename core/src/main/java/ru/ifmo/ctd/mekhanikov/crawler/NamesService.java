package ru.ifmo.ctd.mekhanikov.crawler;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class NamesService {

    private Map<Long, String> names = new HashMap<>();
    private Map<String, Long> ids = new HashMap<>();

    public NamesService(InputStream inputStream) throws IOException {
        read(inputStream);
    }

    private void read(InputStream inputStream) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            try (CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT)) {
                for (CSVRecord record : parser) {
                    long id = Long.parseLong(record.get(0));
                    String name = record.get(1);
                    names.put(id, name);
                    ids.put(name, id);
                }
            }
        }
    }

    public String getName(long id) {
        return names.get(id);
    }

    public Long getId(String name) {
        return ids.get(name);
    }
}
