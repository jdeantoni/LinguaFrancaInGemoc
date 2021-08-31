
/*******************************************************************************
 * Copyright (c) 2017 Universite Cote d'Azur and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Universite Cote d'Azur - initial API and implementation
 * @authors:
 *     joao.cambeiro@univ-cotedazur.fr
 *******************************************************************************/

package fr.univcotedazur.kairos.linguafranca.utils.dotfromtrace;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import fr.univcotedazur.kairos.linguafranca.utils.dotfromtrace.lfranca.Schedule;
import fr.univcotedazur.kairos.linguafranca.utils.dotfromtrace.lfranca.TraceEvent;
import fr.univcotedazur.kairos.linguafranca.utils.dotfromtrace.lfranca.TraceObject;
import fr.univcotedazur.kairos.linguafranca.utils.dotfromtrace.lfranca.TraceObjectType;
import fr.univcotedazur.kairos.linguafranca.utils.dotfromtrace.lfranca.TraceRecord;
import fr.univcotedazur.kairos.linguafranca.utils.dotfromtrace.lfranca.TracesByType;

public class Main {

    private static final String COMMA_DELIMITER = ",";
    private static final String DOT_DELIMITER = ".";
    private static final String TIME_ADVANCE_LABEL_PREFIX = "Model.TimeJump";
    private static final String USAGE = "Usage: java -jar dotFromTrace.jar pathToTraceFile outputDOTFilePath";
    private static final int TRACE_STRUCT_SIZE  = 224;
    private static final int TRACE_EFFECTS_MAX_SIZE  = 10;


    /**
     * Orders the Trace file by (elapsed physical time, micro-step)
     * @return an ordered list of trace records
     */
    public static List<TraceRecord> orderTraceLog(List<TraceRecord> records) {
        return records.stream().sorted(Comparator.comparing(TraceRecord::getElapsedPhysicalTime)
                .thenComparing(TraceRecord::getMicroStep))
                .collect(Collectors.toList());
    }


    /**
     * Generates directed acyclic graph representation in the DOT language of the program trace reactions execution
     * @param root the graph's root node
     * @param outputFilePath the output dot file path
     * @param modelName the model name extracted from the CSV file name. It is used as the generated graph name.
     */
    public static void generateDotFile(Transition root, String outputFilePath, String modelName){
        FileWriter file;
        try {
            file = new FileWriter(outputFilePath);
            BufferedWriter writer = new BufferedWriter(file);
            StringBuilder sb = new StringBuilder();
            long nodeId = 0;
            Transition currentTransition = root;
            writer.write("digraph " + modelName + " {\n");
            while(currentTransition != null){
                writer.write((nodeId  + " [label=\"" +"("+ currentTransition.getLogicalTime() + "," +
                        currentTransition.getMicroStep() + ")\"]"));
                writer.newLine();
                if(currentTransition.getPreviousTransition() != null){
                    if(currentTransition.getPendingEffects() != null)
                        sb.append(nodeId - 1).append(" -> ").append(nodeId).append(" [label=\"")
                            .append(generateReactionLabel(currentTransition.getPendingEffects(), "present"))
                                .append("\"]\n");
                    else if(currentTransition.getSchedules() != null)
                        sb.append(nodeId - 1).append(" -> ").append(nodeId).append(" [label=\"")
                                .append(generateScheduleLabel(currentTransition.getSchedules())).append("\"]\n");
                    else if(currentTransition.getTriggers() != null){
                        if(!currentTransition.isTimeAdvancement())
                            sb.append(nodeId - 1).append(" -> ").append(nodeId).append(" [label=\"")
                                    .append(generateReactionLabel(currentTransition.getTriggers(), "startExecution"))
                                    .append("\"]\n");
                        else
                            sb.append(nodeId - 1).append(" -> ").append(nodeId).append(" [label=\"")
                                    .append(generateReactionLabel(currentTransition.getTriggers(), null))
                                    .append("\"]\n");
                    }
                }
                currentTransition = currentTransition.getNextTransition();
                nodeId++;
            }
            sb.append("}");
            writer.write(sb.toString());
            writer.close();
        } catch (IOException e) {
            System.out.println("Error writing output dot file");
            e.printStackTrace();
        }
    }

