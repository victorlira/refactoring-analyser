package br.ufpe.cin.ines.model;

import br.ufpe.cin.ines.engine.RefactoringFinder;

import java.nio.file.Path;
import java.util.List;

public class RefactoringParams {

    private final String repositoryUrl;

    private final Path localPath;

    private final String initialCommit;

    private final String finalCommit;

    private final String classname;

    private final List<Integer> lines;

    public RefactoringParams(String repositoryUrl, Path localPath, String initialCommit, String finalCommit, String classname, List<Integer> lines) {
        this.repositoryUrl = repositoryUrl;
        this.localPath = localPath;
        this.initialCommit = initialCommit;
        this.finalCommit = finalCommit;
        this.classname = classname;
        this.lines = lines;
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

    public List<Integer> getLine() {
        return lines;
    }
}
