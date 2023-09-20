package de.uulm.sp.fmc.as4moco;

import org.collection.fm.FeatureStepAnalysis;
import org.collection.fm.handler.AnalysisStepHandler;
import org.collection.fm.handler.FeatureStep;
import org.collection.fm.util.AnalysisStepsEnum;
import org.collection.fm.util.FMUtils;


import java.io.File;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        FMUtils.installLibraries();
        AnalysisStepHandler analysisStepHandler = new AnalysisStepHandler();
        EnumMap<AnalysisStepsEnum, Integer> enumMap = new EnumMap<>(AnalysisStepsEnum.class);

        Arrays.stream(AnalysisStepsEnum.values()).forEach(e -> enumMap.put(e, 60));

        analysisStepHandler.initializeHandler(enumMap);

        List<FeatureStep> list = analysisStepHandler.getSingleAnalysis(new File("src/main/resources/test.dimacs"));
        list.forEach(System.out::println);
        
    }
}