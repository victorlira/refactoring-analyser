package br.ufpe.cin.ines.engine.refactoringminer;

import br.ufpe.cin.ines.engine.RefactoringFinder;
import br.ufpe.cin.ines.model.RefactoringParams;
import br.ufpe.cin.ines.model.RefactoringResult;
import gr.uom.java.xmi.diff.CodeRange;
import org.refactoringminer.RefactoringMiner;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import org.eclipse.jgit.lib.Repository;
import refdiff.core.RefDiff;
import refdiff.parsers.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

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
                miner.detectBetweenCommits(repo, this.getParams().getInitialCommit(), this.getParams().getFinalCommit(), new RefactoringHandler() {
                    @Override
                    public void handle(String commitId, List<Refactoring> refactorings) {
                        refactorings
                                .stream()
                                .filter(ref -> ref.rightSide().stream().anyMatch(i -> i.getFilePath().contains(classFilePath)))
                                .flatMap(ref -> ref.rightSide().stream())
                                .forEach(ref -> {
                                    if (getParams().getLine() >= ref.getStartLine() && getParams().getLine() <= ref.getEndLine()) {
                                        result.setRefactoring(true);
                                    }
                                });
                    }
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        return result;
    }
}
