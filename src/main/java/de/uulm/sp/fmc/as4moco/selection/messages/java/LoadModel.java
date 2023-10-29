package de.uulm.sp.fmc.as4moco.selection.messages.java;

import de.uulm.sp.fmc.as4moco.selection.messages.Message;
import de.uulm.sp.fmc.as4moco.selection.messages.MessageEnum;

public class LoadModel extends Message {

    private String modelPath;

    public LoadModel() {
        super(MessageEnum.LOAD_MODEL);
    }

    public LoadModel( String modelPath) {
        super(MessageEnum.LOAD_MODEL);
        this.modelPath = modelPath;
    }

    public String getModelPath() {
        return modelPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }
}
