package de.uulm.sp.fmc.as4moco.selection.messages.java;

import de.uulm.sp.fmc.as4moco.selection.messages.Message;
import de.uulm.sp.fmc.as4moco.selection.messages.MessageEnum;

public class GetPreSchedule extends Message {

    public GetPreSchedule() {
        super(MessageEnum.GET_PRE_SCHEDULE);
    }
}
