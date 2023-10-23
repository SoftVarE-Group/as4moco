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
import java.util.*;
import java.util.concurrent.*;


public class WorkflowManager {

    private final FeatureExtractor featureExtractor;
    private final AlgorithmSelector algorithmSelector;

    private final SolverBudget[] preschedule;

    public WorkflowManager(String modelPath) {
        algorithmSelector = new AlgorithmSelector();

        if ( ! (algorithmSelector.askAutofolio(new LoadModel(modelPath)) instanceof ModelLoaded)) throw new RuntimeException("Problem with autofolio loading") ;
        FeatureGroups featureGroups = (FeatureGroups) algorithmSelector.askAutofolio(new GetFeatureGroups());
        EnumMap<AnalysisStepsEnum, Integer> featureMap = new EnumMap<>(AnalysisStepsEnum.class);
        featureGroups.getEnums().forEach(e -> featureMap.put(e, featureGroups.getCutoff()));

        featureExtractor = new FeatureExtractor(featureMap);

        preschedule = ((PreSchedule) algorithmSelector.askAutofolio(new GetPreSchedule())).getPreSchedule();
    }

    public SolverResponse runSolving(String cnfPath){
        File cnf = new File(cnfPath);
        try(ExecutorService executorService = Executors.newCachedThreadPool()) {
            CompletionService<List<SolverResponse>> completionService = new ExecutorCompletionService<>(executorService);
            completionService.submit(() -> SolverHandler.runSolvers(preschedule, cnf));
            completionService.submit(() -> handleSchedule(cnf));

            SolverResponse bestResponse = getBestResponse(completionService.take().get());
            if (bestResponse.status().equals(SolverStatusEnum.OK)) {
                executorService.shutdownNow();
            } else {
                List<SolverResponse> responses = completionService.take().get();
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

    private List<SolverResponse> handleSchedule(File cnfFile){
        String features = featureExtractor.extractFeatures(cnfFile);
        Prediction prediction = (Prediction) algorithmSelector.askAutofolio(new GetPrediction(features));
        return SolverHandler.runSolvers(prediction.getPrediction(), cnfFile);
    }


}
