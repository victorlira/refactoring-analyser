package br.ufpe.cin.ines.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class LogParser {

    public void parse(String filePath) {
        int i = 0;
        try (Scanner reader = new Scanner(new File(filePath))) {
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                if (line.startsWith("Running soot scenario")) {
                    System.out.println("CENÁRIO: " + (i + 1));
                    System.out.println(line);
                    this.parseScenario(reader);
                    if (i++ >= 8)
                        throw new FileNotFoundException();
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void parseScenario(Scanner reader) {
        String line = reader.nextLine();
        while (!line.startsWith("Running soot scenario")) {
            System.out.println(line);
            line = reader.nextLine();
        }
    }
}
