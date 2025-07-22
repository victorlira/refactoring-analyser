package br.ufpe.cin.ines.output;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BaseProcessor {
    private static final Pattern CTRL_CHARS = Pattern.compile("[\u0000-\u001F]");
    private static final Pattern LINE_COMMENT = Pattern.compile("//.*");
    private static final Pattern BLOCK_COMMENT = Pattern.compile("/\\*[\\s\\S]*?\\*/");
    private static final Pattern TRAILING_COMMA = Pattern.compile(",\\s*(?=[\\]}])");

    private static JsonNode loadJson(String path) {
        ObjectMapper mapper = new ObjectMapper();
        String text;
        try {
            text = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error reading file " + path + ": " + e.getMessage());
            System.exit(1);
            return null;
        }
        if (text.startsWith("\uFEFF")) {
            text = text.substring(1);
        }
        text = CTRL_CHARS.matcher(text).replaceAll(" ");
        text = LINE_COMMENT.matcher(text).replaceAll("");
        text = BLOCK_COMMENT.matcher(text).replaceAll("");
        text = TRAILING_COMMA.matcher(text).replaceAll("");
        try {
            return mapper.readTree(text);
        } catch (JsonProcessingException e) {
            System.err.println("Error parsing JSON in " + path + ": " + e.getMessage());
            System.exit(1);
            return null;
        } catch (IOException e) {
            System.err.println("I/O error parsing JSON in " + path + ": " + e.getMessage());
            System.exit(1);
            return null;
        }
    }

    public static void process(String scenDir) throws IOException {
        Path scenPath = Paths.get(scenDir);
        if (!Files.isDirectory(scenPath)) {
            System.err.println("Warning: directory " + scenDir + " does not exist, skipping.");
            return;
        }
        Path piPath = scenPath.resolve("potential_interferences.json");
        Path rrPath = scenPath.resolve("refactor-result.json");
        if (!Files.isRegularFile(piPath)) {
            System.err.println("Warning: " + piPath + " does not exist, skipping.");
            return;
        }
        if (!Files.isRegularFile(rrPath)) {
            System.err.println("Warning: " + rrPath + " does not exist, skipping.");
            return;
        }

        JsonNode potentials = loadJson(piPath.toString());
        JsonNode refactors = loadJson(rrPath.toString());

        Map<String, List<JsonNode>> refMap = new HashMap<>();
        for (Iterator<JsonNode> it = refactors.elements(); it.hasNext(); ) {
            JsonNode item = it.next();
            String cls = item.get("class").asText();
            int line = item.get("line").asInt();
            String key = cls + "#" + line;
            refMap.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
        }

        List<JsonNode> refInts = new ArrayList<>();
        List<List<JsonNode>> noRefInts = new ArrayList<>();

        for (Iterator<JsonNode> cit = potentials.elements(); cit.hasNext(); ) {
            JsonNode conflict = cit.next();
            JsonNode intrList = conflict.get("interference");
            List<JsonNode> noMatch = new ArrayList<>();
            for (Iterator<JsonNode> iit = intrList.elements(); iit.hasNext(); ) {
                JsonNode intr = iit.next();
                String cls = intr.get("class").asText();
                int line = intr.get("line").asInt();
                String key = cls + "#" + line;
                List<JsonNode> matches = refMap.get(key);
                if (matches != null) {
                    refInts.addAll(matches);
                } else {
                    noMatch.add(intr);
                }
            }
            if (!noMatch.isEmpty()) {
                noRefInts.add(noMatch);
            }
        }

        Path outRef = scenPath.resolve("refactoring_interferences.json");
        Path outNoRef = scenPath.resolve("no_refactoring_interferences.json");

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(outRef.toFile(), refInts);
        mapper.writerWithDefaultPrettyPrinter().writeValue(outNoRef.toFile(), noRefInts);

        System.out.println(String.format("[Scenario %s] → %d refactoring, %d no-refactoring", scenDir, refInts.size(), noRefInts.size()));
    }
}
