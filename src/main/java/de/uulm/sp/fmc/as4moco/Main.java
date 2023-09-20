package de.uulm.sp.fmc.as4moco;

import com.oracle.graal.python.shell.GraalPythonMain;
import org.collection.fm.FeatureStepAnalysis;
import org.collection.fm.handler.AnalysisStepHandler;
import org.collection.fm.handler.FeatureStep;
import org.collection.fm.util.AnalysisStepsEnum;
import org.collection.fm.util.FMUtils;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;

import java.io.File;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        String code = """
                from venv import create
                import os
                                
                
                create("/home/ubuntu/as4moco/as4moco/src/main/resources/vfs/venv", with_pip=True)
                """;

        try (Context context = Context.newBuilder("python").allowAllAccess(true).build()){
            context.eval("python", code);
        }
    }
}