    /**
     *
     * @param reactionsLabels a list of strings in the form of {Reaction[Name].Reaction[Number]}
     * @return returns a string that will be used as label of on the graph edges. ex: {reactor1.reaction1,reactor1.reaction2}
     */
    private static String generateReactionLabel(List<String> reactionsLabels, String typeTermination){
        StringBuilder sb = new StringBuilder();
        for (String reaction : reactionsLabels) {
            sb.append(reaction);
            if(typeTermination != null)
                sb.append(".").append(typeTermination);
            sb.append(COMMA_DELIMITER);
        }
        return sb.substring(0, sb.length() - 1);
    }

    /**
     *
     * @param schedules the list of schedules of a graph node representing a program execution step
     *                  (logical time, micro step)
     * @return a string in the format "schedule({trigger1}.schedules,{logical_time_of_execution}),
     * schedule({trigger2}.schedules,{logical_time_of_execution}))
     */
    private static String generateScheduleLabel(List<Schedule> schedules){
        StringBuilder sb = new StringBuilder();
        for (Schedule currentSchedule: schedules) {
            sb.append("schedule(");
            sb.append(currentSchedule.getTriggerDescription()).append(".starts,");
            sb.append(currentSchedule.getElapsedLogicalTime());
            sb.append("),");
        }
        return sb.substring(0, sb.length() - 1);
    }

    /**
     * Builds a graph representation of the execution of a Lingua Franca program considering the schedule calls,
     * the reaction executed and the produced effects
     * @param traceRecords A list of trace records obtained during the execution of a Lingua Franca program
     * @param triggers The list of triggers present in the execution trace file.
     * @return The root node of a graph that represents the execution steps of a Lingua Franca program
     */
    private static Transition parseRecordsToTransitions(List<TraceRecord> traceRecords, List<TraceObject> triggers) {
        long nextLogicalTimeAdvance = 0, currentMicroStep = 0;
        Map<String, Queue<Schedule>> schedules = new HashMap<>(triggers.size());
        for (TraceObject trigger: triggers)
            schedules.put(trigger.getDescription(), new LinkedList<>());
        Transition rootTransition = new Transition(0,0, null, null,
                null, false, null);   //graph root node
        Transition currentTransition = rootTransition;
        List<String> pendingLabels = new LinkedList<>();
        List<String> effects = new LinkedList<>();
        List<Schedule> pendingScheduled = new LinkedList<>();
        Set<String> scheduledTrigger = new HashSet<>(triggers.size());
        boolean reactionsExecuted = false;
        for (TraceRecord record: traceRecords) {
            if(record.getElapsedLogicalTime() > nextLogicalTimeAdvance || record.getMicroStep() > currentMicroStep) {
                if(pendingLabels.size() > 0) {
                    currentTransition = addReactionEffectsTransitions(currentTransition, effects, pendingLabels,
                            nextLogicalTimeAdvance, currentMicroStep, false, pendingScheduled);
                    pendingLabels = new LinkedList<>();
                    if(effects.size() > 0)
                        effects = new LinkedList<>();
                    if(pendingScheduled.size() > 0)
                        pendingScheduled = new LinkedList<>();
                }
                nextLogicalTimeAdvance = record.getElapsedLogicalTime();
                currentMicroStep = record.getMicroStep();
                currentTransition = addTransition(currentTransition, pendingLabels, nextLogicalTimeAdvance,
                        currentMicroStep, true, null, null);
                pendingLabels = new LinkedList<>();
            }
            if(record.getEventType() == TraceEvent.REACTION_STARTS){
                updatePendingScheduled(schedules, record);
                reactionsExecuted = true;
                if(record.getElapsedLogicalTime() == nextLogicalTimeAdvance && currentMicroStep ==
                        record.getMicroStep()){
                    if(triggersPrecededByEffects(record.getTriggeredBy(), effects)) {  // causality from previous reaction on same Logical time
                        currentTransition = addReactionEffectsTransitions(currentTransition, effects, pendingLabels,
                                nextLogicalTimeAdvance, currentMicroStep, false, pendingScheduled);
                        pendingLabels = new LinkedList<>();
                        effects = new LinkedList<>();
                        pendingScheduled = new LinkedList<>();
                    }
                    pendingLabels.add(generateLinkLabel(record));
                }
            }else if(record.getEventType() == TraceEvent.REACTION_ENDS) {
                if(record.getEffects() != null)
                    addEffects(record.getEffects(), effects);
            }else if(record.getEventType() == TraceEvent.SCHEDULE_CALLED){
                if(reactionsExecuted || !scheduledTrigger.add(record.getTrigger())){
                    Schedule schedule = new Schedule(record.getElapsedLogicalTime(), record.getTrigger());
                    pendingScheduled.add(schedule);
                    schedules.get(record.getTrigger()).add(schedule);
                }
            }
        }
        return rootTransition;
    }

