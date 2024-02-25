package de.uulm.sp.fmc.as4moco.tools;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.uulm.sp.fmc.as4moco.data.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReRunEvalExtractor {

    public static void main(String[] args) throws IOException {

        File referenceFolder = new File(""); //todo insert run folder
        File runFolder = new File(""); //todo insert run folder
        File outputCSV = new File(runFolder, "results.csv"); //todo insert output file


        generateFoldStats(runFolder, referenceFolder, outputCSV);
    }

    private static void generateFoldStats(File runFolder, File referenceFolder, File outputCSV) throws IOException {
        Map<Integer, List<FullRun>> foldMap;
        try (Stream<Path> pathStream = Files.find(runFolder.toPath(), Integer.MAX_VALUE, ((path, basicFileAttributes) -> path.getFileName().toString().contains("MCC") && basicFileAttributes.isRegularFile()))){
            foldMap = pathStream.map(e -> new SimpleEntry<>(
                    Integer.parseInt(String.valueOf(e.getFileName().toString().charAt(10))),
                    readJsonNew(e.toFile()).toList()
            )).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));

        }

        Map<File, List<SolvingRun>> referenceRuns;
        try (Stream<Path> pathStream = Files.find(referenceFolder.toPath(), 1, ((path, basicFileAttributes) -> path.getFileName().toString().contains("fullEval") && basicFileAttributes.isRegularFile()))){
            referenceRuns = pathStream.flatMap(e -> readJsonOld(e.toFile())).collect(Collectors.groupingBy(SolvingRun::cnfFile));
        }

        generateCSV(referenceRuns, foldMap, outputCSV);
    }

    private static void generateCSV(Map<File, List<SolvingRun>> referenceRuns, Map<Integer, List<FullRun>> solvingMap, File output) {
        List<FullRun> solvingRuns = new ArrayList<>(solvingMap.entrySet().stream().flatMap(e -> e.getValue().stream()).toList());
        solvingRuns.sort(Comparator.comparing(e -> e.cnfFile().getName()));
        Map<File, SolvingRun> sbsRuns = getSBS(referenceRuns, solvingMap);
        Map<File, SolvingRun> oracleRuns = getOracle(referenceRuns);

        try (CSVPrinter csvPrinter = new CSVPrinter(Files.newBufferedWriter(output.toPath(), Charset.defaultCharset(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING), CSVFormat.Builder.create().build())) {

            csvPrinter.printRecord("instance", "as4mocoRun", "sbsRun", "oracleRun", "score", "solver_as4moco", "solver_sbs", "solver_oracle", "as4moco_Pipeline", "instance_hardness");
            
            for (FullRun as4mocoRun : solvingRuns) {
                SolvingRun sbsRun = sbsRuns.get(as4mocoRun.cnfFile());
                SolvingRun oracleRun = oracleRuns.get(as4mocoRun.cnfFile());

                double as4mocoT = getSolverTime(as4mocoRun);
                double sbsT = getSolverTime(sbsRun);
                double oracleT = getSolverTime(oracleRun);
                
                csvPrinter.print(as4mocoRun.cnfFile().getName());
                csvPrinter.print(as4mocoT);
                csvPrinter.print(sbsT);
                csvPrinter.print(oracleT);
                csvPrinter.print((sbsT - oracleT) > 0 ? (as4mocoT - oracleT) / (sbsT - oracleT) : -1d);
                csvPrinter.print(as4mocoRun.bestResponse().solver().orElseThrow());
                csvPrinter.print(sbsRun.solverResponse().solver().orElseThrow());
                csvPrinter.print(oracleRun.solverResponse().solver().orElseThrow());
                csvPrinter.print(switch (as4mocoRun.pipelines().getLast()){
                    case HandledSet h -> "PER_SET";
                    case HandledInstance i -> "PER_INSTANCE";
                    default -> throw new RuntimeException(as4mocoRun.pipelines().getLast().getClass().getName());
                });
                csvPrinter.print(getInstanceHardness(referenceRuns.get(as4mocoRun.cnfFile())));
                csvPrinter.println();
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

   

    private static double getInstanceHardness(List<SolvingRun> referenceRuns) {
        return referenceRuns.stream().mapToDouble(ReRunEvalExtractor::getSolverTime).sum() / referenceRuns.size();
    }

    private static Map<File, SolvingRun> getSBS(Map<File, List<SolvingRun>> referenceRuns, Map<Integer, List<FullRun>> solvingMap){
        Map<File, SolvingRun> retValue = new HashMap<>(referenceRuns.size());
        Set<File> allInstances = referenceRuns.keySet();
        solvingMap.forEach( (fold, list) -> {
            Set<File> relevantInstances = new HashSet<>(allInstances);
            list.stream().map(FullRun::cnfFile).toList().forEach(relevantInstances::remove);
            Map<String, Double> solverQuality = referenceRuns.entrySet().stream()
                    .filter(e -> relevantInstances.contains(e.getKey()))
                    .flatMap(e -> e.getValue().stream())
                    .collect(Collectors.toMap(
                            k -> k.solverResponse().solver().get(),
                            ReRunEvalExtractor::getSolverTime,
                            Double::sum
                    ));
            String minSolver = solverQuality.entrySet().stream().min(Comparator.comparingDouble(Map.Entry::getValue)).get().getKey();
            retValue.putAll(
                    list.stream().map(FullRun::cnfFile).map(referenceRuns::get)
                            .map(e -> e.stream().filter(f -> f.solverResponse().solver().equals(Optional.of(minSolver))).findFirst().get())
                            .collect(Collectors.toMap(SolvingRun::cnfFile, Function.identity()))
            );
        });
        return retValue;
    }

    private static Map<File, SolvingRun> getOracle(Map<File, List<SolvingRun>> referenceRuns){
        return referenceRuns.entrySet().stream()
                .map( e -> new SimpleEntry<>(
                        e.getKey(),
                        e.getValue().stream().min(Comparator.comparingDouble(ReRunEvalExtractor::getSolverTime)).get()))
                .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
    }

    private static Stream<FullRun> readJsonNew(File e) {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JavaTimeModule()).registerModule(new Jdk8Module());
        try {
            List<FullRun> runs =  mapper.readValue(e, mapper.getTypeFactory().constructCollectionType(List.class, FullRun.class));
            return runs.stream();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Stream<SolvingRun> readJsonOld(File e) {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JavaTimeModule()).registerModule(new Jdk8Module());
        try {
            List<SolvingRun> runs =  mapper.readValue(e, mapper.getTypeFactory().constructCollectionType(List.class, SolvingRun.class));
            return runs.stream();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static double getSolverTime(SolvingRun e) {
        return switch (e.solverResponse().status()) {
            case OK -> e.duration();
            case ERROR, TIMEOUT -> 3600;
        };
    }

    private static double getSolverTime(FullRun run) {
        return switch (run.bestResponse().status()) {
            case OK -> run.duration();
            case ERROR, TIMEOUT -> 3600;
        };
    }
}
