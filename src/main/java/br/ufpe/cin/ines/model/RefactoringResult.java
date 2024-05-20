package br.ufpe.cin.ines.model;

import java.util.ArrayList;
import java.util.List;

public class RefactoringResult {

    private final List<ResultItem> items;
    private boolean isRefactoring;
    private String description;

    public RefactoringResult() {
        this.items = new ArrayList<>();
    }

    public void addItem(ResultItem item) {
        this.items.add(item);
    }

    public boolean isRefactoring() {
        return !this.items.isEmpty();
    }

    public List<ResultItem> getItems() {
        return items;
    }
}
