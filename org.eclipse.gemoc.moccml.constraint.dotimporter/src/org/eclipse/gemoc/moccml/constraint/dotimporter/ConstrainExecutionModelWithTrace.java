/*******************************************************************************
 * Copyright (c) 2017 I3S laboratory, INRIA and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     I3S laboratory and INRIA Kairos - initial API and implementation
 *******************************************************************************/
package org.eclipse.gemoc.moccml.constraint.dotimporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.lflang.lf.Action;
import org.lflang.lf.Instantiation;
import org.lflang.lf.Model;
import org.lflang.lf.Port;
import org.lflang.lf.Reaction;
import org.lflang.lf.Reactor;
import org.lflang.lf.TypedVariable;

import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;

import toools.io.file.RegularFile;
import fr.inria.aoste.timesquare.ccslkernel.model.TimeModel.Clock;
import fr.inria.aoste.timesquare.ccslkernel.model.TimeModel.EventKind;
import fr.inria.aoste.timesquare.ccslkernel.model.TimeModel.ImportStatement;
import fr.inria.aoste.timesquare.ccslkernel.model.TimeModel.BasicType.Element;
import fr.inria.aoste.timesquare.ccslkernel.model.TimeModel.CCSLModel.ClockConstraintSystem;
import fr.inria.aoste.timesquare.ccslkernel.model.utils.ResourceLoader;

public class ConstrainExecutionModelWithTrace extends AbstractHandler {
	

	private static final boolean LFTrace = true;//crappy way to deal with different traces, need to use an extension point
	private IFile ccslFile;
	private String ccslFilePath;
	private IFile dotFile;
	private String dotFilePath;

	
	/**
	 * Constructor for Action1.
	 */
	public ConstrainExecutionModelWithTrace() {
		super();
	}
	private String[] observedClockNames;
	private Map<String,Clock> clockNameToClock;
	private String modelFilePath;
	private Graph dotGraph;
	private Model m;


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selections = HandlerUtil.getCurrentSelection(event);
		if (selections instanceof IStructuredSelection) {
			if (((IStructuredSelection) selections).size() == 2) {
				for(Object selected: ((IStructuredSelection) selections).toList()){
					if (selected instanceof IFile) {
						if (((IFile)selected).getFileExtension().compareTo("extendedCCSL") == 0
							||
							((IFile)selected).getFileExtension().compareTo("timemodel") == 0){
								ccslFile = (IFile) selected;
								ccslFilePath = ccslFile.getLocation().toString();
								ResultsHolder.originalCCSLFilePath = "platform:/resource"+ccslFile.getFullPath();
						}
						if (((IFile)selected).getFileExtension().compareTo("trace") == 0
								||
								((IFile)selected).getFileExtension().compareTo("dot") == 0){
									dotFile = (IFile) selected;
									dotFilePath = dotFile.getLocation().toString();
							}
					}
				}
			}
		}
		Job job = new Job("MoCCML trace importer on "+ccslFile.toString()) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {	
				return doIt(monitor);
			}
		};
		job.schedule();
		return null;
	}
	protected IStatus doIt(IProgressMonitor monitor) {
		
		
		
		Resource ccslTraceResource = handleCreationOfScenarioFromTrace();
		monitor.worked(10);
		
		dotFile = null;
		dotFilePath="";
		ccslFile=null;
		ccslFilePath="";
		
		monitor.worked(100);
		return Status.OK_STATUS;
     }
	
	
	

