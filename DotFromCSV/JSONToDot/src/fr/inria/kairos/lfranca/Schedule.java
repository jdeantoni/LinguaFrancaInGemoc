package fr.inria.kairos.lfranca;

public class Schedule {
    private final String triggerDescription;
    private long elapsedLogicalTime;


    public Schedule(long elapsedLogicalTime, String triggerDescription) {
        this.triggerDescription = triggerDescription;
        this.elapsedLogicalTime = elapsedLogicalTime;
    }

    public void setElapsedLogicalTime(long elapsedLogicalTime) {
        this.elapsedLogicalTime = elapsedLogicalTime;
    }

    public String getTriggerDescription() {
        return triggerDescription;
    }

    public long getElapsedLogicalTime() {
        return elapsedLogicalTime;
    }

}
