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
import java.util.function.Function;
import java.util.stream.Collectors;

public class FoldRunExtractor {

    public static void main(String[] args) {


        File folder = new File("/home/ubuntu/raphael-dunkel-bachelor/data/fold_runs/MCC22_T1_splits_6000I/MCC2022_T1_F5_6000I"); //todo insert run folder
        File as4mocoInput = Arrays.stream(Objects.requireNonNull(folder.listFiles())).filter(File::isFile).filter(e -> e.getName().startsWith("MCC")).findAny().orElseThrow();
        File sbsInput = Arrays.stream(Objects.requireNonNull(folder.listFiles())).filter(File::isFile).filter(e -> e.getName().startsWith("sbs")).findAny().orElseThrow();
        File oracleInput = Arrays.stream(Objects.requireNonNull(folder.listFiles())).filter(File::isFile).filter(e -> e.getName().startsWith("oracle")).findAny().orElseThrow();
        File outputFile = new File(folder, "results.csv");
        extractCSV(as4mocoInput, sbsInput, oracleInput, outputFile);

    }

    private static void extractCSV(File as4mocoInput, File sbsInput, File oracleInput, File outputFile){
       try (CSVPrinter csvPrinter = new CSVPrinter(Files.newBufferedWriter(outputFile.toPath(), Charset.defaultCharset(), StandardOpenOption.CREATE), CSVFormat.Builder.create().build())){
           ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);;
           mapper.registerModule(new JavaTimeModule()).registerModule(new Jdk8Module());


           //header
           csvPrinter.print("instance");
           csvPrinter.print("as4mocoRun");
           csvPrinter.print("sbsRun");
           csvPrinter.print("oracleRun");
           csvPrinter.print("score");
           csvPrinter.print("sbs-oracle");
           csvPrinter.print("sbs-as4moco");
           csvPrinter.print("as4moco-oracle");
           csvPrinter.println();


           List<SolvingRun> as4mocoRuns =  mapper.readValue(as4mocoInput, mapper.getTypeFactory().constructCollectionType(List.class, SolvingRun.class));
           List<SolvingRun> sbsRunsList =  mapper.readValue(sbsInput, mapper.getTypeFactory().constructCollectionType(List.class, SolvingRun.class));
           List<SolvingRun> oracleRunsList =  mapper.readValue(oracleInput, mapper.getTypeFactory().constructCollectionType(List.class, SolvingRun.class));

           Map<File, SolvingRun> sbsRuns = sbsRunsList.stream().collect(Collectors.toMap(SolvingRun::cnfFile, Function.identity()));
           Map<File, SolvingRun> oracleRuns = oracleRunsList.stream().collect(Collectors.toMap(SolvingRun::cnfFile, Function.identity()));
           for (SolvingRun as4mocoRun : as4mocoRuns) {


               double as4mocoTime = as4mocoRun.duration();
               double sbsTime = sbsRuns.get(as4mocoRun.cnfFile()).duration();
               double oracleTime = oracleRuns.get(as4mocoRun.cnfFile()).duration();
               double score = -1;
               if (sbsTime - oracleTime > 0) score = (as4mocoTime - oracleTime) / (sbsTime - oracleTime);
               Optional<String> as4moco = as4mocoRun.solverResponse().solver();
               Optional<String> sbs = sbsRuns.get(as4mocoRun.cnfFile()).solverResponse().solver();
               Optional<String> oracle = oracleRuns.get(as4mocoRun.cnfFile()).solverResponse().solver();




               csvPrinter.print(as4mocoRun.cnfFile().getName());
               csvPrinter.print(as4mocoTime);
               csvPrinter.print(sbsTime);
               csvPrinter.print(oracleTime);
               csvPrinter.print(score);
               csvPrinter.print(compareSolver(sbs, oracle));
               csvPrinter.print(compareSolver(sbs, as4moco));
               csvPrinter.print(compareSolver(as4moco, oracle));
               csvPrinter.println();
           }


       } catch (IOException e) {
           throw new RuntimeException(e);
       }
    }

    private static boolean compareSolver(Optional<String> a, Optional<String> b){
        return a.equals(b);
    }

}
