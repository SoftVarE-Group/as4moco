package de.uulm.sp.fmc.as4moco.selection.messages.python;

import de.uulm.sp.fmc.as4moco.selection.messages.Message;
import de.uulm.sp.fmc.as4moco.selection.messages.MessageEnum;
import de.uulm.sp.fmc.as4moco.selection.messages.SolverBudget;

public class Prediction extends Message {

    private SolverBudget[] preSchedule;
    private SolverBudget[] prediction;

    public Prediction( SolverBudget[] preSchedule, SolverBudget[] prediction) {
        super(MessageEnum.PREDICTION);
        this.preSchedule = preSchedule;
        this.prediction = prediction;
    }

    public SolverBudget[] getPreSchedule() {
        return preSchedule;
    }

    public void setPreSchedule(SolverBudget[] preSchedule) {
        this.preSchedule = preSchedule;
    }

    public SolverBudget[] getPrediction() {
        return prediction;
    }

    public void setPrediction(SolverBudget[] prediction) {
        this.prediction = prediction;
    }
}
