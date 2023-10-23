package de.uulm.sp.fmc.as4moco.extraction;

import org.collection.fm.handler.AnalysisStepHandler;
import org.collection.fm.handler.FeatureStep;
import org.collection.fm.util.AnalysisStepsEnum;
import org.collection.fm.util.FMUtils;

import java.io.File;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

public class FeatureExtractor {

    private final AnalysisStepHandler analysisStepHandler;

    public FeatureExtractor(EnumMap<AnalysisStepsEnum, Integer> analysisMap) {
        FMUtils.installLibraries();

        analysisStepHandler = new AnalysisStepHandler();
        analysisStepHandler.initializeHandler(analysisMap);
    }

    public String extractFeatures(File file){
        return analysisStepHandler.getSingleAnalysis(file).stream().map(FeatureStep::values).flatMap(List::stream).collect(Collectors.joining(","));
    }


}
