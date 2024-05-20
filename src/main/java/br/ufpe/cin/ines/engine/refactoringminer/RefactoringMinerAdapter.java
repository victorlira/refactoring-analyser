package br.ufpe.cin.ines.engine.refactoringminer;

import br.ufpe.cin.ines.engine.RefactoringFinder;
import br.ufpe.cin.ines.model.RefactoringParams;
import br.ufpe.cin.ines.model.RefactoringResult;
import br.ufpe.cin.ines.model.ResultItem;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import org.eclipse.jgit.lib.Repository;

import java.util.List;
import java.util.Optional;

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

    private RefactoringHandler getRefactoringHandler(String classFilePath, RefactoringResult result) {
        return new RefactoringHandler() {
            @Override
            public void handle(String commitId, List<Refactoring> refactorings) {
                refactorings
                        .stream()
                        .filter(ref -> ref.rightSide().stream().anyMatch(i -> i.getFilePath().contains(classFilePath)))
                        .flatMap(ref -> ref.rightSide().stream())
                        .forEach(ref -> {
                            getParams()
                                    .getLines()
                                    .keySet()
                                    .stream()
                                    .filter(mergeLine -> getParams().getLines().get(mergeLine) >= ref.getStartLine() && getParams().getLines().get(mergeLine) <= ref.getEndLine())
                                    .forEach(mergeLine -> result.addItem(new ResultItem(mergeLine, ref.toString(), getParams().getCommit())));
                        });
            }
        };
    }
}
