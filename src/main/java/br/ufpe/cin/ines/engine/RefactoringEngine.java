package br.ufpe.cin.ines.engine;

import br.ufpe.cin.ines.diff.LineNumberFinder;
import br.ufpe.cin.ines.engine.refactoringminer.RefactoringMinerAdapter;
import br.ufpe.cin.ines.engine.refdiff.RefDiffAdapter;
import br.ufpe.cin.ines.git.GitHelper;
import br.ufpe.cin.ines.model.RefactoringParams;
import br.ufpe.cin.ines.model.RefactoringResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class RefactoringEngine {


    public RefactoringResult run(String repository, String mergeCommit, String className, int[] leftModifications, int[] rightModifications) throws IOException {
        GitHelper git = new GitHelper(repository);

        git.cloneIfNotExists();

        LineNumberFinder lineFinder = new LineNumberFinder(git);

        List<Integer> allOriginalLines = new ArrayList<>();
        for (int l : leftModifications) { allOriginalLines.add(l); }
        for (int l : rightModifications) { allOriginalLines.add(l); }

        List<Integer> leftLines = new ArrayList<>();
        String leftCommit = git.getLeftCommit(mergeCommit);
        allOriginalLines.forEach(line -> leftLines.add(lineFinder.find(mergeCommit, leftCommit, className, line)));

        String rightCommit = git.getRightCommit(mergeCommit);

        List<Integer> rightLines = new ArrayList<>();
        allOriginalLines.forEach(line -> rightLines.add(lineFinder.find(mergeCommit, rightCommit, className, line)));

        String baseCommit = git.getBaseCommit(leftCommit, rightCommit);

        RefactoringFinder[] finders =  {
                new RefactoringMinerAdapter(new RefactoringParams(repository, git.getLocalPath(), mergeCommit, mergeCommit, className, allOriginalLines)),
                new RefactoringMinerAdapter(new RefactoringParams(repository, git.getLocalPath(), baseCommit, leftCommit, className, leftLines)),
                new RefactoringMinerAdapter(new RefactoringParams(repository, git.getLocalPath(), baseCommit, rightCommit, className, rightLines)),
                //new RefDiffAdapter(new RefactoringParams(repository, git.getLocalPath(), baseCommit, leftCommit, className, line)),
                //new RefDiffAdapter(new RefactoringParams(repository, git.getLocalPath(), baseCommit, rightCommit, className, line))
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
