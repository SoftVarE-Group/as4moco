package de.uulm.sp.fmc.as4moco;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import de.uulm.sp.fmc.as4moco.selection.messages.SolverBudget;
import de.uulm.sp.fmc.as4moco.solver.SolverHandler;
import de.uulm.sp.fmc.as4moco.solver.SolverResponse;
import de.uulm.sp.fmc.as4moco.solver.SolverStatusEnum;
import org.apache.commons.cli.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;


import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        CommandLine commandLine = parseCommandLine(args, generateOptions());

        runNormal(commandLine);
        System.exit(0);

    }

    /**
     * Starts single normal solver run
     * @param commandLine
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private static void runNormal(CommandLine commandLine) throws IOException, ExecutionException, InterruptedException {
        Instant before = Instant.now();

        Instant after;
        try (WorkflowManager workflowManager = new WorkflowManager(new File(commandLine.getOptionValue("modelFile")))) {
            System.out.println("as4moco solution: " + workflowManager.runSolving(new File(commandLine.getOptionValue("cnfFile"))));
            after = Instant.now();
        }

        System.out.println("Duration: " + Duration.between(before, after).toMillis()/1000f + "s");
    }

    /**
     * Generates CNF-Filenames from CSV-Split (Helper for runNormalMultiple)
     * @param csvFile csv file with split
     * @param split split to generate
     * @param cnfFolder folder with cnf files
     * @return list of cnf files
     * @throws IOException
     */
    private static List<File> generateFileNames(File csvFile, int split, File cnfFolder) throws IOException {
        try (CSVParser parser = CSVParser.parse(csvFile, Charset.defaultCharset(), CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader().setAllowMissingColumnNames(true).setSkipHeaderRecord(true).build())) {
            return parser.stream().filter(e -> Integer.parseInt(e.get("Split")) == split)
                    .map(e -> "mc2022_track1_%03d.dimacs".formatted(Integer.parseInt(e.get("InstanceNo"))))
                    .map(e -> new File(cnfFolder.getAbsolutePath() + File.separator + e)).toList();
        }
    }

    /**
     * Runs multiple normal solver runs in batch mode
     * @param cnfs cnfs to analyze
     * @param modelFile model file to use for autofolio
     * @param saveFile file to save to
     * @return list of runs
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private static List<SolvingRun> runNormalMultiple(List<File> cnfs, File modelFile, File saveFile) throws IOException, ExecutionException, InterruptedException {
        List<SolvingRun> solvingRuns = new ArrayList<>(cnfs.size());

        JsonFactory factory = new JsonFactory();


        try (WorkflowManager workflowManager = new WorkflowManager(modelFile);
             JsonGenerator out = factory.createGenerator(saveFile, JsonEncoding.UTF8)) {
            out.writeStartArray();
            for (File cnf : cnfs) {
                System.out.println("Run file "+cnf);
                Instant before = Instant.now();
                SolverResponse solverResponse;
                try {
                    solverResponse = workflowManager.runSolving(cnf);
                } catch (Exception e){
                    solverResponse = new SolverResponse(null, SolverStatusEnum.ERROR, Optional.empty());
                    System.out.println("Error in Run for "+cnf);
                    e.printStackTrace();
                }
                Instant after = Instant.now();

                SolvingRun solvingRun = new SolvingRun(cnf, before, after, Duration.between(before, after).toMillis() / 1000d, solverResponse);
                out.writeObject(solvingRun);
                solvingRuns.add(solvingRun);
            }
            out.writeEndArray();
        }
        return solvingRuns;


    }

    /**
     * Runs analysis of SBS and Oracles from CSV file
     * @param csvFile csv file with data
     * @param cnfFolder folder with cnfs
     * @param sbsOut file to save sbs runs to
     * @param oracleOut file to save oracle runs to
     * @param timeout timeout for solver
     * @throws IOException
     */
    private static void runSBSOracleAnalysis(File csvFile, File cnfFolder,File sbsOut, File oracleOut, int timeout) throws IOException {
        try (CSVParser parser = CSVParser.parse(csvFile, Charset.defaultCharset(), CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader().setAllowMissingColumnNames(true).setSkipHeaderRecord(true).build())) {
            List<RunTask> sbsList = new ArrayList<>();
            List<RunTask> oracleList = new ArrayList<>();
            parser.stream().forEach(e -> {
                File instance = new File( cnfFolder.getAbsolutePath()+ File.separator + "mc2022_track1_%03d.dimacs".formatted(Integer.parseInt(e.get("InstanceNo"))));
                sbsList.add(new RunTask(e.get("SBS"), instance, timeout));
                oracleList.add(new RunTask(e.get("Oracle"), instance, timeout));
            });
            testSolver(sbsList, sbsOut);
            testSolver(oracleList, oracleOut);
        }
    }


    /**
     * Runs daughter solvers
     * @param tasks list of tasks for daughter solvers
     * @param saveFile file to save to
     * @return list of runs
     * @throws IOException
     */
    private static List<SolvingRun> testSolver(List<RunTask> tasks, File saveFile) throws IOException{
        List<SolvingRun> solvingRuns = new ArrayList<>(tasks.size());

        JsonFactory factory = new JsonFactory();


        try (JsonGenerator out = factory.createGenerator(saveFile, JsonEncoding.UTF8)) {
            out.writeStartArray();
            for (RunTask task : tasks) {
                System.out.println("Run task "+task);
                Instant before = Instant.now();
                SolverResponse solverResponse;
                try {
                    solverResponse = SolverHandler.runSolvers(new SolverBudget[]{new SolverBudget(task.solver, task.timeout)}, task.cnf).get(0);
                } catch (Exception e){
                    solverResponse = new SolverResponse(null, SolverStatusEnum.ERROR, Optional.empty());
                    System.out.println("Error in Run of solver "+ task.solver +" for "+task.cnf());
                    e.printStackTrace();
                }
                Instant after = Instant.now();

                SolvingRun solvingRun = new SolvingRun(task.cnf(), before, after, Duration.between(before, after).toMillis() / 1000d, solverResponse);
                out.writeObject(solvingRun);
                solvingRuns.add(solvingRun);
            }
            out.writeEndArray();
        }
        return solvingRuns;
    }

    private static CommandLine parseCommandLine(String[] args, Options options){
        CommandLineParser commandLineParser = new DefaultParser(false);
        try{
            return commandLineParser.parse(options, args);

        } catch (ParseException e) {
            System.err.println("Unexpected exception:" + e.getMessage());
            printHelp(options);
            System.exit(-1);
        }
        return null;
    }

    private static Options generateOptions(){
        Options options = new Options();
        //OptionGroup mainOptions = new OptionGroup();
        //mainOptions.setRequired(true);
        //TODO enbable more functionality, e.g. model training, feature extraction, ...
        options.addOption(Option.builder("modelFile").argName("model").hasArg().desc("Path to trained autofolio model").build());
        options.addOption(Option.builder("cnfFile").argName("cnf").hasArg().desc("Path to cnf").build());


        //options.addOptionGroup(mainOptions);

        return options;
    }

    private static void printHelp(Options options){
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("as4moco", "A simple algorithm selection #SAT solver!", options, "For more information, see associated paper: \n link", true); //TODO fix paper link
    }

    private record RunTask(String solver, File cnf, int timeout){

    }

}