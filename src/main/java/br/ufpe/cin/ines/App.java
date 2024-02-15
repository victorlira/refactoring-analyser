package br.ufpe.cin.ines;

import br.ufpe.cin.ines.engine.RefactoringEngine;
import br.ufpe.cin.ines.model.RefactoringResult;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {
        List<>
        RefactoringEngine engine = new RefactoringEngine();
        RefactoringResult result = engine.run("https://github.com/cucumber/cucumber-jvm.git", "4505c156b6267c1b760deec570ddbfe047b42aa9", "cuke4duke.internal.java.JavaLanguage", 36);

        System.out.println("Is refactoring: " + result.isRefactoring());
    }
}

