package de.uulm.sp.fmc.as4moco.selection.messages.python;

import de.uulm.sp.fmc.as4moco.selection.messages.Message;
import de.uulm.sp.fmc.as4moco.selection.messages.MessageEnum;

public class Config extends Message {

    private String config;
    private String scenarioPath;

    public Config(String config, String scenarioPath) {
        super(MessageEnum.CONFIG);
        this.config = config;
        this.scenarioPath = scenarioPath;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getScenarioPath() {
        return scenarioPath;
    }

    public void setScenarioPath(String scenarioPath) {
        this.scenarioPath = scenarioPath;
    }
}
