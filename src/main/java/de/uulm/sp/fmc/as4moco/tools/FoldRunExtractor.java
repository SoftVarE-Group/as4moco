package de.uulm.sp.fmc.as4moco.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.uulm.sp.fmc.as4moco.SolvingRun;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FoldRunExtractor {

    public static void main(String[] args) {
        File as4mocoInput = new File("");
        File sbsInput = new File("");
        File oracleInput = new File("");
        File outputFile = new File("");


    }

    private static void extractCSV(File as4mocoInput, File sbsInput, File oracleInput, File outputFile){
       try (CSVPrinter csvPrinter = new CSVPrinter(Files.newBufferedWriter(outputFile.toPath(), Charset.defaultCharset(), StandardOpenOption.CREATE), CSVFormat.Builder.create().build())){
           ObjectMapper mapper = new ObjectMapper();
           mapper.registerModule(new JavaTimeModule()).registerModule(new Jdk8Module());

           //header
           csvPrinter.print("instance");
           csvPrinter.print("as4mocoRun");
           csvPrinter.print("sbsRun");
           csvPrinter.print("oracleRun");
           csvPrinter.print("score");
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

               csvPrinter.print(as4mocoRun.cnfFile().getName());
               csvPrinter.print(as4mocoTime);
               csvPrinter.print(sbsTime);
               csvPrinter.print(oracleTime);
               csvPrinter.print(score);
               csvPrinter.println();
           }


       } catch (IOException e) {
           throw new RuntimeException(e);
       }
    }

}
