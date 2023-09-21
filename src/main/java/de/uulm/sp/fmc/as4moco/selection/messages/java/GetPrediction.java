package de.uulm.sp.fmc.as4moco.selection.messages.java;

import de.uulm.sp.fmc.as4moco.selection.messages.Message;
import de.uulm.sp.fmc.as4moco.selection.messages.MessageEnum;

public class GetPrediction extends Message {

    private Double[] featureVector;

    public GetPrediction( Double[] featureVector) {
        super(MessageEnum.GET_PREDICTION);
        this.featureVector = featureVector;
    }

    public Double[] getFeatureVector() {
        return featureVector;
    }

    public void setFeatureVector(Double[] featureVector) {
        this.featureVector = featureVector;
    }
}
