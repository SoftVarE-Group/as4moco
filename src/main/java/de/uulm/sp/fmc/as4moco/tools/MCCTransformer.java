package de.uulm.sp.fmc.as4moco.tools;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;


import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;


public class MCCTransformer {

    public static final double TIMEOUT = 3601;

    public static final Path csvFile = Path.of(""); //TODO add
    public static final Path outputFile = Path.of(""); //TODO add


    private record SolverEntry( String instance, String solver, double walltime) {

    }
    private record KeyEntry( String instance, String solver) {

    }

    public static void main(String[] args) {



        try (CSVParser parser = CSVParser.parse(csvFile, Charset.defaultCharset(), CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader().setAllowMissingColumnNames(true).setSkipHeaderRecord(true).build());
             CSVPrinter printer = new CSVPrinter(Files.newBufferedWriter(outputFile, Charset.defaultCharset(), StandardOpenOption.CREATE), CSVFormat.Builder.create().build())){

            HashMap<KeyEntry, Double> entryMap = new HashMap<>(1000);
            HashSet<String> instances = new HashSet<>();
            HashSet<String> solvers = new HashSet<>();

            parser.stream().forEach(e -> {
                String benchmark = e.get("benchmark");
                String solver = e.get("solver")+"/"+e.get("configuration");

                instances.add(benchmark);
                solvers.add(solver);

                KeyEntry keyEntry = new KeyEntry(benchmark, solver);
                double walltime = Double.parseDouble(e.get("wall_time"));

                switch (e.get("status")){
                    case "complete" -> {
                        if (Objects.equals(e.get("corr"), "1")) entryMap.put(keyEntry, walltime);
                        else entryMap.put(keyEntry, TIMEOUT);
                    }
                    case "memout" -> {
                        entryMap.put(keyEntry, TIMEOUT);
                    }
                    case "timeout (cpu)", "timeout (wallclock)" -> {
                        entryMap.put(keyEntry, TIMEOUT);
                    }
                    default -> {
                        throw new RuntimeException(e.toString());
                    }
                }
            });
            printer.print("instance");
            printer.printRecord(solvers.stream().sorted());
            instances.stream().sorted().forEach(instance ->{
                try {
                    printer.print(instance);
                    printer.printRecord(solvers.stream().sorted().map(solver -> entryMap.getOrDefault(new KeyEntry(instance, solver), TIMEOUT+110)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



}
