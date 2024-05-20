package br.ufpe.cin.ines.model;

import java.nio.file.Path;
import java.util.Map;

public class RefactoringParams {

    private final String repositoryUrl;

    private final Path localPath;

    private final String initialCommit;

    private final String finalCommit;

    private final String classname;

    private final Map<Integer, Integer> lines;

    private final CommitEnum commit;

    public RefactoringParams(String repositoryUrl, Path localPath, String initialCommit, String finalCommit, String classname, Map<Integer, Integer> lines, CommitEnum commit) {
        this.repositoryUrl = repositoryUrl;
        this.localPath = localPath;
        this.initialCommit = initialCommit;
        this.finalCommit = finalCommit;
        this.classname = classname;
        this.lines = lines;
        this.commit = commit;
    }
    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public Path getLocalPath() {
        return localPath;
    }

    public String getInitialCommit() {
        return initialCommit;
    }

    public String getFinalCommit() {
        return finalCommit;
    }

    public String getClassname() {
        return classname;
    }

    public Map<Integer, Integer> getLines() {
        return lines;
    }

    public CommitEnum getCommit() {
        return commit;
    }
}
