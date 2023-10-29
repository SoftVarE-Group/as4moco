package de.uulm.sp.fmc.as4moco.selection.messages.python;

import de.uulm.sp.fmc.as4moco.selection.messages.Message;
import de.uulm.sp.fmc.as4moco.selection.messages.MessageEnum;

import java.util.Map;

public class CrossEvaluation extends Message {

    private String config;
    private Map<String, Double> stats;

    public CrossEvaluation() {
        super(MessageEnum.CROSS_EVALUATION);
    }

    public CrossEvaluation(String config, Map<String, Double> stats) {
        super(MessageEnum.CROSS_EVALUATION);
        this.config = config;
        this.stats = stats;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public Map<String, Double> getStats() {
        return stats;
    }

    public void setStats(Map<String, Double> stats) {
        this.stats = stats;
    }
}
