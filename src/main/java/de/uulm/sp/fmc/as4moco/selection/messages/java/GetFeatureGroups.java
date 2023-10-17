package de.uulm.sp.fmc.as4moco.selection.messages.java;

import de.uulm.sp.fmc.as4moco.selection.messages.Message;
import de.uulm.sp.fmc.as4moco.selection.messages.MessageEnum;

public class GetFeatureGroups extends Message {
    public GetFeatureGroups() {
        super(MessageEnum.GET_FEATURE_GROUPS);
    }
}
