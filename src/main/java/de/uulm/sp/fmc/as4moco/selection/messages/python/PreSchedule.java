package de.uulm.sp.fmc.as4moco.selection.messages.python;

import de.uulm.sp.fmc.as4moco.selection.messages.Message;
import de.uulm.sp.fmc.as4moco.selection.messages.MessageEnum;
import de.uulm.sp.fmc.as4moco.selection.messages.SolverBudget;

public class PreSchedule extends Message {

    private SolverBudget[] preSchedule;

    public PreSchedule() {
        super(MessageEnum.PRE_SCHEDULE);
    }

    public PreSchedule( SolverBudget[] preSchedule) {
        super(MessageEnum.PRE_SCHEDULE);
        this.preSchedule = preSchedule;
    }

    public SolverBudget[] getPreSchedule() {
        return preSchedule;
    }

    public void setPreSchedule(SolverBudget[] preSchedule) {
        this.preSchedule = preSchedule;
    }
}
