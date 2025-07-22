package br.ufpe.cin.ines.engine.refactoringminer;

import br.ufpe.cin.ines.engine.RefactoringFinder;
import br.ufpe.cin.ines.model.RefactoringParams;
import br.ufpe.cin.ines.model.RefactoringResult;
import br.ufpe.cin.ines.model.ResultItem;
import gr.uom.java.xmi.diff.CodeRange;
import gr.uom.java.xmi.diff.UMLModelDiff;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.api.PurityCheckResult;
import org.refactoringminer.api.PurityChecker;
import org.refactoringminer.api.RefactoringType;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import org.eclipse.jgit.lib.Repository;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RefactoringMinerAdapter extends RefactoringFinder {

    public RefactoringMinerAdapter(RefactoringParams params) {
        super(params);
    }

    @Override
    public RefactoringResult execute() {
        RefactoringResult result = new RefactoringResult();

        String classFilePath = this.getParams().getClassname().replaceAll("\\.", "/");

        try {
            GitService gitService = new GitServiceImpl();
            try (Repository repo = gitService.openRepository(this.getParams().getLocalPath().toString())) {
                GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();

                if (this.getParams().getInitialCommit().equals(this.getParams().getFinalCommit())) {
                    miner.detectAtCommit(repo, this.getParams().getFinalCommit(), getRefactoringHandler(classFilePath, result));
                } else {
                    miner.detectBetweenCommits(repo, this.getParams().getInitialCommit(), this.getParams().getFinalCommit(), getRefactoringHandler(classFilePath, result));
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        return result;
    }

    @Override
    public String getToolName() {
        return "RefactoringMiner";
    }

    private RefactoringHandler getRefactoringHandler(String classFilePath, RefactoringResult result) {
        return new RefactoringHandler() {
            @Override
            public void handleModelDiff(String commitId, List<Refactoring> refactorings, UMLModelDiff modelDiff) {
                refactorings
                        .stream()
                        .filter(ref -> ref.rightSide().stream().anyMatch(i -> i.getFilePath().contains(classFilePath)))
                        .forEach(refactoringItem -> {
                            refactoringItem.rightSide()
                                    .forEach(ref -> {
                                        getParams()
                                                .getLines()
                                                .keySet()
                                                .stream()
                                                .filter(mergeLine -> {
                                                    int mappedLine = getParams().getLines().get(mergeLine);

                                                    if (hasSafeMoreReportedThanRefactoredLines(refactoringItem)) {
                                                        PurityCheckResult result = PurityChecker.check(refactoringItem, refactorings, modelDiff);

                                                        if (result != null && result.isPure()) {
                                                            return mappedLine >= ref.getStartLine() && mappedLine <= ref.getEndLine();
                                                        } else {
                                                            return mappedLine == ref.getStartLine();
                                                        }
                                                    } else {
                                                        return mappedLine == ref.getStartLine();
                                                    }
                                                })
                                                .forEach(mergeLine -> result.addItem(new ResultItem(mergeLine, ref.toString(), getParams().getCommit(), refactoringItem.toString(), getToolName(), getParams().getLines().get(mergeLine))));
                                    });
                        });
            }
        };
    }

    private static final Set<RefactoringType> AGGREGATE_SAFE_TYPES = EnumSet.of(
            RefactoringType.EXTRACT_OPERATION,
            RefactoringType.INLINE_OPERATION,
            RefactoringType.MOVE_OPERATION,
            RefactoringType.PULL_UP_OPERATION,
            RefactoringType.PUSH_DOWN_OPERATION,
            RefactoringType.SPLIT_OPERATION,
            RefactoringType.EXTRACT_AND_MOVE_OPERATION,
            RefactoringType.MOVE_AND_INLINE_OPERATION,
            RefactoringType.MOVE_AND_RENAME_OPERATION
    );

    public static boolean hasSafeMoreReportedThanRefactoredLines(Refactoring ref) {
        return AGGREGATE_SAFE_TYPES.contains(ref.getRefactoringType());
    }
}
