package de.uulm.sp.fmc.as4moco;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.uulm.sp.fmc.as4moco.selection.messages.SolverBudget;
import de.uulm.sp.fmc.as4moco.solver.SolverHandler;
import de.uulm.sp.fmc.as4moco.solver.SolverInterface;
import de.uulm.sp.fmc.as4moco.solver.SolverResponse;
import de.uulm.sp.fmc.as4moco.solver.SolverStatusEnum;
import org.apache.commons.cli.*;
import org.collection.fm.FeatureStepAnalysis;
import org.collection.fm.handler.AnalysisStepHandler;
import org.collection.fm.handler.FeatureStep;
import org.collection.fm.util.AnalysisStepsEnum;
import org.collection.fm.util.FMUtils;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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

    private static void runNormal(CommandLine commandLine) throws IOException, ExecutionException, InterruptedException {
        Instant before = Instant.now();

        Instant after;
        try (WorkflowManager workflowManager = new WorkflowManager(new File(commandLine.getOptionValue("modelFile")))) {
            System.out.println("as4moco solution: " + workflowManager.runSolving(new File(commandLine.getOptionValue("cnfFile"))));
            after = Instant.now();
        }

        System.out.println("Duration: " + Duration.between(before, after).toMillis()/1000f + "s");
    }

    private static List<SolvingRun> runMultiple(List<File> cnfs, File modelFile, File saveFile) throws IOException, ExecutionException, InterruptedException {
        List<SolvingRun> solvingRuns = new ArrayList<>(cnfs.size());

        JsonFactory factory = new JsonFactory();


        try (WorkflowManager workflowManager = new WorkflowManager(modelFile);
             JsonGenerator out = factory.createGenerator(saveFile, JsonEncoding.UTF8)) {
            out.writeStartArray();
            for (File cnf : cnfs) {
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

    private static List<SolvingRun> testSolver(List<RunTask> tasks, File saveFile) throws IOException{
        List<SolvingRun> solvingRuns = new ArrayList<>(tasks.size());

        JsonFactory factory = new JsonFactory();


        try (JsonGenerator out = factory.createGenerator(saveFile, JsonEncoding.UTF8)) {
            out.writeStartArray();
            for (RunTask task : tasks) {
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