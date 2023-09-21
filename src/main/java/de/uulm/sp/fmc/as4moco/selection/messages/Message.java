package de.uulm.sp.fmc.as4moco.selection.messages;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.uulm.sp.fmc.as4moco.selection.messages.java.*;
import de.uulm.sp.fmc.as4moco.selection.messages.python.*;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = LoadModel.class, name ="LOAD_MODEL"),
        @JsonSubTypes.Type(value = GetPreSchedule.class, name ="GET_PRE_SCHEDULE"),
        @JsonSubTypes.Type(value = GetPrediction.class, name ="GET_PREDICTION"),
        @JsonSubTypes.Type(value = GenerateConfig.class, name ="GENERATE_CONFIG"),
        @JsonSubTypes.Type(value = GenerateModel.class, name ="GENERATE_MODEL"),
        @JsonSubTypes.Type(value = GenerateCrossEval.class, name ="GENERATE_CROSS_EVALUATION"),
        @JsonSubTypes.Type(value = Error.class, name ="ERROR"),
        @JsonSubTypes.Type(value = CrossEvaluation.class, name ="CROSS_EVALUATION"),
        @JsonSubTypes.Type(value = Model.class, name ="MODEL"),
        @JsonSubTypes.Type(value = Config.class, name ="CONFIG"),
        @JsonSubTypes.Type(value = Prediction.class, name ="PREDICTION"),
        @JsonSubTypes.Type(value = PreSchedule.class, name ="PRE_SCHEDULE"),
        @JsonSubTypes.Type(value = ModelLoaded.class, name ="MODEL_LOADED"),
})

public abstract class Message {

    private final MessageEnum type;

    public Message(MessageEnum type) {
        this.type = type;
    }

    public MessageEnum getType() {
        return type;
    }
}
