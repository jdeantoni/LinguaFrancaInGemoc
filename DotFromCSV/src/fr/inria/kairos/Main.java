
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

package fr.inria.kairos;
import fr.inria.kairos.lfranca.TraceRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static final String COMMA_SPACE_DELIMITER = ", ";
    private static final String COMMA_DELIMITER = ",";
    private static final String DOT_DELIMITER = ".";
    private static final String TIME_ADVANCE_LABEL_PREFIX = "Model.TimeAdvancement";

    private static final String REACTION_STARTS = "Reaction starts";
    private static final String REACTION_ENDS = "Reaction ends";
    private static final String REACTION = "Reaction";
    private static final String WORKER_ENDS = "Worker advancing ends";
    private static final String USAGE = "Usage: java -jar DotFromCSV.jar pathToCSVFile outputDOTFilePath";



    /**
     * Orders the CSV file by (elapsed physical time, micro-step)
     * @param filePath the trace CSV input file
     * @return an ordered list of trace records
     */
    private static List<TraceRecord> orderTraceLog(String filePath) {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8));
            in.readLine();
            String currentLine;
            List<TraceRecord> events = new LinkedList<>();
            while ((currentLine = in.readLine()) != null)
                events.add(new TraceRecord(currentLine.split(COMMA_SPACE_DELIMITER)));
            return events.stream().sorted(Comparator.comparing(TraceRecord::getElapsedPhysicalTime)
                    .thenComparing(TraceRecord::getMicroStep))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.out.println("Error parsing CSV file");
            e.printStackTrace();
            return null;
        }
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
                if(currentTransition.getPreviousTransition() != null)
                    sb.append(nodeId - 1).append(" -> ").append(nodeId).append(" [label=\"")
                            .append(generateLabel(currentTransition.getTriggers())).append("\"]\n");
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
    private static String generateLabel(List<String> reactionsLabels){
        StringBuilder sb = new StringBuilder();
        for (String reaction : reactionsLabels)
            sb.append(reaction).append(COMMA_DELIMITER);
        return sb.substring(0, sb.length() - 1);
    }

    /**
     *
     * @param traceRecords list of ordered trace records(elapsed physical time, micro-step).
     * @return a graph's root node. The graph is a representation of the reactions trace execution order and causality.
     */
    private static Transition parseCSVToTransitions(List<TraceRecord> traceRecords) {
        long nextLogicalTimeAdvance = 0, currentMicroStep = 0;
        Transition rootTransition = new Transition(0,0, null, null,
                null, false);   //graph root node
        Transition currentTransition = rootTransition;
        //Set<String> pendingEffects = new HashSet<>();
        List<String> pendingLabels = new LinkedList<>();
        List<String> effects = new LinkedList<>();
        for (TraceRecord record: traceRecords) {
            if(!record.getEvent().startsWith(REACTION) && !record.getEvent().startsWith(WORKER_ENDS))
                continue;
            if(record.getElapsedLogicalTime() > nextLogicalTimeAdvance || record.getMicroStep() > currentMicroStep) {
                if(pendingLabels.size() > 0) {
                    currentTransition = addReactionEffectsTransitions(currentTransition, effects, pendingLabels,
                            nextLogicalTimeAdvance, currentMicroStep, false);
                    pendingLabels = new LinkedList<>();
                    if(effects.size() > 0)
                        effects = new LinkedList<>();
                }
                nextLogicalTimeAdvance = record.getElapsedLogicalTime();
                currentMicroStep = record.getMicroStep();
                //pendingEffects.clear();
                currentTransition = addTransition(currentTransition, pendingLabels, nextLogicalTimeAdvance,
                        currentMicroStep, true);
                pendingLabels = new LinkedList<>();
            }
            if(record.getEvent().equals(REACTION_STARTS)){
                if(record.getElapsedLogicalTime() == nextLogicalTimeAdvance && currentMicroStep ==
                        record.getMicroStep()){
                    if (triggersPrecededByEffects(record.getTriggeredBy(), effects)) {  // causality from previous reaction on same Logical time

                        currentTransition = addReactionEffectsTransitions(currentTransition, effects, pendingLabels,
                                nextLogicalTimeAdvance, currentMicroStep, false);
                        pendingLabels = new LinkedList<>();
                        effects = new LinkedList<>();
                    }
                    pendingLabels.add(generateLinkLabel(record));
                }
            }else if(record.getEvent().equals(REACTION_ENDS))
                if(record.getEffects() != null)
                    addEffects(record.getEffects(), effects);
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
                                            long nextLogicalTimeAdvance, long currentMicroStep, boolean resultsFromWorkerAdvancing) {
        Transition nextTransition = new Transition(nextLogicalTimeAdvance, currentMicroStep,
                pendingReactions, null, currentTransition,resultsFromWorkerAdvancing );
        currentTransition.setNextTransition(nextTransition);
        nextTransition.setPreviousTransition(currentTransition);
        if(resultsFromWorkerAdvancing) {
            nextTransition.setTriggers(new LinkedList<>());
            nextTransition.getTriggers().add(TIME_ADVANCE_LABEL_PREFIX);
        }
        else
            nextTransition.setTriggers(pendingReactions);
        return nextTransition;
    }

    /**
     *
     * @param reactionTriggers array containing a reaction set of triggers
     * @param pendingEffects set of reactions pending effects
     * @return a boolean indicating if at least one of a reactionºs triggers is contained on the set of pending effects.
     * A true return value indicates a causality between a reaction and a previous executed reaction.
     */
    private static boolean triggersPrecededByEffects(String[] reactionTriggers, List<String> pendingEffects) {
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
    private static void addEffects(String[] effects, List<String> pendingEffects) {
        if(effects != null)
            pendingEffects.addAll(Arrays.asList(effects));
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
                                                        long currentMicroStep, boolean resultsFromWorkerAdvancing){
        Transition latestTransition = addTransition(currentTransition, pendingReactions, nextLogicalTimeAdvance,
                currentMicroStep, resultsFromWorkerAdvancing);
        currentTransition.setNextTransition(latestTransition);
        latestTransition.setPreviousTransition(currentTransition);
        if(resultsFromWorkerAdvancing) {
            latestTransition.setTriggers(new LinkedList<>());
            latestTransition.getTriggers().add(TIME_ADVANCE_LABEL_PREFIX);
        }else
            latestTransition.setTriggers(pendingReactions);

        if(effects.size() > 0){
            currentTransition = latestTransition;
            latestTransition = addTransition(currentTransition, effects, nextLogicalTimeAdvance,
                    currentMicroStep, resultsFromWorkerAdvancing);
            currentTransition.setNextTransition(latestTransition);
            latestTransition.setPreviousTransition(currentTransition);
        }
        return latestTransition;
    }

    public static void main(String[] args) {
        if(args.length != 2)
            System.out.println(USAGE);
        else{
            int dotPos = args[0].indexOf(DOT_DELIMITER);
            String modelName = (dotPos != -1)? args[0].substring(0, dotPos) : args[1];
            Transition root = parseCSVToTransitions(Objects.requireNonNull(orderTraceLog(args[0])));;
            generateDotFile(root, args[1], modelName);
        }
    }

}