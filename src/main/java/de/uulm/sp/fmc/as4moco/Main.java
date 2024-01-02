package de.uulm.sp.fmc.as4moco;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.uulm.sp.fmc.as4moco.selection.messages.SolverBudget;
import de.uulm.sp.fmc.as4moco.solver.SolverHandler;
import de.uulm.sp.fmc.as4moco.solver.SolverMap;
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
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        CommandLine commandLine = parseCommandLine(args, generateOptions());

        evaluateScenario(
                new File("/home/ubuntu/as4moco/AutoFolio/examples/MCC2022_T1_randomSplits/split.csv"),
                1,
                new File("/home/ubuntu/MCC2022_T1_cnfs"),
                new File("MCC_T1_S1_fullEval.json"),
                3600
        );

//        runNormal(commandLine);
//        runSBSOracleAnalysis(
//                new File("/home/ubuntu//as4moco/AutoFolio/examples/MCC2022_T1_randomSplits/split.csv"),
//                1,
//                new File("/home/ubuntu/MCC2022_T1_cnfs"),
//                new File("sbsRuns_1.json"),
//                new File("oracleRuns_1.json"),
//                3600,
//                1
//        );

//        testSolver(List.of(
//                new RunTask("SharpSAT-TD-unweighted/default", new File("/home/ubuntu/mcc2022/cnfs/MCC2022_track1-complete/mc2022_track1_129.dimacs"), 3600),
//                new RunTask("SharpSAT-TD-unweighted/default", new File("/home/ubuntu/mcc2022/cnfs/MCC2022_track1-complete/mc2022_track1_150.dimacs"), 3600),
//                new RunTask("SharpSAT-TD-unweighted/default", new File("/home/ubuntu/mcc2022/cnfs/MCC2022_track1-complete/mc2022_track1_151.dimacs"), 3600),
//                new RunTask("SharpSAT-TD-unweighted/default", new File("/home/ubuntu/mcc2022/cnfs/MCC2022_track1-complete/mc2022_track1_133.dimacs"), 3600),
//                new RunTask("SharpSAT-TD-unweighted/default", new File("/home/ubuntu/mcc2022/cnfs/MCC2022_track1-complete/mc2022_track1_131.dimacs"), 3600),
//                new RunTask("Narsimha-track1v-51fd045537919d/track1_conf1.sh", new File("/home/ubuntu/mcc2022/cnfs/MCC2022_track1-complete/mc2022_track1_168.dimacs"), 3600),
//                new RunTask("dpmcpre/1pre1mp1", new File("/home/ubuntu/mcc2022/cnfs/MCC2022_track1-complete/mc2022_track1_166.dimacs"), 3600)
//        ), new File("test_Run_tt.json"), 1);

