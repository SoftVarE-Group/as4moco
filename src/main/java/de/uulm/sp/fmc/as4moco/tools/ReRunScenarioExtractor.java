package de.uulm.sp.fmc.as4moco.tools;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.uulm.sp.fmc.as4moco.data.SolvingRun;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Stream;

public class ReRunScenarioExtractor {

    public static void main(String[] args) {


        File folder = new File(""); //todo insert run folder
        HashMap<File, List<SolvingRun>> runs = new HashMap<>();
        Arrays.stream(Objects.requireNonNull(folder.listFiles())).filter(File::isFile).filter(e -> e.getName().startsWith("MCC"))
                .flatMap(ReRunScenarioExtractor::readJson).forEach(e -> {
                    runs.putIfAbsent(e.cnfFile(), new ArrayList<>());
                    runs.get(e.cnfFile()).add(e);
                });
        generateCSV(runs, new File("output.csv"));
    }

    private static void generateCSV(HashMap<File, List<SolvingRun>> runs, File output) {
        try (CSVPrinter csvPrinter = new CSVPrinter(Files.newBufferedWriter(output.toPath(), Charset.defaultCharset(), StandardOpenOption.CREATE), CSVFormat.Builder.create().build())) {


            runs.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().getName())).forEach(e -> {
                String instance = e.getKey().toString();
                e.getValue().forEach(i -> {
                    try {
                        csvPrinter.print(instance);
                        csvPrinter.print(1);
                        csvPrinter.print(i.solverResponse().solver().orElseThrow());
                        csvPrinter.print(switch (i.solverResponse().status()){
                            case OK -> i.duration();
                            case ERROR -> 3650.01d;
                            case TIMEOUT -> 3600.01d;
                        });
                        csvPrinter.print(switch (i.solverResponse().status()) {
                            case OK -> "ok";
                            case ERROR -> "crash";
                            case TIMEOUT -> "timeout";
                        });
                        csvPrinter.println();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
            });


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static Stream<SolvingRun> readJson(File e) {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);;
        mapper.registerModule(new JavaTimeModule()).registerModule(new Jdk8Module());
        try {
            List<SolvingRun> runs =  mapper.readValue(e, mapper.getTypeFactory().constructCollectionType(List.class, SolvingRun.class));
            return runs.stream();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


}
