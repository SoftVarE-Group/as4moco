package de.uulm.sp.fmc.as4moco.tools;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FoldGenerator {

    private final static String scenarioOutFolder = "/tmp/pycharm_project_349/examples/MCC2022_T1_randomSplits/Training_5";
    private final static String scenarioInFolder = "/tmp/pycharm_project_349/examples/MCC2022_Track1_complete";
    private final static String algoRuns = "algorithm_runs.arff";
    private final static String featureCosts = "feature_costs.arff";
    private final static String featureStatus = "feature_runstatus.arff";
    private final static String featureValues = "feature_values.arff";

    public static void main(String[] args) throws IOException {
        try (BufferedReader sysInReader = new BufferedReader(new InputStreamReader(System.in))) {

            List<Integer> cnfsToRemove = new ArrayList<>(40);
            String line;
            while (!Objects.equals(line = sysInReader.readLine(), "")){
                cnfsToRemove.add(Integer.parseInt(line));
            }

            handleFile(new File(scenarioInFolder + File.separator + algoRuns), new File(scenarioOutFolder + File.separator + algoRuns), cnfsToRemove);
            handleFile(new File(scenarioInFolder + File.separator + featureCosts), new File(scenarioOutFolder + File.separator + featureCosts), cnfsToRemove);
            handleFile(new File(scenarioInFolder + File.separator + featureStatus), new File(scenarioOutFolder + File.separator + featureStatus), cnfsToRemove);
            handleFile(new File(scenarioInFolder + File.separator + featureValues), new File(scenarioOutFolder + File.separator + featureValues), cnfsToRemove);
        }


    }

    public static void handleFile(File input, File output, List<Integer> toRemove){
        try(BufferedReader in = new BufferedReader(new FileReader(input));
            PrintWriter out = new PrintWriter(new FileWriter(output, false))) {
            System.out.println("starting with "+input);
            in.lines().filter(line -> !containsAny(line, toRemove)).forEach(out::println);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static boolean containsAny(String line, List<Integer> toRemove){
        return toRemove.stream().anyMatch(e -> line.contains("%03d.dimacs".formatted(e)));
    }

}
