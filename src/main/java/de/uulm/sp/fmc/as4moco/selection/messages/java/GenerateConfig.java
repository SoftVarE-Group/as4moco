package de.uulm.sp.fmc.as4moco.selection.messages.java;

import de.uulm.sp.fmc.as4moco.selection.messages.Message;
import de.uulm.sp.fmc.as4moco.selection.messages.MessageEnum;

import java.util.Optional;

public class GenerateConfig extends Message {

    private String scenarioPath;
    private boolean maximize;
    private Optional<Integer> timeLimit;
    private Optional<Integer> runLimit;

    public GenerateConfig() {
        super(MessageEnum.GENERATE_CONFIG);
    }

    public GenerateConfig(String scenarioPath, boolean maximize, Optional<Integer> timeLimit, Optional<Integer> runLimit) {
        super(MessageEnum.GENERATE_CONFIG);
        this.scenarioPath = scenarioPath;
        this.maximize = maximize;
        this.timeLimit = timeLimit;
        this.runLimit = runLimit;
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

    public Optional<Integer> getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Optional<Integer> timeLimit) {
        this.timeLimit = timeLimit;
    }

    public Optional<Integer> getRunLimit() {
        return runLimit;
    }

    public void setRunLimit(Optional<Integer> runLimit) {
        this.runLimit = runLimit;
    }
}
