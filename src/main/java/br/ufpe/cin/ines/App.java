package br.ufpe.cin.ines;

import br.ufpe.cin.ines.engine.RefactoringEngine;
import br.ufpe.cin.ines.git.GitHelper;
import br.ufpe.cin.ines.model.CommitEnum;
import br.ufpe.cin.ines.model.RefactoringResult;
import br.ufpe.cin.ines.model.ResultItem;
import br.ufpe.cin.ines.parser.LogParser;
import br.ufpe.cin.ines.util.RefactoringResultJsonExporter;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;
import refdiff.core.RefDiff;
import refdiff.core.diff.CstDiff;
import refdiff.core.diff.Relationship;
import refdiff.parsers.java.JavaPlugin;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.eclipse.jgit.lib.Repository;
public class App 
{
    public static void main(String[] args) throws Exception {
        String COMMA_DELIMITER = ",";
        List<Long> executionTimes = new ArrayList<>();
        List<String> ids = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("/home/victorlira/Documents/experiment/miningframework/input-refactoring.csv"))) {
            String lineText = br.readLine();
            while ((lineText = br.readLine()) != null) {
                String[] values = lineText.split(COMMA_DELIMITER);
                String id = values[0];
                String repositoryUrl = values[1];
                String mergeCommit = values[2];
                String className = values[3];
                int[] left_modifications = readArray(values[4]);
                int[] right_modifications = readArray(values[5]);

                try {
                    System.out.println("==========================================");
                    System.out.println("RUNNING ID: " + id);

                    RefactoringEngine engine = new RefactoringEngine();
                    long startTime = System.currentTimeMillis();

                    RefactoringResult result = engine.run(repositoryUrl, mergeCommit, className, left_modifications, right_modifications);

                    Path saida = Paths.get("/home/victorlira/Documents/experiment/miningframework/victor-results/"+id, "refactor-result.json");

                    //RefactoringResultJsonExporter.exportToJson(result, className, saida);

                    //printBranchAndLine(result);

                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;

                    System.out.println("Execution time (ms) for ID " + id + ": " + duration);

                    executionTimes.add(duration);
                    ids.add(id);

                    System.out.println("ID: " + id + " PROCESSED.");
                } catch (Exception ex) {
                    System.out.println("Error in ID:" + id);
                    System.out.println(ex.getMessage());
                }
            }
        }

        if (!executionTimes.isEmpty()) {
            System.out.println("\n===== Execution Times Summary =====");
            for (int i = 0; i < executionTimes.size(); i++) {
                System.out.println("ID: " + ids.get(i) + ", Time: " + executionTimes.get(i) + " ms");
            }

            long max = executionTimes.stream().mapToLong(Long::longValue).max().getAsLong();
            long min = executionTimes.stream().mapToLong(Long::longValue).min().getAsLong();
            double avg = executionTimes.stream().mapToLong(Long::longValue).average().getAsDouble();

            System.out.println("Max time: " + max + " ms");
            System.out.println("Min time: " + min + " ms");
            System.out.println("Avg time: " + avg + " ms");

            // Escrever CSV
            try (PrintWriter pw = new PrintWriter(new FileWriter("execution_times.csv"))) {
                pw.println("id,execution-time");
                for (int i = 0; i < ids.size(); i++) {
                    pw.println(ids.get(i) + "," + executionTimes.get(i));
                }
            }

            System.out.println("execution_times.csv saved.");
        }

        System.out.println("Experiment executed.");
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

    public static void printBranchAndLine(RefactoringResult refactoringResult) {
        for (ResultItem item : refactoringResult.getItems()) {
            String branch = mapCommitToBranch(item.getCommit());
            System.out.print(branch + ": " + item.getLine()+ ", ");
        }
        System.out.println();
    }

    // Maps the commit enum to "L", "R", or "B"
    private static String mapCommitToBranch(CommitEnum commit) {
        switch (commit) {
            case LEFT:  return "L";
            case RIGHT: return "R";
            default:    return "B";
        }
    }
}

