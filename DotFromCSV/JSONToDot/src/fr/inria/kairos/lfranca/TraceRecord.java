package fr.inria.kairos.lfranca;

import java.util.Arrays;
import java.util.List;

public class TraceRecord {
    private final String reactor;
    //private final String[] triggeredBy;
    private final List<String> triggeredBy;
    //private final String[] effects;
    private final List<String> effects;
    private final String event;
    private final String trigger;
    private final int reactionNumber;
    private final long elapsedLogicalTime;
    private final long extraDelay;
    private final long elapsedPhysicalTime;
    private final long microStep;
    private final int worker;
    private final TraceEvent eventType;
    private long executionScheduledTime;

    private static final int NONE_PORT_NUMBER = -1;
    private static final String EMPTY_LIST = "<>";
    private static final String DOUBLE_POINT_SEPARATOR = ":";

    public TraceRecord(String[] traceEvent) {
        this.event = traceEvent[0];
        this.reactor = traceEvent[1];
        this.reactionNumber = isNumeric(traceEvent[2]) ? Integer.parseInt(traceEvent[2])
            : NONE_PORT_NUMBER;
        this.worker = Integer.parseInt(traceEvent[3]);
        this.elapsedLogicalTime = Long.parseLong(traceEvent[4].stripLeading());
        this.microStep = Long.parseLong(traceEvent[5].stripLeading());
        this.elapsedPhysicalTime = Long.parseLong(traceEvent[6].stripLeading());
        this.trigger = traceEvent[7].stripLeading();
        this.extraDelay = Long.parseLong(traceEvent[4].stripLeading());
        //this.triggeredBy = getTokensFromTraceLists(traceEvent[9]);
        this.triggeredBy = null;
        //this.effects = getTokensFromTraceLists(traceEvent[10]);
        this.effects = null;
        this.eventType = null;
        this.executionScheduledTime = 0;
    }

    /*public TraceRecord(TraceEvent eventType, String reactor, int reactionNumber, int worker, long elapsedLogicalTime,
                       long elapsedPhysicalTime, int microStep, String trigger, long extraDelay, String[] triggeredBy,
                       String[] effects){
        this.eventType = eventType;
        this.event = null;
        this.reactor = reactor;
        this.reactionNumber = reactionNumber;
        this.worker = worker;
        this.elapsedLogicalTime = elapsedLogicalTime;
        this.microStep = microStep;
        this.elapsedPhysicalTime = elapsedPhysicalTime;
        this.trigger = trigger;
        this.extraDelay = extraDelay;
        this.triggeredBy = triggeredBy;
        this.effects = effects;
    }*/

    public void setExecutionScheduledTime(long executionScheduledTime) {
        this.executionScheduledTime = executionScheduledTime;
    }

    public TraceRecord(TraceEvent eventType, String reactor, int reactionNumber, int worker, long elapsedLogicalTime,
                       long elapsedPhysicalTime, int microStep, String trigger, long extraDelay, List<String> triggeredBy,
                       List<String> effects){
        this.eventType = eventType;
        this.event = null;
        this.reactor = reactor;
        this.reactionNumber = reactionNumber;
        this.worker = worker;
        this.elapsedLogicalTime = elapsedLogicalTime;
        this.microStep = microStep;
        this.elapsedPhysicalTime = elapsedPhysicalTime;
        this.trigger = trigger;
        this.extraDelay = extraDelay;
        this.triggeredBy = triggeredBy;
        this.effects = effects;
        this.executionScheduledTime = 0;
    }

    /*
    @Override
    public String toString() {
        return "TraceEvent{" +
                "reactor='" + reactor + '\'' +
                //", triggeredBy=" + Arrays.toString(triggeredBy) +
                ", triggeredBy=" + triggeredBy.stream().; +
                //", effects=" + Arrays.toString(effects) +
                ", effects=" + Arrays.toString(effects) +
                ", event='" + event + '\'' +
                ", trigger='" + trigger + '\'' +
                ", reactionNumber=" + reactionNumber +
                ", elapsedLogicalTime=" + elapsedLogicalTime +
                ", extraDelay=" + extraDelay +
                ", elapsedPhysicalTime=" + elapsedPhysicalTime +
                ", microStep=" + microStep +
                ", worker=" + worker +
                '}';
    }*/

    public int getWorker() {
        return worker;
    }


    /**
     *
     * @param traceField The String
     * @return an array of ReactorName.Trigger from strings where tokens are separated by ":"
     * Used for extracting token in the triggeredBy, Effects lists
     */
    private String[] getTokensFromTraceLists(String traceField){
        String[] tokens;
        if(!traceField.equals(EMPTY_LIST)){
            if(!traceField.contains(DOUBLE_POINT_SEPARATOR))
                tokens = new String[]{traceField};
            else
                tokens = (traceField.substring(1, traceField.length() - 2)).split(DOUBLE_POINT_SEPARATOR);
        }else
            tokens = null;
        return tokens;

    }


    public String getReactor() {
        return reactor;
    }

    public List<String> getTriggeredBy() {
        return triggeredBy;
    }

    public List<String> getEffects() {
        return effects;
    }

    public String getEvent() {
        return event;
    }

    public TraceEvent getEventType() {
        return eventType;
    }

    public String getTrigger() {
        return trigger;
    }

    public int getReactionNumber() {
        return reactionNumber;
    }

    public long getElapsedLogicalTime() {
        return elapsedLogicalTime;
    }

    public long getElapsedPhysicalTime() {
        return elapsedPhysicalTime;
    }

    public long getMicroStep() {
        return microStep;
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null)
            return false;
        try {
            Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }


}
