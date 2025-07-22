package br.ufpe.cin.ines.output;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonFileSanitizer {
    private static final Pattern PAT_CODE_ELEMENT = Pattern.compile(
            "(\"codeElement\"\\s*:\\s*\")(.*?)(\")(?=\\s*\\r?\\n\\s*\\},)",
            Pattern.DOTALL
    );

    private static final Pattern PAT_CTRL = Pattern.compile("[\\x00-\\x08\\x0B-\\x0C\\x0E-\\x1F]");

    public static void processFile(String path) throws IOException {
        Path file = Paths.get(path);
        Path backup = Paths.get(path + ".bak");
        Files.copy(file, backup, StandardCopyOption.REPLACE_EXISTING);

        String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);

        text = replaceWithProcessor(text, PAT_CODE_ELEMENT, JsonFileSanitizer::escapeCodeElem);
        text = sanitizeStrings(text);
        text = PAT_CTRL.matcher(text).replaceAll("");
        // Quote unquoted tool values
        text = text.replaceAll(
                "\"tool\"\\s*:\\s*([A-Za-z][A-Za-z0-9_]*)",
                "\"tool\": \"$1\""
        );

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        JsonNode data = mapper.readTree(text);
        mapper.writeValue(file.toFile(), data);

        Files.delete(backup);
    }

    private static String replaceWithProcessor(String input, Pattern pattern, MatchProcessor processor) {
        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String replacement = processor.process(matcher);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String escapeCodeElem(Matcher m) {
        String prefix = m.group(1);
        String raw = m.group(2);
        String suffix = m.group(3);
        String escaped = raw
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
        return prefix + escaped + suffix;
    }

    private static String sanitizeStrings(String text) {
        StringBuilder out = new StringBuilder();
        boolean inStr = false;
        boolean esc = false;
        for (char c : text.toCharArray()) {
            if (esc) {
                out.append(c);
                esc = false;
            } else if (inStr) {
                if (c == '\\') {
                    out.append(c);
                    esc = true;
                } else if (c == '"') {
                    out.append(c);
                    inStr = false;
                } else if (c == '\n') {
                    out.append("\\n");
                } else if (c == '\r') {
                    out.append("\\r");
                } else if (c == '\t') {
                    out.append("\\t");
                } else if (c >= 32) {
                    out.append(c);
                }
            } else {
                if (c == '"') {
                    out.append(c);
                    inStr = true;
                } else {
                    out.append(c);
                }
            }
        }
        return out.toString();
    }

    @FunctionalInterface
    private interface MatchProcessor {
        String process(Matcher m);
    }
}
