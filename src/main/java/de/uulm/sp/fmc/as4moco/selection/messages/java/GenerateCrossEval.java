package de.uulm.sp.fmc.as4moco.selection.messages.java;

import de.uulm.sp.fmc.as4moco.selection.messages.Message;
import de.uulm.sp.fmc.as4moco.selection.messages.MessageEnum;

public class GenerateCrossEval extends Message {

    private String config;

    public GenerateCrossEval(String config) {
        super(MessageEnum.GENERATE_CROSS_EVALUATION);
        this.config = config;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }
}
