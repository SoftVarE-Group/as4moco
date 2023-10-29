package de.uulm.sp.fmc.as4moco.selection.messages.java;

import de.uulm.sp.fmc.as4moco.selection.messages.Message;
import de.uulm.sp.fmc.as4moco.selection.messages.MessageEnum;

public class GetPrediction extends Message {

    private String featureVector;

    public GetPrediction( ) {
        super(MessageEnum.GET_PREDICTION);
    }

    public GetPrediction( String featureVector) {
        super(MessageEnum.GET_PREDICTION);
        this.featureVector = featureVector;
    }

    public String getFeatureVector() {
        return featureVector;
    }

    public void setFeatureVector(String featureVector) {
        this.featureVector = featureVector;
    }
}
