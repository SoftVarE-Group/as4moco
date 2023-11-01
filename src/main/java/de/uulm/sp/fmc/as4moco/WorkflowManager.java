package de.uulm.sp.fmc.as4moco;

import de.uulm.sp.fmc.as4moco.extraction.FeatureExtractor;
import de.uulm.sp.fmc.as4moco.selection.AlgorithmSelector;
import de.uulm.sp.fmc.as4moco.selection.messages.SolverBudget;
import de.uulm.sp.fmc.as4moco.selection.messages.java.GetFeatureGroups;
import de.uulm.sp.fmc.as4moco.selection.messages.java.GetPreSchedule;
import de.uulm.sp.fmc.as4moco.selection.messages.java.GetPrediction;
import de.uulm.sp.fmc.as4moco.selection.messages.java.LoadModel;
import de.uulm.sp.fmc.as4moco.selection.messages.python.FeatureGroups;
import de.uulm.sp.fmc.as4moco.selection.messages.python.ModelLoaded;
import de.uulm.sp.fmc.as4moco.selection.messages.python.PreSchedule;
import de.uulm.sp.fmc.as4moco.selection.messages.python.Prediction;
import de.uulm.sp.fmc.as4moco.solver.SolverHandler;
import de.uulm.sp.fmc.as4moco.solver.SolverResponse;
import de.uulm.sp.fmc.as4moco.solver.SolverStatusEnum;
import org.collection.fm.util.AnalysisStepsEnum;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;


public class WorkflowManager implements AutoCloseable {

    private final FeatureExtractor featureExtractor;
    private final AlgorithmSelector algorithmSelector;

    private final SolverBudget[] preschedule;

    public WorkflowManager(File modelFile) throws ExecutionException, InterruptedException {
        algorithmSelector = new AlgorithmSelector();

        if ( ! (algorithmSelector.askAutofolio(new LoadModel(modelFile.getAbsolutePath())).get() instanceof ModelLoaded)) throw new RuntimeException("Problem with autofolio loading") ;
        System.out.println("Autofolio loaded successfully!");

        FeatureGroups featureGroups = (FeatureGroups) algorithmSelector.askAutofolio(new GetFeatureGroups()).get();
        EnumMap<AnalysisStepsEnum, Integer> featureMap = new EnumMap<>(AnalysisStepsEnum.class);
        featureGroups.getEnums().forEach(e -> featureMap.put(e, featureGroups.getCutoff()));
        featureExtractor = new FeatureExtractor(featureMap);
        System.out.println("Initialized Feature Extraction");

        preschedule = ((PreSchedule) algorithmSelector.askAutofolio(new GetPreSchedule()).get()).getPreSchedule();
        System.out.println("Read Pre-Schedule");
    }

    public SolverResponse runSolving(File cnf){
        try(ExecutorService executorService = Executors.newCachedThreadPool()) {
            CompletionService<List<SolverResponse>> completionService = new ExecutorCompletionService<>(executorService);
            completionService.submit(() -> SolverHandler.runSolvers(preschedule, cnf));
            completionService.submit(() -> handleSchedule(cnf));

            SolverResponse bestResponse = getBestResponse(completionService.take().get());
            System.out.println("First Pipeline finished!");
            if (bestResponse.status().equals(SolverStatusEnum.OK)) {
                executorService.shutdownNow();
                System.out.println("Status ok, kill other pipeline");
            } else {
                System.out.println("Status not ok, wait for other pipeline");
                List<SolverResponse> responses = completionService.take().get();
                System.out.println("Second pipeline finished!");
                bestResponse = getBestResponse(responses);
            }
            return bestResponse;
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private static SolverResponse getBestResponse(List<SolverResponse> responses) {
        return responses.stream().reduce(new SolverResponse(null, SolverStatusEnum.ERROR, Optional.empty()), (acc, next) -> acc.status().equals(SolverStatusEnum.OK) ? acc : next);
    }

    private List<SolverResponse> handleSchedule(File cnfFile) throws ExecutionException, InterruptedException {
        String features = featureExtractor.extractFeatures(cnfFile);
        System.out.printf("Extracted feature Vector: %s%n", features);
        if (Thread.currentThread().isInterrupted()) return new ArrayList<>();
        Prediction prediction = (Prediction) algorithmSelector.askAutofolio(new GetPrediction(features)).get();
        if (Thread.currentThread().isInterrupted()) return new ArrayList<>();
        System.out.println("Got Prediction: "+prediction);
        return SolverHandler.runSolvers(prediction.getPrediction(), cnfFile);
    }

    @Override
    public void close() throws IOException {
        System.out.println("Closing Autofolio!");
        algorithmSelector.closeAutofolio();
    }

}