    /**
     *
     * @param currentTransition the graph's node representing the highest superdense time
     * @param pendingReactions a reactions list executed on the time advancement step that is going yo be added to the
     *                         graph.
     * @param nextLogicalTimeAdvance the new time advancement step's logical time elapsed
     * @param currentMicroStep the new time advancement step's micro-step elapsed
     * @param resultsFromWorkerAdvancing indicates if the time advancement results from a worker advancing time
     * @return the latest added transition to the graph representing the current time advancement step
     */
    private static Transition addTransition(Transition currentTransition, List<String> pendingReactions,
                                            long nextLogicalTimeAdvance, long currentMicroStep,
                                            boolean resultsFromWorkerAdvancing, List<Schedule> schedules,
                                            List<String> effects) {
        Transition nextTransition = new Transition(nextLogicalTimeAdvance, currentMicroStep,
                pendingReactions, null, currentTransition,resultsFromWorkerAdvancing, schedules);
        currentTransition.setNextTransition(nextTransition);
        nextTransition.setPreviousTransition(currentTransition);
        if(resultsFromWorkerAdvancing) {
            nextTransition.setTriggers(new LinkedList<>());
            nextTransition.getTriggers().add(TIME_ADVANCE_LABEL_PREFIX);
        }else {
            if(effects != null)
                nextTransition.setPendingEffects(effects);
            else if(pendingReactions != null)
                nextTransition.setTriggers(pendingReactions);
            else if(schedules != null)
                nextTransition.setSchedules(schedules);
        }
        return nextTransition;
    }

    /**
     * Assigns the elapsed logical time of a reaction time to the relative logical time assigned to a Schedule call.
     * The logical time of the reaction execution is assigned to the schedule call with the lowest logical time.
     * @param scheduledTriggers A queue containing Schedules that still don't have an assigned time for the execution of its trigger
     * @param reaction A reaction that has been triggered by a previous schedule call
     */
    private static void updatePendingScheduled(Map<String, Queue<Schedule>> scheduledTriggers, TraceRecord reaction){
        if(scheduledTriggers.size() == 0 || reaction.getTriggeredBy() == null)
            return;
        Schedule firstPendingSchedule;
        for (String triggeredBy: reaction.getTriggeredBy()) {
            firstPendingSchedule = scheduledTriggers.get(triggeredBy).poll();
            if(firstPendingSchedule != null)
                firstPendingSchedule.setElapsedLogicalTime(reaction.getElapsedLogicalTime() -
                        firstPendingSchedule.getElapsedLogicalTime());
        }
    }

