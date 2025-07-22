package br.ufpe.cin.ines.util;

import br.ufpe.cin.ines.model.RefactoringResult;
import br.ufpe.cin.ines.model.ResultItem;
import br.ufpe.cin.ines.model.CommitEnum;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RefactoringResultJsonExporter {

    /**
     * Generates a JSON file from a RefactoringResult.
     *
     * @param refactoringResult the object containing the list of ResultItem
     * @param className         the value for the "class" property, read from a local variable
     * @param outputPath        the path (including filename) of the output .json file; subdirectories will be created if necessary
     * @throws IOException if an I/O error occurs during directory creation or file writing
     */
    public static void exportToJson(RefactoringResult refactoringResult,
                                    String className,
                                    Path outputPath) throws IOException {
        List<ResultItem> items = refactoringResult.getItems();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(System.lineSeparator());

        for (int i = 0; i < items.size(); i++) {
            ResultItem item = items.get(i);
            String branch = mapCommitToBranch(item.getCommit());

            sb.append("  {");
            sb.append(System.lineSeparator());
            sb.append("    \"branch\": \"").append(branch).append("\",");
            sb.append(System.lineSeparator());
            sb.append("    \"class\": \"").append(escapeJson(className)).append("\",");
            sb.append(System.lineSeparator());
            sb.append("    \"tool\": ");
            sb.append(item.getDetectingTool());
            sb.append(",");
            sb.append(System.lineSeparator());
            sb.append("    \"refactoringInfo\": \"").append(escapeJson(item.getRefactoringInfo())).append("\",");
            sb.append(System.lineSeparator());
            // Embed description as raw JSON (no escaping, no surrounding quotes)
            sb.append("    \"description\": ");
            sb.append(item.getDescription());
            sb.append(",");
            sb.append(System.lineSeparator());
            sb.append("    \"line\": \"").append(item.getLine()).append("\"");
            sb.append(",");
            sb.append(System.lineSeparator());
            sb.append("    \"branchLine\": \"").append(item.getCommitLine()).append("\"");
            sb.append(System.lineSeparator());
            sb.append("  }");
            if (i < items.size() - 1) {
                sb.append(",");
            }
            sb.append(System.lineSeparator());
        }

        sb.append("]");

        // Ensure parent directories exist
        Path parent = outputPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        // Write the content to the file using UTF-8 encoding
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            writer.write(sb.toString());
        }
    }

    // Maps the commit enum to "L", "R", or "B"
    private static String mapCommitToBranch(CommitEnum commit) {
        if (commit == CommitEnum.LEFT) {
            return "L";
        } else if (commit == CommitEnum.RIGHT) {
            return "R";
        } else {
            // for MERGE or any other case
            return "B";
        }
    }

    // Escapes special characters for basic JSON compliance
    private static String escapeJson(String s) {
        if (s == null) return "";
        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}