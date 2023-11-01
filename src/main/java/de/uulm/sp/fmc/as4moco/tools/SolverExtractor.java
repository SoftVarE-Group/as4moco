package de.uulm.sp.fmc.as4moco.tools;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class SolverExtractor {

    private static final File algoRuns = new File("/tmp/pycharm_project_349/examples/MCC2022_Track1_complete/algorithm_runs.arff");
    private static final File splitFile = new File("/tmp/pycharm_project_349/examples/MCC2022_T1_randomSplits/splits.txt");


    public static void main(String[] args) throws IOException {
        getOracleList(algoRuns).forEach(System.out::println);
        System.out.println("----------------------------------------------");
        getSBSwithSplits(algoRuns, splitFile).forEach(System.out::println);


    }

    private static List<String> getOracleList(File file) throws IOException {

        try (BufferedReader in = new BufferedReader(new FileReader(file))) {

            for (int i = 0; i < 9; i++) {
                in.readLine();
            }

            Map<Integer, SolverTuple> map = in.lines().map(e -> e.split(",")).map(e -> new SolverTuple(Integer.parseInt(e[0].substring(14, 17)), e[2], Double.parseDouble(e[3])))
                    .collect(Collectors.toMap(SolverTuple::instance, e -> e, (f,s) -> f.runtime < s.runtime ? f : s));
            return map.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).map(e -> e.getValue().solver()).toList();
        }

    }

    private static List<String> getSBSwithSplits(File input, File splits) throws IOException {
        try (BufferedReader in = new BufferedReader(new FileReader(input));
             BufferedReader splitIn = new BufferedReader(new FileReader(splits))) {

            for (int i = 0; i < 2; i++) {
                splitIn.readLine();
            }
            Map<Integer, List<Integer>> splitMap = new HashMap<>();
            String line;
            int split = 0;
            while ( (line = splitIn.readLine()) != null){
                if (line.contains("Split")) split = Integer.parseInt(String.valueOf(line.charAt(6)));
                else if (!line.isEmpty()) {
                    splitMap.computeIfAbsent(split, k -> new ArrayList<>()).add(Integer.parseInt(line));
                }
            }

            //--------------------
            for (int i = 0; i < 9; i++) {
                in.readLine();
            }


            List<SolverTuple> solvers = in.lines().map(e -> e.split(",")).map(e -> new SolverTuple(Integer.parseInt(e[0].substring(14, 17)), e[2], Double.parseDouble(e[3]))).toList();

            return splitMap.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).map(entry -> {
                int i = entry.getKey();
                List<Integer> splitEntries = entry.getValue();

                Map<String, Double> solverQualityMap = solvers.stream().filter(e -> splitEntries.contains(e.instance)).collect(Collectors.toMap(SolverTuple::solver, SolverTuple::runtime, Double::sum));
                return solverQualityMap.entrySet().stream().min(Comparator.comparingDouble(Map.Entry::getValue)).orElseThrow().getKey();
            }).toList();
        }
    }


    private record SolverTuple(int instance, String solver, double runtime){

    }

}
