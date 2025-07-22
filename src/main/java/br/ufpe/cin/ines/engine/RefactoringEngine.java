package br.ufpe.cin.ines.engine;

import br.ufpe.cin.ines.diff.LineNumberFinder;
import br.ufpe.cin.ines.engine.reextractorplus.ReExtractorPlusAdapter;
import br.ufpe.cin.ines.engine.refactoringminer.RefactoringMinerAdapter;
import br.ufpe.cin.ines.git.GitHelper;
import br.ufpe.cin.ines.model.CommitEnum;
import br.ufpe.cin.ines.model.RefactoringParams;
import br.ufpe.cin.ines.model.RefactoringResult;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;

public class RefactoringEngine {

    public RefactoringEngine() {
    }

    public RefactoringResult run(String repository, String mergeCommit, String className, int[] leftModifications, int[] rightModifications) throws IOException {
        GitHelper git = new GitHelper(repository);

        System.out.print("Cloning repository...");
        git.cloneIfNotExists();
        git.clearRepository(mergeCommit);

        System.out.print("Mapping lines...");
        LineNumberFinder lineFinder = new LineNumberFinder(git);

        System.out.print("Getting B, L and R commits...");
        String leftCommit = git.getLeftCommit(mergeCommit);
        String rightCommit = git.getRightCommit(mergeCommit);
        String baseCommit = git.getBaseCommit(leftCommit, rightCommit);

        System.out.print("Squashing commits...");
        String squashedLeftCommit = git.getSquashedCommit(baseCommit, leftCommit);
        Map<Integer, Integer> leftMappingLines = new Hashtable<>();
        Arrays.stream(leftModifications).forEach(originalLine -> leftMappingLines.put(originalLine, lineFinder.find(mergeCommit, squashedLeftCommit, className, originalLine)));

        String squashedRightCommit = git.getSquashedCommit(baseCommit, rightCommit);
        Map<Integer, Integer> rightMappingLines = new Hashtable<>();
        Arrays.stream(rightModifications).forEach(originalLine -> rightMappingLines.put(originalLine, lineFinder.find(mergeCommit, squashedRightCommit, className, originalLine)));


        RefactoringFinder[] finders =  {
                new RefactoringMinerAdapter(new RefactoringParams(repository, git.getLocalPath(), baseCommit, squashedLeftCommit, className, leftMappingLines, CommitEnum.LEFT)),
                new RefactoringMinerAdapter(new RefactoringParams(repository, git.getLocalPath(), baseCommit, squashedRightCommit, className, rightMappingLines, CommitEnum.RIGHT)),
                new ReExtractorPlusAdapter(new RefactoringParams(repository, git.getLocalPath(), baseCommit, squashedLeftCommit, className, leftMappingLines, CommitEnum.LEFT)),
                new ReExtractorPlusAdapter(new RefactoringParams(repository, git.getLocalPath(), baseCommit, squashedRightCommit, className, rightMappingLines, CommitEnum.RIGHT)),
                //new RefDiffAdapter(new RefactoringParams(repository, git.getLocalPath(), baseCommit, leftCommit, className, leftLines)),
                //new RefDiffAdapter(new RefactoringParams(repository, git.getLocalPath(), baseCommit, rightCommit, className, rightLines))
        };

        return this.seekRefactorings(finders);
    }

    private RefactoringResult seekRefactorings(RefactoringFinder[] finders) {
        RefactoringResult aggregated = new RefactoringResult();

        for (RefactoringFinder finder : finders) {
            System.out.println("▶ Executing finder: " + finder.getClass().getSimpleName());

            final RefactoringResult[] holder = new RefactoringResult[1];

            Thread t = new Thread(() -> {
                try {
                    holder[0] = finder.execute();
                } catch (Throwable ex) {
                    System.err.println("✖ Exception in " + finder.getClass().getSimpleName() + ": " + ex.getMessage());
                }
            }, finder.getClass().getSimpleName() + "-Thread");

            t.start();

            try {
                t.join(TimeUnit.MINUTES.toMillis(5));
            } catch (InterruptedException ignored) { }

            if (t.isAlive()) {
                System.err.println("⚠ Timeout (5 min) on " + finder.getClass().getSimpleName() + " — interrupting...");
                t.interrupt();

                try {
                    t.join(1000);
                } catch (InterruptedException ignored) { }

                if (t.isAlive()) {
                    System.err.println("‼ Still alive — forcing stop via reflection");
                    forceStopThread(t);
                }
            } else {
                RefactoringResult r = holder[0];
                if (r != null) {
                    r.getItems().forEach(aggregated::addItem);
                }
            }
        }

        return aggregated;
    }

    /**
     * Invoca Thread.stop(Throwable) por reflection para não
     * provocar erro de compilação no IDE.
     */
    private void forceStopThread(Thread thread) {
        try {
            Method m = Thread.class.getDeclaredMethod("stop", Throwable.class);
            m.setAccessible(true);
            m.invoke(thread, new ThreadDeath());
            System.err.println("‼ Thread stopped: " + thread.getName());
        } catch (Exception ex) {
            System.err.println("✖ Failed to force-stop " + thread.getName() + ": " + ex.getMessage());
        }
    }

}