public Resource handleCreationOfScenarioFromTrace() {
	Resource ccslResource = null;
	try {
		ccslResource = ResourceLoader.INSTANCE.loadResource(ccslFile.getFullPath());
	} catch (IOException e) {
		System.err.println("load ccsl file problem on "+ccslFilePath+ "\nexception:");
		e.printStackTrace();
	}
	
	Resource ccslTraceResource = null;
	try {
		ccslTraceResource = createScenarioFromDot(dotFile,ccslResource);
	} catch (IOException e2) {
		e2.printStackTrace();
	}
			
	
	try {
		dotFile.refreshLocal(1, null);
	} catch (CoreException e2) {
		e2.printStackTrace();
	}
	return ccslTraceResource;
}



	
	
	private Resource createScenarioFromDot(IFile dotFile, Resource ccslResource) throws IOException {
		InputStream is = null;
		try {
			is = dotFile.getContents();
		} catch (CoreException e2) {
			e2.printStackTrace();
		}
		Parser p = new Parser(is);
		boolean parseOK = false;
		try {
			parseOK = p.parse(new StringBuffer(new BufferedReader(new InputStreamReader(is))
					   .lines().collect(Collectors.joining("\n"))));
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ArrayList<Graph> al =p.getGraphs();
		dotGraph = al.get(0);
		
		modelFilePath = getModelFilePathFromCCSL(ccslResource);
		ArrayList<Clock> allCCSLClocks = getAllCCSLClocks(ccslResource);
		
		observedClockNames = getObservedClockNames(dotGraph);
		ResultsHolder.observedClockNames = observedClockNames;
				
		if(LFTrace) { //crappy way to deal with different traces, need to use an extension point
			feedLFTraceNamesToClock(allCCSLClocks);
		}else {
			clockNameToClock = computeClockMatchings(allCCSLClocks, observedClockNames);
		}
		ResultsHolder.clockNameToClock = clockNameToClock;
		
	
		
		String resultingCCSLFilePath="";
		try {
			resultingCCSLFilePath = createScenarioFile(clockNameToClock,observedClockNames, allCCSLClocks);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ResourceLoader.INSTANCE.loadResource(resultingCCSLFilePath);
	}

	private String createScenarioFile(Map<String, Clock> clockNameToClock,String[] observedClockNames, ArrayList<Clock> allCCSLClocks) throws IOException {
		RegularFile scenarioFile = new RegularFile(ccslFile.getParent().getLocation().toString()+"/scenarioFromDotFile.moccmlscenario");
		if(scenarioFile.exists()){
			scenarioFile.delete();
		}
			
		createScenarioHeader(scenarioFile);
		createContentScenario(scenarioFile);
		
		return ccslFile.getParent().getFullPath().toString()+"/scenarioFromDotFile.moccmlscenario";
	}

	private void createScenarioHeader(RegularFile scenarioFile) throws IOException {
		scenarioFile.append("/*\n".getBytes());
		scenarioFile.append("* Scenario specification\n".getBytes());
		scenarioFile.append(" * @author:  the trace checker written bu Julien Deantoni\n".getBytes());
		scenarioFile.append((" * date :  "+toools.util.Date.now("yyyy-MM-dd HH:mm:ss")).getBytes());
		scenarioFile.append(" */\n".getBytes());
		scenarioFile.append("Scenario traceScenario \n".getBytes());
		scenarioFile.append(("	importModel \"platform:/resource"+ccslFile.getFullPath().toString()+"\";\n").getBytes());
		scenarioFile.append(("	importModel \""+modelFilePath+"\";\n").getBytes());
		scenarioFile.append(("	importClass linguafranca.xdsml.api.impl.LinguaFrancaRTDAccessor;\n").getBytes());
		scenarioFile.append((" 	Variable helper : LinguaFrancaRTDAccessor;\n").getBytes());
		scenarioFile.append("     \n".getBytes());
	}
	private void createContentScenario (RegularFile scenarioFile) throws IOException{

		//hopefully they are sorted
		for(Node node : dotGraph.getNodes(false)){
			
			List<Edge> allOutgoingEdges = dotGraph.getEdges().stream().filter(e -> e.getSource().getNode().getId().getId() == node.getId().getId()).map(o -> (Edge)o).collect(Collectors.toList());
			String sep ="";
			for(Edge outgoingEdge : allOutgoingEdges) {
				sep = "";
				String label = outgoingEdge.getAttributes().get("label");
				if(label.startsWith("schedule")) {
					label=label.substring(label.indexOf("(")+1);
					label=label.substring(0, label.indexOf(")"));
					String[] splittedLine = label.split(",");
					String actionQN= getActionQN(splittedLine[0]);
					scenarioFile.append(("execute helper.setnextSchedule("+actionQN+","+splittedLine[1]+")\n").getBytes());
					label = outgoingEdge.getAttributes().get("label");
					label=label.substring(label.indexOf("(")+1);
					label=label.substring(0, label.indexOf(","));
					continue;
				}
				scenarioFile.append(("		expect ").getBytes());
				String[] splittedLine = label.split(",");
				String allClocks = "";
				for (int j = 0; j < splittedLine.length; j++){
					allClocks+=sep+clockNameToClock.get(splittedLine[j]).getName().replaceAll("\\.",  "_");
					sep = " and ";
				}
				label = outgoingEdge.getAttributes().get("label");

				scenarioFile.append((allClocks+"\n").getBytes());
			
			}
		}
		
	}
	/**
	 * translate from instance.actionName to Type.actionName
	 * @param theActionQN
	 * @return
	 */
	private String getActionQN(String theActionQN) {
		String[] splittedQN = theActionQN.split("\\.");
		TreeIterator<EObject> it = m.eAllContents();
		List<Instantiation> allInstances = new ArrayList<>();
		while(it.hasNext()) {
			EObject eo = it.next();
			if (eo instanceof Instantiation) {
				allInstances.add((Instantiation) eo);
			}
		}
		Instantiation instanceOftheActionQN = allInstances.stream().filter(i -> i.getName().compareTo(splittedQN[0]) == 0).collect(Collectors.toList()).get(0);
		String typeName = instanceOftheActionQN.getReactorClass().getName();
		return typeName+"."+splittedQN[1];
	}
	private Map<String,Clock> computeClockMatchings(ArrayList<Clock> allCCSLClocks, String[] observedClockNames) {
		Map<String,Clock> clockNameToClock = new HashMap<String,Clock>(observedClockNames.length); 
		for(String clockName : observedClockNames){
			Clock matchingClock = lookForClockName(clockName,allCCSLClocks);
			if (matchingClock != null){
				clockNameToClock.put(clockName, matchingClock);
			}
		}
		return clockNameToClock;
	}

	private void feedLFTraceNamesToClock(ArrayList<Clock> allCCSLClocks) {
		clockNameToClock = new  HashMap<String,Clock>(observedClockNames.length); 
		obsLoop: for(String observedName : observedClockNames) {
			for(Clock aClock : allCCSLClocks) {
				String associatedObjectNormalizedName = computeNormalizedName(aClock);
				if(observedName.compareTo(associatedObjectNormalizedName) == 0) {
					clockNameToClock.put(observedName, aClock);
					System.out.println("OK: "+observedName);
					continue obsLoop;
				}
			}
			System.out.println("KO: "+observedName);
		}
			return;
	}
	private String computeNormalizedName(Clock aClock) {
		EObject associatedObject = getClockAssociatedObject(aClock);
		if(associatedObject == null) {
			return "noAssociatedObject";
		}
		if(! ((associatedObject instanceof Port) ||(associatedObject instanceof Action) || (associatedObject instanceof Reaction) || (associatedObject instanceof Model))) {
			return "notUseful";
		}
		if (associatedObject instanceof Model && aClock.getTickingEvent().getKind() == EventKind.START){
			return "Model.TimeAdvancement";
		}
		return getNormalizedName(associatedObject)+'.'+aClock.getName().substring(aClock.getName().lastIndexOf('_')+1);
		
	}
	
	
	private String getNormalizedName(EObject associatedObject) {
		Reactor rReactor = (Reactor) associatedObject.eContainer();
		if(rReactor == null){
			return "notUseful";
		}
		m = (Model) associatedObject.eResource().getContents().get(0);
		TreeIterator<EObject> it = m.eAllContents();
		List<Instantiation> allInstances = new ArrayList<>();
		while(it.hasNext()) {
			EObject eo = it.next();
			if (eo instanceof Instantiation) {
				allInstances.add((Instantiation) eo);
			}
		}
		List<Instantiation> instancesOfrReactor = allInstances.stream().filter(i -> i.getReactorClass().getName().compareTo(rReactor.getName()) == 0).collect(Collectors.toList());
		if (instancesOfrReactor.size() > 1) {
			System.err.println("models with multiple instantiations of the same reactor are not supported yet.\n trying to continue but without guarantee");
		}
		String reactorInstanceName = instancesOfrReactor.get(0).getName();
		
		if(associatedObject instanceof Reaction) {
			int indexOfReactionInReactor = rReactor.getReactions().indexOf(associatedObject);
			String resultingName = reactorInstanceName+"."+indexOfReactionInReactor;
			return resultingName;
		}
		if(associatedObject instanceof Action) {
			String instanceName = instancesOfrReactor.get(0).getName();
			return instanceName+"."+((Action)associatedObject).getName();
		}
		if(associatedObject instanceof Port) {
			String instanceName = instancesOfrReactor.get(0).getName();
			return instanceName+"."+((Port)associatedObject).getName();
		}
		return "not implemented case";
	}
	

	
	private EObject getClockAssociatedObject(Clock aClock) {
		if (aClock.getTickingEvent() == null) {
			return null;
		}
		EList<EObject> elemRef = aClock.getTickingEvent().getReferencedObjectRefs();
		if(elemRef.size() == 0) {
			return null;
		}
		EObject theEObject = elemRef.get(elemRef.size()-1);
		if (theEObject instanceof EOperation) {
			theEObject = elemRef.get(elemRef.size()-2);
		}
		return theEObject;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private List<String> getTraceLines() {
		RegularFile theTraceFile = new RegularFile(dotFilePath);
		List<String> traceLines =null;
		try {
			traceLines = theTraceFile.getLines();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ResultsHolder.maxTraceTime = new Integer(traceLines.get(traceLines.size()-1).split(";")[0]);
		return traceLines;
	}

	private String getModelFilePathFromCCSL(Resource ccslResource) {
		EList<ImportStatement> allCCSLImports = ((ClockConstraintSystem)ccslResource.getContents().get(0)).getImports();
		
		for(ImportStatement i : allCCSLImports) {
			if (! (   i.getImportURI().contains("moccml")
				   || i.getImportURI().contains("extendedCCSL")
				   || i.getImportURI().contains("ccslLib")
			)) {
				return i.getImportURI();
			}
				   
		}
		
		return "model not found";
	}
	
	private ArrayList<Clock> getAllCCSLClocks(Resource ccslResource) {
		EList<Element> allCCSLElements = ((ClockConstraintSystem)ccslResource.getContents().get(0)).getSuperBlock().getElements();
		ArrayList<Clock> allCCSLClocks = new ArrayList<Clock>();
		for(Element e : allCCSLElements){
			if (e instanceof Clock){
				allCCSLClocks.add((Clock)e);
			}
		}
		return allCCSLClocks;
	}

	private String[] getObservedClockNames(Graph dotGraph) {
		Set<String> res = new HashSet<>();
		for(Edge e : dotGraph.getEdges()) {
			String[] splittedLine = e.getAttributes().get("label").split(",");
			for(String clockName : splittedLine) {
				if(clockName.compareTo("LS !") == 0) {
					continue;
				}
				res.add(clockName);
			}
		}
		return res.toArray(new String[res.size()]);
	}

	private Clock lookForClockName(String clockName,ArrayList<Clock> allCCSLClocks) {
		String sanitizedClockName= clockName.substring(4);
		for(Clock c : allCCSLClocks){
			if (c.getName().compareTo(sanitizedClockName) == 0) {
				return c;
			}
//			String[] splittedName = clockName.split("_");
//			String beginOfName = splittedName[0]; //example Task2
//			String endOfName = splittedName[splittedName.length-1]; //example STARTED
//			System.out.println(beginOfName+"_"+endOfName);
//			
//			if(c.getName().contains(beginOfName)
//					&&
//			   c.getName().contains(endOfName)){
//				return c;
//			}
		}
		return null;
	}
	
	
//	
//	};
//	return job;
//	}
	
}
