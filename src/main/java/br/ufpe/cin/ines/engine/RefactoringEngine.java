package br.ufpe.cin.ines.engine;

import br.ufpe.cin.ines.diff.LineNumberFinder;
import br.ufpe.cin.ines.engine.refactoringminer.RefactoringMinerAdapter;
import br.ufpe.cin.ines.git.GitHelper;
import br.ufpe.cin.ines.model.RefactoringParams;
import br.ufpe.cin.ines.model.RefactoringResult;

import java.io.IOException;

public class RefactoringEngine {


    public RefactoringResult run(String repository, String mergeCommit, String className, int line) throws IOException {
        GitHelper git = new GitHelper(repository);

        git.cloneIfNotExists();

        LineNumberFinder lineFinder = new LineNumberFinder(git);

        String leftCommit = git.getLeftCommit(mergeCommit);
        int leftLineNumber = lineFinder.find(mergeCommit, leftCommit, className, line);

        String rightCommit = git.getRightCommit(mergeCommit);
        int rightLineNumber = lineFinder.find(mergeCommit, rightCommit, className, line);

        String baseCommit = git.getBaseCommit(leftCommit, rightCommit);

        RefactoringFinder[] finders =  {
                //new RefactoringMinerAdapter(new RefactoringParams(repository, git.getLocalPath(), baseCommit, leftCommit, className, leftLineNumber)),
                new RefactoringMinerAdapter(new RefactoringParams(repository, git.getLocalPath(), baseCommit, rightCommit, className, rightLineNumber))/*,
                new RefDiffAdapter(new RefactoringParams(repository, git.getLocalPath(), baseCommit, leftCommit, className, line)),
                new RefDiffAdapter(new RefactoringParams(repository, git.getLocalPath(), baseCommit, rightCommit, className, line))*/
        };

        return this.seekRefactorings(finders);
    }

    private RefactoringResult seekRefactorings(RefactoringFinder[] finders) {
        RefactoringResult result = null;

        for (RefactoringFinder finder : finders) {
            result = finder.execute();

            if (result.isRefactoring()) {
                break;
            }
        }

        return result;
    }


}
