package de.uulm.sp.fmc.as4moco.tools;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class FoldGenerator {

    private final static String scenarioOutFolder = "/tmp/pycharm_project_349/examples/MCC2022_T1_randomSplits/Training_";
    private final static String scenarioInFolder = "/tmp/pycharm_project_349/examples/MCC2022_Track1_complete";
    private final static String csvFile = "/tmp/pycharm_project_349/examples/MCC2022_T1_randomSplits/split.csv";

    private final static String algoRuns = "algorithm_runs.arff";
    private final static String featureCosts = "feature_costs.arff";
    private final static String featureStatus = "feature_runstatus.arff";
    private final static String featureValues = "feature_values.arff";

    public static void main(String[] args) throws IOException {
        try (CSVParser parser = CSVParser.parse(new File(csvFile), Charset.defaultCharset(), CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader().setAllowMissingColumnNames(true).setSkipHeaderRecord(true).build())) {

            Map<Integer, List<Integer>> splitMap = new HashMap<>();
            parser.stream().forEach(e -> splitMap.computeIfAbsent(Integer.valueOf(e.get("Split")), k -> new ArrayList<>()).add(Integer.valueOf(e.get("InstanceNo"))));

            splitMap.forEach( (split, cnfsToRemove) -> {
                handleFile(new File(scenarioInFolder + File.separator + algoRuns), new File(scenarioOutFolder + split + File.separator + algoRuns), cnfsToRemove);
                handleFile(new File(scenarioInFolder + File.separator + featureCosts), new File(scenarioOutFolder  + split+ File.separator + featureCosts), cnfsToRemove);
                handleFile(new File(scenarioInFolder + File.separator + featureStatus), new File(scenarioOutFolder  + split+ File.separator + featureStatus), cnfsToRemove);
                handleFile(new File(scenarioInFolder + File.separator + featureValues), new File(scenarioOutFolder  + split+ File.separator + featureValues), cnfsToRemove);
            });


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