//        runNormalMultiple(
//                generateFileNames(
//                        new File("/home/ubuntu/as4moco/AutoFolio/examples/MCC2022_T1_randomSplits/split.csv"),
//                        1,
//                        new File("/home/ubuntu/MCC2022_T1_cnfs")
//                        ),
//                new File("/home/ubuntu/as4moco/AutoFolio/mcc2022_T1_F1_2000I.pkl"),
//                new File("MCC22_T1_F1_2000I.json")
//        );

        //tester();

        System.exit(0);

    }

    private static void tester() throws IOException, InterruptedException, ExecutionException {
        File cnf = new File("/home/ubuntu/mcc2022/cnfs/MCC2022_track1-complete/mc2022_track1_019.dimacs");
        int timeout = 140;
        testSolver(List.of(
                new RunTask("ExactMC/ExactMC_Arjun.sh", cnf, timeout),
                new RunTask("ExactMC/ExactMC_BE.sh", cnf, timeout),
                new RunTask("Narsimha-track1v-51fd045537919d/track1_conf1.sh", cnf, timeout),
                new RunTask("Narsimha-track1v-51fd045537919d/track1_conf2.sh", cnf, timeout),
                new RunTask("Narsimha-track1v-7112ef8eb466e9475/track1_conf1.sh", cnf, timeout),
                new RunTask("Narsimha-track1v-7112ef8eb466e9475/track1_conf2.sh", cnf, timeout),
                new RunTask("SharpSAT-TD-unweighted/default", cnf, timeout),
                new RunTask("TwG/1.sh", cnf, timeout),
                new RunTask("TwG/2.sh", cnf, timeout),
                new RunTask("c2d/default", cnf, timeout),
                new RunTask("d4/default.sh", cnf, timeout),
                new RunTask("dpmcpre/1pre1mp0", cnf, timeout),
                new RunTask("dpmcpre/1pre1mp1", cnf, timeout),
                new RunTask("gpmc/track1", cnf, timeout),
                new RunTask("gpmc_r2/track1", cnf, timeout),
                new RunTask("mtmc/default", cnf, timeout)

        ), new File("test_Run_019.json"), 1);
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
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule()).registerModule(new Jdk8Module());
            out.setCodec(mapper);
            out.useDefaultPrettyPrinter();
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
     * @param split split to evaluate
     * @param cnfFolder folder with cnfs
     * @param sbsOut file to save sbs runs to
     * @param oracleOut file to save oracle runs to
     * @param timeout timeout for solver
     * @throws IOException
     */
    private static void runSBSOracleAnalysis(File csvFile, int split, File cnfFolder,File sbsOut, File oracleOut, int timeout, int nThreads) throws IOException, InterruptedException, ExecutionException {
        try (CSVParser parser = CSVParser.parse(csvFile, Charset.defaultCharset(), CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader().setAllowMissingColumnNames(true).setSkipHeaderRecord(true).build())) {
            List<RunTask> sbsList = new ArrayList<>();
            List<RunTask> oracleList = new ArrayList<>();
            parser.stream().filter(e -> Integer.parseInt(e.get("Split")) == split).forEach(e -> {
                File instance = new File( cnfFolder.getAbsolutePath()+ File.separator + "mc2022_track1_%03d.dimacs".formatted(Integer.parseInt(e.get("InstanceNo"))));
                sbsList.add(new RunTask(e.get("SBS"), instance, timeout));
                oracleList.add(new RunTask(e.get("Oracle"), instance, timeout));
            });
            testSolver(sbsList, sbsOut, nThreads);
            testSolver(oracleList, oracleOut, nThreads);
        }
    }

    /**
     * Fully evaluates a scenario
     * @param csvFile csv file with split
     * @param split which split to evaluate
     * @param cnfFolder folder with cnfs
     * @param out file to save runs to
     * @param timeout timeout for solver
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private static void evaluateScenario(File csvFile, int split, File cnfFolder, File out, int timeout) throws IOException, ExecutionException, InterruptedException {
        try (CSVParser parser = CSVParser.parse(csvFile, Charset.defaultCharset(), CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader().setAllowMissingColumnNames(true).setSkipHeaderRecord(true).build())) {
            List<RunTask> tasks = new ArrayList<>();
            parser.stream().filter(e -> Integer.parseInt(e.get("Split")) == split).forEach(e -> {
                File instance = new File( cnfFolder.getAbsolutePath()+ File.separator + "mc2022_track1_%03d.dimacs".formatted(Integer.parseInt(e.get("InstanceNo"))));
                SolverMap.getNames().forEach(s -> tasks.add(new RunTask(s, instance, timeout)));
            });
            testSolver(tasks, out, 1);
        }
    }


    /**
     * Runs daughter solvers
     * @param tasks list of tasks for daughter solvers
     * @param saveFile file to save to
     * @return list of runs
     * @throws IOException
     */
    private static List<SolvingRun> testSolver(List<RunTask> tasks, File saveFile, int nThreads) throws IOException, InterruptedException, ExecutionException {
        List<SolvingRun> solvingRuns = new ArrayList<>(tasks.size());

        JsonFactory factory = new JsonFactory();


        try (JsonGenerator out = factory.createGenerator(saveFile, JsonEncoding.UTF8);
             ExecutorService executorService = Executors.newFixedThreadPool(nThreads)) {
            CompletionService<SolvingRun> completionService = new ExecutorCompletionService<>(executorService);
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule()).registerModule(new Jdk8Module());
            out.setCodec(mapper);
            out.useDefaultPrettyPrinter();
            out.writeStartArray();

            tasks.forEach(task -> completionService.submit(() -> {
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
                return solvingRun;
            }));

            for (int i = 0; i < tasks.size(); i++) {
                solvingRuns.add(completionService.take().get());
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