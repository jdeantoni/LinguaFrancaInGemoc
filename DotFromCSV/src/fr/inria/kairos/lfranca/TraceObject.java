package fr.inria.kairos.lfranca;

public class TraceObject {
    private TraceObjectType type;
    private long triggerAddress;
    private long reactorAddress;
    private String description;

    public TraceObject(TraceObjectType type, long triggerAddress, long reactorAddress, String description) {
        this.type = type;
        this.triggerAddress = triggerAddress;
        this.reactorAddress = reactorAddress;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public long getTriggerAddress() {
        return triggerAddress;
    }

    public long getReactorAddress() {
        return reactorAddress;
    }
}
