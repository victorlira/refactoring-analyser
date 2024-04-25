package br.ufpe.cin.ines.engine.refdiff;

import br.ufpe.cin.ines.engine.RefactoringFinder;
import br.ufpe.cin.ines.git.GitHelper;
import br.ufpe.cin.ines.model.RefactoringParams;
import br.ufpe.cin.ines.model.RefactoringResult;
import org.eclipse.jgit.revwalk.RevCommit;
import refdiff.core.RefDiff;
import refdiff.core.diff.CstDiff;
import refdiff.core.diff.Relationship;
import refdiff.core.io.SourceFileSet;
import refdiff.parsers.java.JavaPlugin;

import java.io.File;

public class RefDiffAdapter extends RefactoringFinder {
    public RefDiffAdapter(RefactoringParams params) {
        super(params);
    }

    @Override
    public RefactoringResult execute() {
        File tempFolder = new File(GitHelper.BASE_FOLDER);
        JavaPlugin javaPlugin = new JavaPlugin(tempFolder);
        RefDiff refDiffJava = new RefDiff(javaPlugin);

        RefactoringResult result = new RefactoringResult();

        File repo = refDiffJava.cloneGitRepository(new File(GitHelper.BASE_FOLDER, this.getParams().getLocalPath().toString()), this.getParams().getRepositoryUrl());

        try {
            CstDiff diff = refDiffJava.computeDiffForCommit(repo, this.getParams().getFinalCommit());
            for (Relationship rel : diff.getRefactoringRelationships()) {
                if (rel.getNodeBefore().getLocalName().equals(this.getParams().getClassname())) {
                    result.setRefactoring(true);
                }
            }
        } catch (RuntimeException rex ) {  }

        return result;
    }
}
