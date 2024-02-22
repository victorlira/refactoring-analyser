package br.ufpe.cin.ines.engine;

import br.ufpe.cin.ines.model.RefactoringParams;
import br.ufpe.cin.ines.model.RefactoringResult;

public abstract class RefactoringFinder {

    private final RefactoringParams params;

    public RefactoringFinder(RefactoringParams params) {
        this.params = params;
    }

    public abstract RefactoringResult execute();

    public RefactoringParams getParams() {
        return params;
    }
}
