package br.ufpe.cin.ines;

import br.ufpe.cin.ines.engine.RefactoringEngine;
import br.ufpe.cin.ines.git.GitHelper;
import br.ufpe.cin.ines.model.CommitEnum;
import br.ufpe.cin.ines.model.RefactoringResult;
import br.ufpe.cin.ines.model.ResultItem;
import br.ufpe.cin.ines.output.BaseProcessor;
import br.ufpe.cin.ines.output.JsonFileSanitizer;
import br.ufpe.cin.ines.output.OutTxtProcessor;
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
public class ExperimentApp
{
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Use: java ExperimentApp <base_path> <id>");
            System.exit(1);
        }

        String basePath = args[0];
        if (!basePath.endsWith(File.separator)) {
            basePath += File.separator;
        }

        String idArg = args[1];

        String COMMA_DELIMITER = ",";
        List<Long> executionTimes = new ArrayList<>();
        List<String> ids = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(basePath + "input-refactoring.csv"))) {
            String lineText = br.readLine();
            while ((lineText = br.readLine()) != null) {
                String[] values = lineText.split(COMMA_DELIMITER);
                String id = values[0];
                String repositoryUrl = values[1];
                String mergeCommit = values[2];
                String className = values[3];
                int[] left_modifications = readArray(values[4]);
                int[] right_modifications = readArray(values[5]);

                if (!id.equals(idArg)) {
                    continue;
                }

                try {
                    System.out.println("==========================================");
                    System.out.println("RUNNING ID: " + id);

                    RefactoringEngine engine = new RefactoringEngine();
                    long startTime = System.currentTimeMillis();

                    String scenarioPath = basePath + id;
                    RefactoringResult result = engine.run(repositoryUrl, mergeCommit, className, left_modifications, right_modifications);
                    Path saida = Paths.get(scenarioPath, "refactor-result.json");
                    RefactoringResultJsonExporter.exportToJson(result, className, saida);

                    JsonFileSanitizer.processFile(scenarioPath + File.separator + "refactor-result.json");
                    OutTxtProcessor.processOutTxt(scenarioPath);
                    BaseProcessor.process(scenarioPath);

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

            try (PrintWriter pw = new PrintWriter(new FileWriter(basePath + "execution_times.csv", true))) {
                pw.println("id,execution-time");
                for (int i = 0; i < ids.size(); i++) {
                    pw.println(ids.get(i) + "," + executionTimes.get(i));
                }
            }

            System.out.println("execution_times.csv saved.");
        }

        System.out.println("Experiment executed.");
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

