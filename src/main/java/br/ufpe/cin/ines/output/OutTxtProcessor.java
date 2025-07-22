package br.ufpe.cin.ines.output;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Utility class to process an `out.txt` file and extract potential interferences into JSON.
 * Uses Jackson for JSON serialization (add "com.fasterxml.jackson.core:jackson-databind" to your pom.xml).
 */
public class OutTxtProcessor {

    private static final Pattern PATTERN = Pattern.compile(
            "Node\\(<([^:>]+):[^>]+>.*?,\\s*(-?\\d+),",
            Pattern.DOTALL
    );

    /**
     * Reads 'out.txt' from the given directory, extracts class and line info,
     * and writes 'potential_interferences.json' in the same directory.
     *
     * @param dirPath the directory containing 'out.txt'
     * @throws IOException if file operations fail
     */
    public static void processOutTxt(String dirPath) throws IOException {
        Path inputPath = Paths.get(dirPath, "out.txt");
        Path outputPath = Paths.get(dirPath, "potential_interferences.json");

        List<String> allLines = Files.readAllLines(inputPath);
        List<Map<String, Object>> interferences = new ArrayList<>();

        for (String rawLine : allLines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }

            Matcher matcher = PATTERN.matcher(line);
            List<Map<String, String>> interferenceList = new ArrayList<>();

            while (matcher.find()) {
                String cls = matcher.group(1);
                String ln = matcher.group(2);

                // Remove inner-class suffix after '$'
                String sanitizedCls = cls.contains("$")
                        ? cls.substring(0, cls.indexOf('$'))
                        : cls;

                Map<String, String> entry = new HashMap<>();
                entry.put("line", ln);
                entry.put("class", sanitizedCls);
                interferenceList.add(entry);
            }

            Map<String, Object> wrapper = new HashMap<>();
            wrapper.put("interference", interferenceList);
            interferences.add(wrapper);
        }

        // Write JSON output
        ObjectMapper mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(outputPath.toFile(), interferences);
    }
}