    /**
     *
     * @param reactionTriggers array containing a reaction set of triggers
     * @param pendingEffects set of reactions pending effects
     * @return a boolean indicating if at least one of a reactionºs triggers is contained on the set of pending effects.
     * A true return value indicates a causality between a reaction and a previous executed reaction.
     */
    private static boolean triggersPrecededByEffects(List<String> reactionTriggers, List<String> pendingEffects) {
        if(reactionTriggers != null)
            for (String trigger: reactionTriggers) {
                if(pendingEffects.contains(trigger))
                    return true;
            }
        return false;
    }

    /**
     *
     * @param effects adds a reaction's effects to the set of effects that can become triggers of subsequent reactions
     * @param pendingEffects set of reactions pending effects
     */
    private static void addEffects(List<String> effects, List<String> pendingEffects) {
        if(effects != null)
            pendingEffects.addAll(effects);
    }

    /**
     *
     * @param event a trace record
     * @return a string in the format {Reactor_name}.{Reaction_number}. This string is part of a graph edge label
     */
    private static String generateLinkLabel(TraceRecord event){
        return event.getReactor() + DOT_DELIMITER + event.getReactionNumber();
    }

    /**
     * Adds the transitions to the LF program's graph. One transition node is added if the trace record is a worker time
     * advancement. If not, there is a causality between reactions, and two nodes are added to the graph.
     * One transition node with an incident edge's label indicating the executed reactions and a second node with an
     * incident edge's label indicating the effects produced by the first node's reactions.
     * @param currentTransition the graph's node representing the highest superdense time
     * @param pendingReactions a reactions list executed on the time advancement step that is going yo be added to the
     *                         graph.
     * @param nextLogicalTimeAdvance the new time advancement step's logical time elapsed
     * @param currentMicroStep the new time advancement step's micro-step elapsed
     * @param resultsFromWorkerAdvancing indicates if the time advancement results from a worker advancing time
     * @return the latest node added to the graph
     */
    public static Transition addReactionEffectsTransitions(Transition currentTransition, List<String> effects,
                                                        List<String> pendingReactions, long nextLogicalTimeAdvance,
                                                        long currentMicroStep, boolean resultsFromWorkerAdvancing,
                                                           List<Schedule> scheduledReactions){

        Transition latestTransition = null;
        latestTransition = addTransition(currentTransition, pendingReactions, nextLogicalTimeAdvance,
                currentMicroStep, resultsFromWorkerAdvancing, null, null);
        if(resultsFromWorkerAdvancing) {
            latestTransition.setTriggers(new LinkedList<>());
            latestTransition.getTriggers().add(TIME_ADVANCE_LABEL_PREFIX);
        }else
            latestTransition.setTriggers(pendingReactions);
        
        currentTransition = latestTransition;
        if(scheduledReactions.size() > 0 ) {
            latestTransition = addTransition(currentTransition, null, nextLogicalTimeAdvance,
                    currentMicroStep, resultsFromWorkerAdvancing, scheduledReactions, null);
        }
        

        if(effects.size() > 0) {
            currentTransition = latestTransition;
            latestTransition = addTransition(currentTransition, null, nextLogicalTimeAdvance,
                    currentMicroStep, resultsFromWorkerAdvancing, null, effects);
        }
        return latestTransition;
    }

