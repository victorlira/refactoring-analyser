package br.ufpe.cin.ines;

import br.ufpe.cin.ines.engine.RefactoringEngine;
import br.ufpe.cin.ines.git.GitHelper;
import br.ufpe.cin.ines.model.CommitEnum;
import br.ufpe.cin.ines.model.RefactoringResult;
import br.ufpe.cin.ines.parser.LogParser;
import refdiff.core.RefDiff;
import refdiff.core.diff.CstDiff;
import refdiff.core.diff.Relationship;
import refdiff.parsers.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        /*String filePath = "/home/victorlira/Documents/results/results/execution-1/outConsole.txt";
        LogParser parser = new LogParser();
        parser.parse(filePath);
        System.exit(0);*/
        String COMMA_DELIMITER = ",";
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("/home/victorlira/Documents/input-refactoring.csv"))) {
            String lineText = br.readLine();
            while ((lineText = br.readLine()) != null) {
                String[] values = lineText.split(COMMA_DELIMITER);

                String id = values[0];
                String repositoryUrl = values[1];
                String mergeCommit = values[2];
                String className = values[3];
                int[] left_modifications = readArray(values[4]);
                int[] right_modifications = readArray(values[5]);

                /*if (Integer.parseInt(id) < 79) {
                    continue;
                }*/
                try {
                    System.out.println("==========================================");
                    System.out.println("RUNNING ID: " + id);

                    RefactoringEngine engine = new RefactoringEngine();
                    RefactoringResult result = engine.run(repositoryUrl, mergeCommit, className, left_modifications, right_modifications);
                    System.out.println("Is refactoring: " + id + ": " + result.isRefactoring());
                    if (result.isRefactoring()) {
                        result.getItems().forEach(item -> System.out.println(item.getCommit() + ": " + item.getLine()));
                        //System.out.println(result.getDescription());
                    }
                    System.out.print("Left: ");
                    printArray(Arrays.stream(left_modifications)
                            .filter(line -> result.getItems().stream().noneMatch(resultItem -> resultItem.getCommit() == CommitEnum.LEFT && resultItem.getLine() == line))
                            .toArray());

                    System.out.print("Right: ");
                    printArray(Arrays.stream(right_modifications)
                            .filter(line -> result.getItems().stream().noneMatch(resultItem -> resultItem.getCommit() == CommitEnum.RIGHT && resultItem.getLine() == line))
                            .toArray());
                } catch (Exception ex) {
                    //ex.printStackTrace();
                    System.out.println("Error in ID:" + id);
                }
            }
        }

        /*String repositoryUrl = "https://github.com/cucumber/cucumber-jvm.git";
        String mergeCommit = "4505c156b6267c1b760deec570ddbfe047b42aa9";
        String className = "cuke4duke.internal.java.JavaLanguage";
        int line = 36;;

        String repositoryUrl = "https://github.com/square/okhttp.git";
        String mergeCommit = "1151c9853ccc3c9c3211c613b9b845b925f8c6a6";
        String className = "com.squareup.okhttp.internal.bytes.GzipSource";
        int line = 138;*/

        /*String id = "48";
        String repositoryUrl = "https://github.com/richardwilly98/elasticsearch-river-mongodb.git";
        String mergeCommit = "6b6ce8e851c6613213c4508c3f277a80649e0c7b";
        String className = "org.elasticsearch.river.mongodb.Indexer";
        int line = 287;

        RefactoringEngine engine = new RefactoringEngine();
        RefactoringResult result = engine.run(repositoryUrl, mergeCommit, className , line);

        System.out.println("ID: " + id + ", IS REFACTORING: " + result.isRefactoring());*/
    }

    private static void printArray(int[] x) {
        String s = "[";
        for (int i : x) {
            s += i + ", ";
        }
        s += "]";
        s = s.replace(", ]", "]");
        System.out.println(s);
    }

    private static int[] readArray(String text) {
        return Arrays.stream(text
                .replaceAll("\\[", "")
                .replaceAll("\\]", "")
                .split(";"))
                .mapToInt(t -> Integer.parseInt(t.trim()))
                .toArray();
    }
}

