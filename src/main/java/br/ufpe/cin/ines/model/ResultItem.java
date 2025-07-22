package br.ufpe.cin.ines.model;

import org.refactoringminer.api.RefactoringType;

public class ResultItem {

    private final int line;

    private final int commitLine;
    private final String description;
    private final CommitEnum commit;

    private final String refactoringInfo;

    private final String detectingTool;

    public ResultItem(int line, String description, CommitEnum item, String refactoringInfo, String detectingTool, int commitLine) {
        this.line = line;
        this.description = description;
        this.commit = item;
        this.refactoringInfo = refactoringInfo;
        this.detectingTool = detectingTool;
        this.commitLine = commitLine;
    }

    public int getLine() {
        return line;
    }

    public String getDescription() {
        return description;
    }

    public CommitEnum getCommit() {
        return commit;
    }

    public String getRefactoringInfo() { return refactoringInfo; }

    public String getDetectingTool() {
        return detectingTool;
    }

    public int getCommitLine() {
        return commitLine;
    }
}