    /**
     *This method is used to parse the Lingua-France binary trace file
     * @param filePath path of the binary trace file to be parsed
     * @return a list containing the parsed trace records
     */
    public static TracesByType parseBinaryFile(String filePath){
        FileChannel channel = null;
        List<TraceRecord> records = new LinkedList<>();
        List<TraceRecord> schedules = new LinkedList<>();
        List<TraceObject> traceTriggers = new LinkedList<>();
        try {
            FileInputStream stream =  new FileInputStream(filePath);
            channel = stream.getChannel();
            ByteBuffer buffLong = ByteBuffer.allocate(8);
            ByteBuffer buffInt = ByteBuffer.allocate(4);
            ByteBuffer buffChar = ByteBuffer.allocate(1);
            ByteBuffer recordBuffer = ByteBuffer.allocate(TRACE_STRUCT_SIZE);
            buffLong.order(ByteOrder.LITTLE_ENDIAN);
            buffInt.order(ByteOrder.LITTLE_ENDIAN);
            buffChar.order(ByteOrder.LITTLE_ENDIAN);
            recordBuffer.order(ByteOrder.LITTLE_ENDIAN);
            channel.read(buffLong);
            final long startTime = buffLong.flip().getLong();
            buffLong.clear();
            channel.read(buffInt);
            final int objectsRead = buffInt.flip().getInt();
            List<TraceObject> traceReactors = new LinkedList<>();
            List<TraceObject> traceUsers = new LinkedList<>();
            buffInt.clear();
            StringBuilder builder = new StringBuilder();
            long reactorAddress, triggerAddress;
            char descriptionSymbol;
            for(int i = 0; i < objectsRead; i++){
                channel.read(buffLong);
                reactorAddress = buffLong.flip().getLong();
                buffLong.clear();
                channel.read(buffLong);
                triggerAddress = buffLong.flip().getLong();
                channel.read(buffInt);
                do {
                    channel.read(buffChar);
                    descriptionSymbol = (char) (buffChar.flip().get() & 0xFF);
                    builder.append(descriptionSymbol);
                    buffChar.clear();
                }while(descriptionSymbol != 0);
                final int traceInputType = buffInt.flip().getInt();
                switch (traceInputType){
                    case 0:
                        traceReactors.add(new TraceObject(TraceObjectType.TRACE_REACTOR, triggerAddress, reactorAddress,
                                builder.substring(0, builder.length() - 1)));
                        break;
                    case 1:
                        traceTriggers.add(new TraceObject(TraceObjectType.TRACE_TRIGGER, triggerAddress, reactorAddress,
                                builder.substring(0, builder.length() - 1)));
                        break;
                    case 2:
                        traceUsers.add(new TraceObject(TraceObjectType.TRACE_USER, triggerAddress, reactorAddress,
                                builder.substring(0, builder.length() - 1)));
                        break;
                }
                buffInt.clear();
                buffLong.clear();
                buffChar.clear();
                builder = new StringBuilder();
            }
            final Map<Long, TraceObject> reactors = traceReactors.stream()
                    .collect(Collectors.toMap(TraceObject::getReactorAddress, Function.identity()));
            final Map<Long, TraceObject> triggers = traceTriggers.stream()
                    .collect(Collectors.toMap(TraceObject::getTriggerAddress, Function.identity()));
            final Map<Long, TraceObject> users = traceUsers.stream()
                    .collect(Collectors.toMap(TraceObject::getReactorAddress, Function.identity()));
            //parse trace_records
            int recordsAvailable;
            while(channel.read(buffInt) != -1){
                recordsAvailable = buffInt.flip().getInt();
                for(int i = 0; i < recordsAvailable; i++){
                    channel.read(recordBuffer);
                    TraceRecord parsedRecord = getRecordFromTrace(recordBuffer, startTime, reactors, triggers, users);
                    if(parsedRecord != null)
                        records.add(parsedRecord);
                    recordBuffer.clear();
                }
                buffInt.clear();
            }
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(channel != null && channel.isOpen()) {
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new TracesByType(schedules, records, traceTriggers);
    }

    /**Parses a 224 bytes long byte buffer containing a trace record in binary into a TraceRecord.
     *
     * @param buffer contains the trace record bytes
     * @param startTime the start time of the execution trace
     * @param reactors the reactors objects of the LF program
     * @param triggers the triggers objects of the LF program
     * @param users the users objects of the LF program
     * @return a TraceRecord representing the parsed trace record
     */
    private static TraceRecord getRecordFromTrace(ByteBuffer buffer, long startTime, Map<Long, TraceObject> reactors,
                                                  Map<Long, TraceObject> triggers, Map<Long, TraceObject> users){
        String reactorName = null;
        String triggerName = null;
        TraceRecord parsedRecord = null;
        final TraceEvent eventType = TraceEvent.values()[buffer.flip().getInt()];
        if(eventType == TraceEvent.REACTION_STARTS || eventType == TraceEvent.REACTION_ENDS
                || eventType == TraceEvent.WORKER_ADVANCING_TIME_STARTS
                    || eventType == TraceEvent.WORKER_ADVANCING_TIME_ENDS || eventType == TraceEvent.SCHEDULE_CALLED) {
            buffer.position(buffer.position() + 4); //padding
            final long reactorAddress = buffer.getLong();
            TraceObject reactor = reactors.get(reactorAddress);

            if(reactor != null)
                reactorName = reactor.getDescription();

            final int reaction = buffer.getInt();
            final int worker = buffer.getInt();
            final long logicalTime = (buffer.getLong() - startTime);
            //final long logicalTime = (buffer.getLong() - startTime) / 1000;
            final int microStep = buffer.getInt();
            buffer.position(buffer.position() + 4); //padding
            //final long physicalTime = (buffer.getLong() - startTime) / 1000;
            final long physicalTime = (buffer.getLong() - startTime);
            final long triggerAddress = buffer.getLong();
            if(triggerAddress != 0) {
                TraceObject trigger = triggers.get(triggerAddress);
                if(trigger != null)
                    triggerName = trigger.getDescription();
            }
            final long extraDelay = buffer.getLong();
            List<String> recordTriggers = null;
            List<String> recordEffects = null;
            if(eventType == TraceEvent.REACTION_STARTS) {
                recordTriggers = getTriggersEffects(buffer, triggers);
            }else if(eventType == TraceEvent.REACTION_ENDS){
                buffer.position(buffer.position() + TRACE_EFFECTS_MAX_SIZE * 8);
                recordEffects = getTriggersEffects(buffer, triggers);
            }
            parsedRecord = new TraceRecord(eventType, reactorName, reaction, worker, logicalTime, physicalTime,
                    microStep, triggerName, extraDelay, recordTriggers, recordEffects);
        }
        return parsedRecord;
    }


    /**
     * Maps the event effectsTriggers or effects addresses into a list of {reactor.reactionNumber}
     * @param buffer containing the list of 10 (fixed) effects / effectsTriggers
     * @param effectsTriggers a map containing a set of of reactors or triggers. a long representation of the
     *                        reactors / triggers  addresses is used as map keys.
     * @return list of {reactor.reactionNumber} that match the addresses of the reactors / triggers
     */
    private static List<String> getTriggersEffects(ByteBuffer buffer, Map<Long, TraceObject> effectsTriggers) {
        List<String> recordEffectsTriggers = null;
        for (int i = 0; i < TRACE_EFFECTS_MAX_SIZE; i++) {
            TraceObject effect = effectsTriggers.get(buffer.getLong());
            if(effect != null && effect.getDescription() != null) {
                if(recordEffectsTriggers == null)
                    recordEffectsTriggers = new LinkedList<>();
                recordEffectsTriggers.add(effect.getDescription());
            }
        }
        return recordEffectsTriggers;
    }

    public static void main(String[] args) {
        if (args.length != 2)
            System.out.println(USAGE);
        else {
            int dotPos = args[0].indexOf(DOT_DELIMITER);
            String modelName = (dotPos != -1) ? args[0].substring(0, dotPos) : args[1];
            TracesByType records = parseBinaryFile(args[0]);
            Transition root = parseRecordsToTransitions(records.getReactionsTimeAdv(), records.getTriggers());
            generateDotFile(root, args[1], modelName);
        }
    }


}