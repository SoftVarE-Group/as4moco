package de.uulm.sp.fmc.as4moco;

import org.apache.commons.cli.*;
import org.collection.fm.FeatureStepAnalysis;
import org.collection.fm.handler.AnalysisStepHandler;
import org.collection.fm.handler.FeatureStep;
import org.collection.fm.util.AnalysisStepsEnum;
import org.collection.fm.util.FMUtils;


import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        CommandLine commandLine = parseCommandLine(args, generateOptions());

        Instant before = Instant.now();

        WorkflowManager workflowManager = new WorkflowManager(commandLine.getOptionValue("modelFile"));

        System.out.println("as4moco solution: "+workflowManager.runSolving(commandLine.getOptionValue("cnfFile")));
        Instant after = Instant.now();
        workflowManager.close();

        System.out.println("Duration: " + Duration.between(before, after).toMillis()/1000f + "s");
        System.exit(0);

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

}