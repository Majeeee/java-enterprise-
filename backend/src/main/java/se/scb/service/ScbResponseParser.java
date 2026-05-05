package se.scb.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.scb.model.Migration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ScbResponseParser {

    private static final Logger log = LoggerFactory.getLogger(ScbResponseParser.class);

    private static final Map<String, String> REGION_NAMES = Map.ofEntries(
            Map.entry("01", "Stockholms l\u00e4n"),
            Map.entry("03", "Uppsala l\u00e4n"),
            Map.entry("04", "S\u00f6dermanlands l\u00e4n"),
            Map.entry("05", "\u00d6sterg\u00f6tlands l\u00e4n"),
            Map.entry("06", "J\u00f6nk\u00f6pings l\u00e4n"),
            Map.entry("07", "Kronobergs l\u00e4n"),
            Map.entry("08", "Kalmar l\u00e4n"),
            Map.entry("09", "Gotlands l\u00e4n"),
            Map.entry("10", "Blekinge l\u00e4n"),
            Map.entry("12", "Sk\u00e5ne l\u00e4n"),
            Map.entry("13", "Hallands l\u00e4n"),
            Map.entry("14", "V\u00e4stra G\u00f6talands l\u00e4n"),
            Map.entry("17", "V\u00e4rmlands l\u00e4n"),
            Map.entry("18", "\u00d6rebro l\u00e4n"),
            Map.entry("19", "V\u00e4stmanlands l\u00e4n"),
            Map.entry("20", "Dalarnas l\u00e4n"),
            Map.entry("21", "G\u00e4vleborgs l\u00e4n"),
            Map.entry("22", "V\u00e4sternorrlands l\u00e4n"),
            Map.entry("23", "J\u00e4mtlands l\u00e4n"),
            Map.entry("24", "V\u00e4sterbottens l\u00e4n"),
            Map.entry("25", "Norrbottens l\u00e4n")
    );

    private static final Map<String, String> GENDER_LABELS = Map.of(
            "1", "men",
            "2", "women"
    );

    private final ObjectMapper objectMapper;

    public ScbResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<Migration> parse(String json) throws IOException {
        return parse(json, "total");
    }

    /**
     * Parses the current SCB JSON format. The selected observations are returned as
     * value columns, so values[0] = immigrations and values[1] = emigrations.
     */
    public List<Migration> parse(String json, String ageGroup) throws IOException {
        JsonNode root = objectMapper.readTree(json);
        JsonNode data = root.path("data");

        record RowKey(String regionCode, String gender, String ageGroup, int year) {}
        Map<RowKey, int[]> accumulator = new HashMap<>();

        int skipped = 0;

        for (JsonNode row : data) {
            JsonNode key = row.path("key");
            JsonNode values = row.path("values");

            if (key.size() < 4 || values.size() < 2) {
                skipped++;
                continue;
            }

            String regionCode = key.get(0).asText();
            String genderCode = key.get(2).asText();
            int year = Integer.parseInt(key.get(3).asText());

            Integer immigrations = parseCount(values.get(0));
            Integer emigrations = parseCount(values.get(1));
            if (immigrations == null || emigrations == null) {
                skipped++;
                continue;
            }

            String gender = GENDER_LABELS.getOrDefault(genderCode, genderCode);
            RowKey rowKey = new RowKey(regionCode, gender, ageGroup, year);
            int[] counts = accumulator.computeIfAbsent(rowKey, ignored -> new int[2]);
            counts[0] += immigrations;
            counts[1] += emigrations;
        }

        List<Migration> migrations = new ArrayList<>(accumulator.size());
        for (Map.Entry<RowKey, int[]> entry : accumulator.entrySet()) {
            RowKey rowKey = entry.getKey();
            int[] counts = entry.getValue();

            migrations.add(new Migration(
                    REGION_NAMES.getOrDefault(rowKey.regionCode(), rowKey.regionCode()),
                    rowKey.regionCode(),
                    rowKey.gender(),
                    rowKey.ageGroup(),
                    rowKey.year(),
                    counts[0],
                    counts[1]
            ));
        }

        log.info("Parsade {} SCB-rader till {} Migration-objekt for alder {} ({} hoppades over)",
                data.size(), migrations.size(), ageGroup, skipped);

        return migrations;
    }

    private Integer parseCount(JsonNode value) {
        String valueText = value.asText().trim();
        if (valueText.isEmpty() || valueText.equals("..")) {
            return null;
        }
        try {
            return Integer.parseInt(valueText);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
