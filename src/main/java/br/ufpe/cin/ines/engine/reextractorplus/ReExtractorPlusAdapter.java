package br.ufpe.cin.ines.engine.reextractorplus;

import br.ufpe.cin.ines.engine.RefactoringFinder;
import br.ufpe.cin.ines.model.RefactoringParams;
import br.ufpe.cin.ines.model.RefactoringResult;
import br.ufpe.cin.ines.model.ResultItem;
import org.eclipse.jgit.lib.Repository;

import java.util.List;

public class ReExtractorPlusAdapter extends RefactoringFinder {

    public ReExtractorPlusAdapter(RefactoringParams params) {
        super(params);
    }

    @Override
    public RefactoringResult execute() {
        RefactoringResult result = new RefactoringResult();

        String classFilePath = this.getParams().getClassname().replaceAll("\\.", "/");

        try {
            org.remapper.service.GitService gitService = new org.remapper.util.GitServiceImpl();
            try (Repository repo = gitService.openRepository(this.getParams().getLocalPath().toString())) {

                org.reextractor.service.RefactoringExtractorService service = new org.reextractor.service.RefactoringExtractorServiceImpl();
                service.detectAtCommit(repo, this.getParams().getFinalCommit(), getRefactoringHandler(classFilePath, result));
            }
        } catch (Throwable ex) { ex.printStackTrace(); }

        return result;
    }

    @Override
    public String getToolName() {
        return "ReExtractorPlus";
    }

    private org.reextractor.handler.RefactoringHandler getRefactoringHandler(String classFilePath, RefactoringResult result) {
        return new org.reextractor.handler.RefactoringHandler() {
            @Override
            public void handle(String commitId, org.remapper.dto.MatchPair matchPair, List<org.reextractor.refactoring.Refactoring> refactorings) {
                try {
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
                                                    .filter(mergeLine ->  getParams().getLines().get(mergeLine) == ref.getStartLine() )
                                                    .forEach(mergeLine -> result.addItem(new ResultItem(mergeLine, ref.toString(), getParams().getCommit(), refactoringItem.toString(), getToolName(), getParams().getLines().get(mergeLine))));
                                        });
                            });
                } catch (Throwable ex) { System.out.println("ERROR: Result doesn't have right side."); }
            }

            @Override
            public void handleException(String commit, Exception e) {
                System.err.println("Error processing commit " + commit);
                e.printStackTrace(System.err);
            }
        };
    }
}
