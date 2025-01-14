package de.uulm.sp.fmc.as4moco.extraction;

import org.collection.fm.handler.AnalysisStepHandler;
import org.collection.fm.handler.FeatureStep;
import org.collection.fm.util.AnalysisStepsEnum;
import org.collection.fm.util.FMUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FeatureExtractor {

    private final AnalysisStepHandler analysisStepHandler;

    public FeatureExtractor(EnumMap<AnalysisStepsEnum, Integer> analysisMap) {
        FMUtils.installLibraries();

        analysisStepHandler = new AnalysisStepHandler(Path.of("libs/FeatureExtraction"));
        analysisStepHandler.initializeHandler(analysisMap);
    }

    public FeatureVector extractFeatures(File file) throws InterruptedException{
        List<FeatureStep> analysisSteps = analysisStepHandler.getSingleAnalysis(file).stream().filter(Objects::nonNull).toList();
        return new FeatureVector(
                analysisSteps.stream().map(FeatureStep::values).flatMap(List::stream).collect(Collectors.joining(",")),
                analysisSteps.stream().collect(Collectors.toMap(FeatureStep::name, FeatureStep::usedRuntime)),
                analysisSteps.stream().collect(Collectors.toMap(FeatureStep::name, FeatureStep::featureStatus))
                );


    }

}
