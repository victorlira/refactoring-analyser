package br.ufpe.cin.ines.git;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.StringJoiner;

public class GitHelper {
    public static final String BASE_FOLDER = "projects";
    private final String repositoryUrl;

    private final Path localPath;

    public GitHelper(String repository) {
        this.repositoryUrl = repository;

        String[] splitRepository = this.repositoryUrl.split("/");
        String repositoryPathString =
                splitRepository[splitRepository.length - 2]
                        .concat("-")
                        .concat(splitRepository[splitRepository.length - 1]);

        this.localPath = Paths.get(BASE_FOLDER).resolve(repositoryPathString);
    }

    public Path getLocalPath() {
        return this.localPath;
    }

    public void cloneIfNotExists() throws IOException {
        if (!Files.exists(this.localPath)) {
            Files.createDirectories(this.localPath);

            this.cloneRepository();
        }
    }

    public String getBaseCommit(String leftCommit, String rightCommit) {
        String shortLeftHash = leftCommit.substring(0, 7);
        String shortRightHash = rightCommit.substring(0, 7);
        return this.executeCommand("git", "merge-base", shortLeftHash, shortRightHash);
    }

    public String getLeftCommit(String mergeCommit) {
        String result = this.getLeftAndRightCommits(mergeCommit);
        return result.split(" ")[0];
    }

    public String getRightCommit(String mergeCommit) {
        String result = this.getLeftAndRightCommits(mergeCommit);
        return result.split(" ")[1];
    }

    private String getLeftAndRightCommits(String mergeCommit) {
        return this.executeCommand("git", "log", "--pretty=%P", "-n" , "1", mergeCommit );
    }

    private void cloneRepository() {
        this.executeCommand("git", "clone", this.repositoryUrl, this.localPath.getFileName().toString());
    }

    private String executeCommand(String ... command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);

            if (Arrays.asList(command).contains("clone")) {
                processBuilder.directory(new File(this.localPath.getParent().toString()));
            } else {
                processBuilder.directory(new File(this.localPath.toString()));
            }

            Process process = processBuilder.start();

            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            InputStream errorStream = process.getErrorStream();
            InputStreamReader errorStreamReader = new InputStreamReader(errorStream);
            BufferedReader errorBufferedReader = new BufferedReader(errorStreamReader);

            String line;
            StringBuilder error = new StringBuilder();
            while ((line = errorBufferedReader.readLine()) != null) {
                error.append(line).append("\n");
            }

            StringBuilder result = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line).append("\n");
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return result.toString().trim();
            } else {
                StringJoiner joiner = new StringJoiner(" ");
                for (String element : command) {
                    joiner.add(element);
                }
                throw new RuntimeException("Error while executing command: " + joiner.toString() + ", Error: " + error);
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
