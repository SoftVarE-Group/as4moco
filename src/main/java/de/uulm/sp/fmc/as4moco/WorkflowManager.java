package de.uulm.sp.fmc.as4moco;

import de.uulm.sp.fmc.as4moco.data.*;
import de.uulm.sp.fmc.as4moco.extraction.FeatureExtractor;
import de.uulm.sp.fmc.as4moco.extraction.FeatureVector;
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
import de.uulm.sp.fmc.as4moco.solver.SolverStatusEnum;
import de.uulm.sp.fmc.as4moco.solver.SolverType;
import org.collection.fm.util.AnalysisStepsEnum;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
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

    public FullRun runSolving(File cnf){
        Instant before = Instant.now();
        try(ExecutorService executorService = Executors.newCachedThreadPool()) {
            CompletionService<PipelineCompletion> completionService = new ExecutorCompletionService<>(executorService);

            List<PipelineCompletion> completions = new ArrayList<>(2);

            completionService.submit(() -> SolverHandler.runSolvers(preschedule, cnf));
            completionService.submit(() -> handleSchedule(cnf));

            PipelineCompletion pipelineCompletion = completionService.take().get();
            completions.add(pipelineCompletion);
            SolverRunInstance bestResponse = getBestResponse(pipelineCompletion.solverResponses());
            System.out.println("First Pipeline finished!");
            if (bestResponse.status().equals(SolverStatusEnum.OK)) {
                executorService.shutdownNow();
                System.out.println("Status ok, kill other pipeline");
            } else {
                System.out.println("Status not ok, wait for other pipeline");
                PipelineCompletion secondCompletion = completionService.take().get();
                completions.add(secondCompletion);
                System.out.println("Second pipeline finished!");
                bestResponse = getBestResponse(secondCompletion.solverResponses());

            }
            Instant after = Instant.now();


            return new FullRun(cnf, before, after, Duration.between(before, after).toMillis() / 1000d, completions, bestResponse);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private static SolverRunInstance getBestResponse(List<SolverRunInstance> responses) {
        return responses.stream().reduce(new SolverRunInstance(null, SolverStatusEnum.ERROR, Optional.empty(), 0, SolverType.ERR), (acc, next) -> acc.status().equals(SolverStatusEnum.OK) ? acc : next);
    }

    private HandledInstance handleSchedule(File cnfFile) throws ExecutionException, InterruptedException {
        FeatureVector features = featureExtractor.extractFeatures(cnfFile);
        System.out.printf("Extracted feature Vector: %s%n", features.vector());
        if (Thread.currentThread().isInterrupted()) return new HandledInstance(new ArrayList<>(), null, 0);
        Instant before = Instant.now();
        Prediction prediction = (Prediction) algorithmSelector.askAutofolio(new GetPrediction(features.vector())).get();
        if (Thread.currentThread().isInterrupted()) return new HandledInstance(new ArrayList<>(), null, 0);
        Duration duration = Duration.between(before, Instant.now());
        System.out.println("Got Prediction: "+prediction);
        HandledSet handledSet = SolverHandler.runSolvers(prediction.getPrediction(), cnfFile);
        return new HandledInstance(handledSet.solverResponses(), features, duration.toMillis() / 1000d);
    }

    @Override
    public void close() throws IOException {
        System.out.println("Closing Autofolio!");
        algorithmSelector.closeAutofolio();
    }

}
