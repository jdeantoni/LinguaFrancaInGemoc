package fr.inria.kairos.lfranca;

import java.util.Arrays;

public class TraceRecord {
    private final String reactor;
    private final String[] triggeredBy;
    private final String[] effects;
    private final String event;
    private final String trigger;
    private final int reactionNumber;
    private final long elapsedLogicalTime;
    private final long extraDelay;
    private final long elapsedPhysicalTime;
    private final long microStep;
    private final int worker;

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
        this.triggeredBy = getTokensFromTraceLists(traceEvent[9]);
        this.effects = getTokensFromTraceLists(traceEvent[10]);
    }

    @Override
    public String toString() {
        return "TraceEvent{" +
                "reactor='" + reactor + '\'' +
                ", triggeredBy=" + Arrays.toString(triggeredBy) +
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
    }

    public int getWorker() {
        return worker;
    }


    /**
     *
     * @param traceField
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

    public String[] getTriggeredBy() {
        return triggeredBy;
    }

    public String[] getEffects() {
        return effects;
    }

    public String getEvent() {
        return event;
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
