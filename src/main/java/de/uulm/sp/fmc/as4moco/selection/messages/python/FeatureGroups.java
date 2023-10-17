package de.uulm.sp.fmc.as4moco.selection.messages.python;

import de.uulm.sp.fmc.as4moco.selection.messages.Message;
import de.uulm.sp.fmc.as4moco.selection.messages.MessageEnum;

import java.util.List;

public class FeatureGroups extends Message {

    private int cutoff;
    private List<String> fgroups;

    public FeatureGroups(int cutoff, List<String> fgroups) {
        super(MessageEnum.FEATURE_GROUPS);

        this.cutoff = cutoff;
        this.fgroups = fgroups;
    }

    public int getCutoff() {
        return cutoff;
    }

    public void setCutoff(int cutoff) {
        this.cutoff = cutoff;
    }

    public List<String> getFgroups() {
        return fgroups;
    }

    public void setFgroups(List<String> fgroups) {
        this.fgroups = fgroups;
    }
}
