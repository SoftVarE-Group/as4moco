package de.uulm.sp.fmc.as4moco.selection.messages;

public class Error extends Message{

    private String reason;

    public Error(String reason) {
        super(MessageEnum.ERROR);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
