package br.ufpe.cin.ines.engine;

import br.ufpe.cin.ines.diff.LineNumberFinder;
import br.ufpe.cin.ines.engine.refactoringminer.RefactoringMinerAdapter;
import br.ufpe.cin.ines.engine.refdiff.RefDiffAdapter;
import br.ufpe.cin.ines.git.GitHelper;
import br.ufpe.cin.ines.model.CommitEnum;
import br.ufpe.cin.ines.model.RefactoringParams;
import br.ufpe.cin.ines.model.RefactoringResult;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

public class RefactoringEngine {


    public RefactoringResult run(String repository, String mergeCommit, String className, int[] leftModifications, int[] rightModifications) throws IOException {
        GitHelper git = new GitHelper(repository);

        git.cloneIfNotExists();

        LineNumberFinder lineFinder = new LineNumberFinder(git);

        List<Integer> allOriginalLines = new ArrayList<>();
        for (int l : leftModifications) { allOriginalLines.add(l); }
        for (int l : rightModifications) { allOriginalLines.add(l); }

        Map<Integer, Integer> leftMappingLines = new Hashtable<>();
        String leftCommit = git.getLeftCommit(mergeCommit);
        Arrays.stream(leftModifications).forEach(originalLine -> leftMappingLines.put(originalLine, lineFinder.find(mergeCommit, leftCommit, className, originalLine)));

        String rightCommit = git.getRightCommit(mergeCommit);

        Map<Integer, Integer> rightMappingLines = new Hashtable<>();
        Arrays.stream(rightModifications).forEach(originalLine -> rightMappingLines.put(originalLine, lineFinder.find(mergeCommit, rightCommit, className, originalLine)));

        String baseCommit = git.getBaseCommit(leftCommit, rightCommit);

        RefactoringFinder[] finders =  {
                //new RefactoringMinerAdapter(new RefactoringParams(repository, git.getLocalPath(), mergeCommit, mergeCommit, className, allOriginalLines)),
                new RefactoringMinerAdapter(new RefactoringParams(repository, git.getLocalPath(), baseCommit, leftCommit, className, leftMappingLines, CommitEnum.LEFT)),
                new RefactoringMinerAdapter(new RefactoringParams(repository, git.getLocalPath(), baseCommit, rightCommit, className, rightMappingLines, CommitEnum.RIGHT)),
                //new RefDiffAdapter(new RefactoringParams(repository, git.getLocalPath(), baseCommit, leftCommit, className, leftLines)),
                //new RefDiffAdapter(new RefactoringParams(repository, git.getLocalPath(), baseCommit, rightCommit, className, rightLines))
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
