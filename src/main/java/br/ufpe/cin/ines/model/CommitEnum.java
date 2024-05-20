package br.ufpe.cin.ines.model;

public enum CommitEnum {
    LEFT("Left"),
    RIGHT("Right"),
    MERGE("Merge");

    private final String name;

    private CommitEnum(String name) {
        this.name = name;
    }

    public boolean equalsName(String otherName) {
        return this.name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
