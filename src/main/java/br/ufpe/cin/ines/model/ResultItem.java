package br.ufpe.cin.ines.model;

public class ResultItem {

    private final int line;
    private final String description;
    private final CommitEnum commit;

    public ResultItem(int line, String description, CommitEnum item) {
        this.line = line;
        this.description = description;
        this.commit = item;
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
}
