package br.ufpe.cin.ines.model;

import br.ufpe.cin.ines.engine.RefactoringFinder;

import java.nio.file.Path;

public class RefactoringParams {

    private final String repositoryUrl;

    private final Path localPath;

    private final String initialCommit;

    private final String finalCommit;

    private final String classname;

    private final int line;

    public RefactoringParams(String repositoryUrl, Path localPath, String initialCommit, String finalCommit, String classname, int line) {
        this.repositoryUrl = repositoryUrl;
        this.localPath = localPath;
        this.initialCommit = initialCommit;
        this.finalCommit = finalCommit;
        this.classname = classname;
        this.line = line;
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

    public int getLine() {
        return line;
    }
}
