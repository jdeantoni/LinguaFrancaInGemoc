package fr.univcotedazur.kairos.linguafranca.utils.dotfromtrace.lfranca;



import java.util.List;

import fr.univcotedazur.kairos.linguafranca.utils.dotfromtrace.Main;

public class TracesByType {
    private final List<TraceRecord> schedules;
    private final List<TraceRecord> reactionsTimeAdv;
    private final List<TraceObject> triggers;

    public TracesByType(List<TraceRecord> schedules, List<TraceRecord> reactionsTimeAdv, List<TraceObject> triggers){
        this.schedules = Main.orderTraceLog(schedules);
        this.reactionsTimeAdv = Main.orderTraceLog(reactionsTimeAdv);
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
