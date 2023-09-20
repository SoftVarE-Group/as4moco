package de.uulm.sp.fmc.as4moco.extraction;

import org.collection.fm.handler.AnalysisStepHandler;
import org.collection.fm.handler.FeatureStep;
import org.collection.fm.util.AnalysisStepsEnum;
import org.collection.fm.util.FMUtils;

import java.io.File;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

public class FeatureExtractor {

    private final AnalysisStepHandler analysisStepHandler;

    public FeatureExtractor() {
        FMUtils.installLibraries();

        analysisStepHandler = new AnalysisStepHandler();
        EnumMap<AnalysisStepsEnum, Integer> enumMap = new EnumMap<>(AnalysisStepsEnum.class);
        Arrays.stream(AnalysisStepsEnum.values()).forEach(e -> enumMap.put(e, 60));
        analysisStepHandler.initializeHandler(enumMap);
    }

    public List<String> extractFeatures(File file){
        return analysisStepHandler.getSingleAnalysis(file).stream().map(FeatureStep::values).flatMap(List::stream).toList();
    }


}
