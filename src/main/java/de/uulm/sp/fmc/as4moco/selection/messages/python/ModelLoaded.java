package de.uulm.sp.fmc.as4moco.selection.messages.python;

import de.uulm.sp.fmc.as4moco.selection.messages.Message;
import de.uulm.sp.fmc.as4moco.selection.messages.MessageEnum;

public class ModelLoaded extends Message {

    private String modelPath;

    public ModelLoaded() {
        super(MessageEnum.MODEL_LOADED);
    }

    public ModelLoaded(String modelPath) {
        super(MessageEnum.MODEL_LOADED);
        this.modelPath = modelPath;
    }

    public String getModelPath() {
        return modelPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }
}
