package br.ufpe.cin.ines;

import br.ufpe.cin.ines.engine.RefactoringEngine;
import br.ufpe.cin.ines.model.RefactoringResult;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {
        RefactoringEngine engine = new RefactoringEngine();
        String repositoryUrl = "https://github.com/cucumber/cucumber-jvm.git";
        String mergeCommit = "4505c156b6267c1b760deec570ddbfe047b42aa9";
        String className = "cuke4duke.internal.java.JavaLanguage";
        int line = 36;

        RefactoringResult result = engine.run(repositoryUrl, mergeCommit, className , line);

        System.out.println("Is refactoring: " + result.isRefactoring());
    }
}

