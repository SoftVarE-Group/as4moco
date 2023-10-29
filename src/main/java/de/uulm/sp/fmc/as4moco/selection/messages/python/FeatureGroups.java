package de.uulm.sp.fmc.as4moco.selection.messages.python;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.uulm.sp.fmc.as4moco.selection.messages.Message;
import de.uulm.sp.fmc.as4moco.selection.messages.MessageEnum;
import org.collection.fm.util.AnalysisStepsEnum;

import java.util.List;
import java.util.Objects;

public class FeatureGroups extends Message {

    private int cutoff;
    private List<String> fgroups;

    public FeatureGroups() {
        super(MessageEnum.FEATURE_GROUPS);
    }

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

    @JsonIgnore
    public List<AnalysisStepsEnum> getEnums(){
        return fgroups.stream().map(e -> e.substring(7)).map(e -> e.replace("-", "")).map(AnalysisStepsEnum::getIgnoreCase).filter(Objects::isNull).toList();
    }

    public void setFgroups(List<String> fgroups) {
        this.fgroups = fgroups;
    }
}
