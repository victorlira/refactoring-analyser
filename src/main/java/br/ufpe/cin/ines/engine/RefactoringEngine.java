package br.ufpe.cin.ines.engine;

import br.ufpe.cin.ines.git.GitHelper;
import br.ufpe.cin.ines.model.RefactoringParams;
import br.ufpe.cin.ines.model.RefactoringResult;

import java.io.IOException;

import org.eclipse.jgit.lib.Repository;

public class RefactoringEngine {


    public RefactoringResult run(String repository, String mergeCommit, String className, int line) throws IOException {
        GitHelper git = new GitHelper(repository);

        git.cloneIfNotExists();

        String leftCommit = git.getLeftCommit(mergeCommit);
        String rightCommit = git.getRightCommit(mergeCommit);
        String baseCommit = git.getBaseCommit(leftCommit, rightCommit);

        RefactoringFinder[] finders =  {
                /*new RefactoringMinerAdapter(new RefactoringParams(repository, git.getLocalPath(), baseCommit, leftCommit, className, line)),
                new RefactoringMinerAdapter(new RefactoringParams(repository, git.getLocalPath(), baseCommit, rightCommit, className, line)),*/
                new RefDiffAdapter(new RefactoringParams(repository, git.getLocalPath(), baseCommit, leftCommit, className, line)),
                new RefDiffAdapter(new RefactoringParams(repository, git.getLocalPath(), baseCommit, rightCommit, className, line))
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
