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
        int line = 0;

        String javaFile = this.getJavaFileName(fileName);
        String[] originalFileContent = this.getJavaClassContent(originalCommit, javaFile, fileName);
        String originalLineContent = originalFileContent[originalLineNumber - 1];

        String[] searchingFileContent = this.getJavaClassContent(searchingCommit, javaFile, fileName);
        return this.findLine(searchingFileContent, originalLineContent, originalLineNumber);
    }

    private int findLine(String[] fileContent, String lineContent, int originalLineNumber) {
        int result = -1;

        List<Integer> lines = new ArrayList<>();

        lineContent = lineContent.trim();
        for (int i = 1; i <= fileContent.length; i++) {
            if (StringMatch.isMatch(fileContent[i - 1].trim(), lineContent)) {
                lines.add(i);
            }
        }

        if (!lines.isEmpty()) {
            result = this.findNearestNumber(lines, originalLineNumber);
        }

        return result;
    }

    public int findNearestNumber(List<Integer> list, int number) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("List cannot be null");
        }

        int nearestNumber = list.get(0);
        int minimalDifference = Math.abs(number - nearestNumber);

        for (int item : list) {
            int difference = Math.abs(number - item);
            if (difference < minimalDifference) {
                minimalDifference = difference;
                nearestNumber = item;
            }
        }

        return nearestNumber;
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
