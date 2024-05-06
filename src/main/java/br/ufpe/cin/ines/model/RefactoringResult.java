package br.ufpe.cin.ines.model;

public class RefactoringResult {

    private boolean isRefactoring;
    private String description;

    public boolean isRefactoring() {
        return isRefactoring;
    }

    public void setRefactoring(boolean refactoring) {
        isRefactoring = refactoring;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
