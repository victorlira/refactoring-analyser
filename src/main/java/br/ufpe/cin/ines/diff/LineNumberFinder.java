package br.ufpe.cin.ines.diff;

import br.ufpe.cin.ines.diff.StringMatch;
import br.ufpe.cin.ines.git.GitHelper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class LineNumberFinder {

    private GitHelper git;

    public LineNumberFinder(GitHelper git) {
        this.git = git;
    }

    public int find(String originalCommit, String searchingCommit, String fileName, int originalLineNumber) {
        String javaFile = this.getJavaFileName(fileName);
        String[] originalFileContent = this.getJavaClassContent(originalCommit, javaFile, fileName);
        String originalLineContent = originalFileContent[originalLineNumber - 1];

        String[] searchingFileContent = this.getJavaClassContent(searchingCommit, javaFile, fileName);
        return this.findLine(searchingFileContent, originalLineContent, originalLineNumber);
    }

    private int findLine(String[] fileContent, String lineContent, int originalLineNumber) {
        int result = -1;

        Map<Integer, Double> linesToMatchPertecentage = new Hashtable<>();
        List<Integer> lines = new ArrayList<>();

        lineContent = lineContent.trim();
        for (int i = 1; i <= fileContent.length; i++) {
            if (StringMatch.isMatch(fileContent[i - 1].trim(), lineContent)) {
                lines.add(i);
                linesToMatchPertecentage.put(i, StringMatch.getMatch(fileContent[i - 1].trim(), lineContent));
            }
        }

        if (!lines.isEmpty()) {
            result = this.findNearestNumber(linesToMatchPertecentage, originalLineNumber);
        }

        return result;
    }

    public int findNearestNumber(Map<Integer, Double> linesToMatchPercentage, int number) {
        if (linesToMatchPercentage == null || linesToMatchPercentage.isEmpty()) {
            throw new IllegalArgumentException("Map cannot be null or empty");
        }

        int bestLine = 0;
        double bestPercentage = -1.0;
        int bestDistance = Integer.MAX_VALUE;
        boolean first = true;

        for (Map.Entry<Integer, Double> entry : linesToMatchPercentage.entrySet()) {
            int line = entry.getKey();
            double percentage = entry.getValue();

            if (percentage < 0.0 || percentage > 100.0) {
                throw new IllegalArgumentException("Percentage must be between 0 and 100 (inclusive)");
            }

            int distance = Math.abs(number - line);

            if (first) {
                bestLine = line;
                bestPercentage = percentage;
                bestDistance = distance;
                first = false;
                continue;
            }


            if (percentage > bestPercentage) {
                bestLine = line;
                bestPercentage = percentage;
                bestDistance = distance;
            } else if (percentage == bestPercentage && distance < bestDistance) {
                bestLine = line;
                bestDistance = distance;
            }
        }

        return bestLine;
    }

    private String[] getJavaClassContent(String commit, String javaFileName, String classFullname) {
        String[] result = null;

        git.checkoutCommit(commit);

        File root  = new File(git.getLocalPath().toFile().getAbsolutePath());
        String file = this.findFilePath(root, javaFileName, classFullname);
        Path filePath = Paths.get(file);
        Charset charset = StandardCharsets.UTF_8;

        List<String> lines = null;
        try {
            lines = Files.readAllLines(filePath, charset);
            result = lines.toArray(new String[lines.size()]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }
    private String findFilePath(File root, String fileName, String classFullname) {
        File[] files = root.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String foundPath = findFilePath(file, fileName, classFullname);

                    if (foundPath != null) {
                        return foundPath;
                    }
                } else {
                    if (file.getName().equals(fileName) && this.getPackages(classFullname).stream().allMatch(p -> file.toString().contains(p))) {
                            return file.getAbsolutePath();
                    }
                }
            }
        }

        return null;
    }

    private String getJavaFileName(String fullName) {
        String[] array = fullName.split("\\.");
        return array[array.length - 1] + ".java";
    }

    private List<String> getPackages(String fullName) {
        return Arrays.asList(fullName.split("\\."));
    }
}
