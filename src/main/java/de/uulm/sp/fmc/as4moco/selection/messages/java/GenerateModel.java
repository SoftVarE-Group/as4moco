package de.uulm.sp.fmc.as4moco.selection.messages.java;

import de.uulm.sp.fmc.as4moco.selection.messages.Message;
import de.uulm.sp.fmc.as4moco.selection.messages.MessageEnum;

import java.util.Map;
import java.util.Optional;

public class GenerateModel extends Message {

    private String scenarioPath;
    private boolean maximize;
    private String config;
    private Optional<String> modelPath;

    public GenerateModel( ) {
        super(MessageEnum.GENERATE_MODEL);
    }

    public GenerateModel( String scenarioPath, boolean maximize, String config, Optional<String> modelPath) {
        super(MessageEnum.GENERATE_MODEL);
        this.scenarioPath = scenarioPath;
        this.maximize = maximize;
        this.config = config;
        this.modelPath = modelPath;
    }

    public String getScenarioPath() {
        return scenarioPath;
    }

    public void setScenarioPath(String scenarioPath) {
        this.scenarioPath = scenarioPath;
    }

    public boolean isMaximize() {
        return maximize;
    }

    public void setMaximize(boolean maximize) {
        this.maximize = maximize;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public Optional<String> getModelPath() {
        return modelPath;
    }

    public void setModelPath(Optional<String> modelPath) {
        this.modelPath = modelPath;
    }
}
