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
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;

import toools.io.file.RegularFile;
import fr.inria.aoste.timesquare.ccslkernel.model.TimeModel.Clock;
import fr.inria.aoste.timesquare.ccslkernel.model.TimeModel.BasicType.Element;
import fr.inria.aoste.timesquare.ccslkernel.model.TimeModel.CCSLModel.ClockConstraintSystem;
import fr.inria.aoste.timesquare.ccslkernel.model.utils.ResourceLoader;

public class ConstrainExecutionModelWithTrace extends AbstractHandler {
	

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
		
		
		
		Resource ccslTraceResource = handleCreationOfCCSLFromTrace();
		monitor.worked(10);
		
		dotFile = null;
		dotFilePath="";
		ccslFile=null;
		ccslFilePath="";
		
		monitor.worked(100);
		return Status.OK_STATUS;
     }
	
	
	

public Resource handleCreationOfCCSLFromTrace() {
	Resource ccslResource = null;
	try {
		ccslResource = ResourceLoader.INSTANCE.loadResource(ccslFile.getFullPath());
	} catch (IOException e) {
		System.err.println("load ccsl file problem on "+ccslFilePath+ "\nexception:");
		e.printStackTrace();
	}
	
	Resource ccslTraceResource = null;
	try {
		ccslTraceResource = createCCSLFromDot(dotFile,ccslResource);
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



	
	
	private Resource createCCSLFromDot(IFile dotFile, Resource ccslResource) throws IOException {
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
		Graph dotGraph = al.get(0);
		
		ArrayList<Clock> allCCSLClocks = getAllCCSLClocks(ccslResource);
		
		observedClockNames = getObservedClockNames(dotGraph);
		ResultsHolder.observedClockNames = observedClockNames;
				
		clockNameToClock = computeClockMatchings(allCCSLClocks, observedClockNames);
		ResultsHolder.clockNameToClock = clockNameToClock;
		
		String traceSpecificConstraintDefinitionPath="";
		try {
			traceSpecificConstraintDefinitionPath = createConstraintSpecificMoCCMLDefinition(dotGraph, observedClockNames);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String resultingCCSLFilePath="";
		try {
			resultingCCSLFilePath = createCCSLFile(clockNameToClock,observedClockNames,traceSpecificConstraintDefinitionPath, allCCSLClocks);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ResourceLoader.INSTANCE.loadResource(resultingCCSLFilePath);
	}

	private String createCCSLFile(Map<String, Clock> clockNameToClock,String[] observedClockNames, String traceSpecificConstraintDefinitionPath, ArrayList<Clock> allCCSLClocks) throws IOException {
		RegularFile traceCCSLFile = new RegularFile(ccslFile.getParent().getLocation().toString()+"/SpecAugmentedWithTrace.extendedCCSL");
		if(traceCCSLFile.exists()){
			traceCCSLFile.delete();
		}
			
		createCCSLHeader(traceCCSLFile, traceSpecificConstraintDefinitionPath);
		
//		ResultsHolder.physicalClockName = "";
//		for(Clock c : allCCSLClocks){
//			if (c.getName().endsWith("_ms")){
//				ResultsHolder.physicalClockName = c.getName();
//				break;
//			}
//		}
		createCCSLTraceRelation(traceCCSLFile, clockNameToClock,observedClockNames);//, ResultsHolder.physicalClockName);
		
		return ccslFile.getParent().getFullPath().toString()+"/SpecAugmentedWithTrace.extendedCCSL";
	}
	private void createCCSLTraceRelation(RegularFile traceCCSLFile, Map<String, Clock> clockNameToClock,String[] observedClockNames) throws IOException{
		traceCCSLFile.append(("\t Relation theUltimateRelation[TraceSpecificConstraint](\n").getBytes());
		String sep="";
		for(String observedClockName : observedClockNames ){
			traceCCSLFile.append(("\t\t"+sep+"TraceSpecificConstraint_"+observedClockName+"-> "+clockNameToClock.get(observedClockName).getName()+" \n").getBytes());
			sep=",";
		}
		traceCCSLFile.append("\t)\n".getBytes());
		traceCCSLFile.append("\t}\n".getBytes());
		traceCCSLFile.append("}\n".getBytes());
	}
	private void createCCSLHeader(RegularFile traceCCSLFile, String traceSpecificConstraintDefinitionPath) throws IOException {
		traceCCSLFile.append("/*\n".getBytes());
		traceCCSLFile.append("* CCSL specification\n".getBytes());
		traceCCSLFile.append(" * @author:  the trace checker written bu Julien Deantoni\n".getBytes());
		traceCCSLFile.append(" * date :  Thu June 9 2016  10:51:42 CEST \n".getBytes());
		traceCCSLFile.append(" */\n".getBytes());
		traceCCSLFile.append("ClockConstraintSystem traceSpecification {\n".getBytes());
		traceCCSLFile.append("    imports {\n".getBytes());
		traceCCSLFile.append("        // import statements\n".getBytes());
		traceCCSLFile.append("		import \"platform:/plugin/fr.inria.aoste.timesquare.ccslkernel.model/ccsllibrary/kernel.ccslLib\" as lib0;\n".getBytes()); 
		traceCCSLFile.append("		import \"platform:/plugin/fr.inria.aoste.timesquare.ccslkernel.model/ccsllibrary/CCSL.ccslLib\" as lib1; \n".getBytes());
		traceCCSLFile.append(("		import \""+traceSpecificConstraintDefinitionPath+"\" as TSC;\n").getBytes());
		traceCCSLFile.append(("		import \"platform:/resource"+ccslFile.getFullPath().toString()+"\" as theSpec;\n").getBytes());
		traceCCSLFile.append("    }\n".getBytes());
		traceCCSLFile.append("    entryBlock main\n".getBytes());
		traceCCSLFile.append("     \n".getBytes());
		traceCCSLFile.append("        Block main {\n".getBytes());
	}
	private String createConstraintSpecificMoCCMLDefinition (Graph dotGraph, String[] observedClockNames) throws IOException{
		RegularFile constraintDefFile = new RegularFile(ccslFile.getParent().getLocation().toString()+"/TraceSpecificConstraint.moccml");
		
		if(constraintDefFile.exists()){
			constraintDefFile.delete();
		}
	
		createConstraintDefHeader(constraintDefFile);
//		createConstraintDefVariables(constraintDefFile, dotGraph);
		createConstraintDefStates(constraintDefFile, dotGraph);
		createConstraintDefFooter(constraintDefFile, observedClockNames);
	
		constraintDefFile.create();
		return "platform:/resource"+ccslFile.getParent().getFullPath().toString()+"/TraceSpecificConstraint.moccml";
	}

	private void createConstraintDefFooter(RegularFile constraintDefFile, String[] observedClockNames) throws IOException{
		constraintDefFile.append("\n".getBytes());
		constraintDefFile.append("RelationDeclaration	TraceSpecificConstraint(".getBytes());
		String sep = "";
		for(String aClock : observedClockNames){
			constraintDefFile.append((sep+"TraceSpecificConstraint_"+aClock+":clock").getBytes());
			sep=",";
		}
		constraintDefFile.append(")\n".getBytes());
		constraintDefFile.append("	           }\n".getBytes());
		constraintDefFile.append("}\n".getBytes());
	}
	
	private void createConstraintDefStates(RegularFile constraintDefFile,Graph dotGraph) throws IOException{
		constraintDefFile.append(("	          init:  fakeInit\n"
				+ "	           State fakeInit(\n"
				+ "	           	out : fakeTransition\n"
				+ "	           )\n"
				+ "	           from fakeInit to s0 : fakeTransition -> ()").getBytes());
		
		for(Node node : dotGraph.getNodes(false)){
			constraintDefFile.append(("	           State s"+node.getId().getId()+"(\n").getBytes());
			
			List<Edge> allIncomingEdges = dotGraph.getEdges().stream().filter(e -> e.getTarget().getNode().getId().getId() == node.getId().getId()).map(o -> (Edge)o).collect(Collectors.toList());
			if(allIncomingEdges.size() > 0 ) {
				constraintDefFile.append(("	           		in: ").getBytes());
			}
			String sep ="";
			for(Edge incomingEdge : allIncomingEdges) {
				constraintDefFile.append((sep+"s"+incomingEdge.getSource().getNode().getId().getId()+"s"+incomingEdge.getTarget().getNode().getId().getId()).getBytes());
				sep = ",";
			}
//			if (i!=1){
//				constraintDefFile.append((", s"+(i-1)+"s"+i+"bis, s"+(i-1)+"s"+i+"ter\n").getBytes());				
//			}else{
				constraintDefFile.append(("\n").getBytes());
//			}
			List<Edge> allOutgoingEdges = dotGraph.getEdges().stream().filter(e -> e.getSource().getNode().getId().getId() == node.getId().getId()).map(o -> (Edge)o).collect(Collectors.toList());
			if (allOutgoingEdges.size() > 0) {
							constraintDefFile.append(("					out : ").getBytes());
			}
			sep ="";
			for(Edge outgoingEdge : allOutgoingEdges) {
				constraintDefFile.append((sep+"s"+outgoingEdge.getSource().getNode().getId().getId()+"s"+outgoingEdge.getTarget().getNode().getId().getId()).getBytes());
				sep = ",";
			}
			constraintDefFile.append(("\n			   )\n").getBytes());
			
			for(Edge outgoingEdge : dotGraph.getEdges().stream().filter(e -> e.getSource().getNode().getId().getId() == node.getId().getId()).map(o -> (Edge)o).collect(Collectors.toList())) {
				String sourceName = "s"+outgoingEdge.getSource().getNode().getId().getId();
				String targetName = "s"+outgoingEdge.getTarget().getNode().getId().getId();
				String[] splittedLine = outgoingEdge.getAttributes().get("label").split(":");
				sep = "";
				String allClocks = "";
				for (int j = 1; j < splittedLine.length; j++){
					allClocks+=sep+" TraceSpecificConstraint_"+splittedLine[j];
					sep = ",";
				}
				constraintDefFile.append(("				from "+sourceName+" to "+targetName+" : "+sourceName+targetName+" -> ( when "+ allClocks +")").getBytes());
			}
		}
		constraintDefFile.append(("			   \n}\n").getBytes());
		
	}
//	private void createConstraintDefVariables(RegularFile constraintDefFile, List<String> traceLines) throws IOException {
//		constraintDefFile.append("			variables { \n".getBytes());
//		constraintDefFile.append(" 				Integer un = 1\n".getBytes());
//		constraintDefFile.append(" 				Integer currentTime = 0\n".getBytes());
//		int i = 1;
//		for(String line : traceLines){
//			constraintDefFile.append((" 				Integer timeStamp"+(i++)+" = "+line.substring(0,line.indexOf(';'))+"\n").getBytes());
//		}
//		constraintDefFile.append(" 			}\n".getBytes());
//	}

	private void createConstraintDefHeader(RegularFile constraintDefFile) throws IOException {
		constraintDefFile.append("AutomataConstraintLibrary temporalConstraints{\n".getBytes());
		constraintDefFile.append("   import 'platform:/plugin/fr.inria.aoste.timesquare.ccslkernel.model/ccsllibrary/kernel.ccslLib' as kernel;\n".getBytes());
		constraintDefFile.append("\n".getBytes());
		constraintDefFile.append("   RelationLibrary temporalRelations{\n".getBytes());
		constraintDefFile.append("\n".getBytes());
		constraintDefFile.append("      AutomataRelationDefinition TraceSpecificConstraintDef[TraceSpecificConstraint]{\n".getBytes());
		constraintDefFile.append("\n".getBytes());
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
			String[] splittedLine = e.getAttributes().get("label").split(":");
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
