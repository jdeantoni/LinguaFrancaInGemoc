package fr.inria.kairos.lfranca;


import java.util.List;

import static fr.inria.kairos.Main.orderTraceLog;

public class TracesByType {
    private final List<TraceRecord> schedules;
    private final List<TraceRecord> reactionsTimeAdv;
    private final List<TraceObject> triggers;

    public TracesByType(List<TraceRecord> schedules, List<TraceRecord> reactionsTimeAdv, List<TraceObject> triggers){
        this.schedules = orderTraceLog(schedules);
        this.reactionsTimeAdv = orderTraceLog(reactionsTimeAdv);
        this.triggers = triggers;
    }

    public List<TraceObject> getTriggers() {
        return triggers;
    }

    public List<TraceRecord> getSchedules() {
        return schedules;
    }

    public List<TraceRecord> getReactionsTimeAdv() {
        return reactionsTimeAdv;
    }
}
