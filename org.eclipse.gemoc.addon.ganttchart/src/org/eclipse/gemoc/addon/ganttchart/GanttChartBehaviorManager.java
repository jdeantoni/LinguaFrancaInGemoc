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
package org.eclipse.gemoc.addon.ganttchart;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.gemoc.execution.concurrent.ccsljavaengine.concurrentmse.FeedbackMSE;
import org.eclipse.gemoc.execution.concurrent.ccsljavaengine.engine.AbstractSolverCodeExecutorConcurrentEngine;
import org.eclipse.gemoc.execution.concurrent.ccsljavaxdsml.api.moc.ICCSLSolver;
import org.eclipse.gemoc.execution.concurrent.ccsljavaxdsml.api.moc.ISolver;
import org.eclipse.gemoc.trace.commons.model.helper.StepHelper;
import org.eclipse.gemoc.trace.commons.model.trace.MSEOccurrence;
import org.eclipse.gemoc.trace.commons.model.trace.Step;
import org.eclipse.gemoc.xdsmlframework.api.core.IExecutionEngine;
import org.eclipse.gemoc.xdsmlframework.api.engine_addon.IEngineAddon;
import org.eclipse.swt.widgets.Display;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;

import fr.inria.aoste.timesquare.ccslkernel.model.TimeModel.Clock;
import fr.inria.aoste.timesquare.ccslkernel.model.TimeModel.EventKind;
import fr.inria.aoste.timesquare.ccslkernel.model.TimeModel.NamedElement;
import fr.inria.aoste.timesquare.ccslkernel.model.TimeModel.CCSLModel.ClockExpressionAndRelation.ConcreteEntity;
import fr.inria.aoste.timesquare.ccslkernel.model.TimeModel.CCSLModel.ClockExpressionAndRelation.Relation;
import fr.inria.aoste.timesquare.instantrelation.CCSLRelationModel.CCSLConstraintRef;
import fr.inria.aoste.timesquare.instantrelation.CCSLRelationModel.OccurrenceRelation;
import fr.inria.aoste.timesquare.instantrelation.CCSLRelationModel.Precedence;
import fr.inria.aoste.trace.ModelElementReference;

public class GanttChartBehaviorManager implements IEngineAddon {

	final private List<AnimateTicksBehaviour> behaviorList = new ArrayList<AnimateTicksBehaviour>();
//	final private List<AnimateAssertBehaviour> assertBehaviorList = new ArrayList<AnimateAssertBehaviour>();
//	private CCSLInfo ccslhelper = null;	
	private ISolver _solver;
	public GanttChartForLF theChart;
	private Resource loadedResource = null; 



	@Override
	public void stepExecuted(IExecutionEngine<?> engine, Step<?> logicalStepExecuted){
		for ( AnimateTicksBehaviour b : behaviorList){
			b.finish();
		}
		
		List<OccurrenceRelation> relations = ((ICCSLSolver) _solver).getLastOccurrenceRelations();
		relations.removeIf(or -> ! (or instanceof Precedence));
		List<OccurrenceRelation> interestingRelations = new ArrayList<>();
		for(OccurrenceRelation or : relations) {
			Precedence p = (Precedence)or;
			CCSLConstraintRef constRef = (CCSLConstraintRef) p.eContainer();
			EList<EObject> elems = ((ModelElementReference)constRef.getCcslConstraint()).getElementRef();
			EObject associatedConstraint = elems.get(elems.size()-1);
			if(associatedConstraint instanceof Relation
					&&
				((Relation)associatedConstraint).getName().contains("ConnectorSourcePresenceCausesConnectorTargetPresence")	
					) {
				interestingRelations.add(or);
			}
//			System.out.println(p);
		}
//		relations.removeIf(or -> ! ((Precedence)or).));
		for(MSEOccurrence occ : StepHelper.collectAllMSEOccurrences(logicalStepExecuted)){
			if(occ.getMse() instanceof FeedbackMSE){
				Clock c = (Clock) ((FeedbackMSE)occ.getMse()).getFeedbackModelSpecificEvent().getSolverEvent();
				
				for ( AnimateTicksBehaviour b : behaviorList){
					b.instantRelations.addAll(interestingRelations); //may be optimized
					ConcreteEntity ce = ((GemocClockEntity)b.getClock())._ce;
					if (ce.getName().compareTo(c.getName()) == 0){ //TODO: Fix this ugly comparison
						if(c.getTickingEvent().getKind() != EventKind.UNDEFINED) {
							b.start();
							break;
						}
					}
				}
			}
		}
	}
	
	@Override
	public void engineAboutToStop(IExecutionEngine<?> engine) {
	//public void end(ConfigurationHelper helper) {
		try {
			Display.getDefault().syncExec(new RunnableEnd());						
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}


	@Override
	public void engineStopped(IExecutionEngine<?> engine) {
	//public void clear() {
		behaviorList.clear();
	}


	@Override
	public void engineStarted(IExecutionEngine<?> executionEngine) {
	//public void beforeExecution(ConfigurationHelper helper, IPath folderin, String namefilein, ISolverForBackend solver) {
//		try {
//			Display.getDefault().syncExec(new RunnableStart());						
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
	}	
	
	private class RunnableEnd implements Runnable {

		@Override
		public void run() {
			for (AnimateTicksBehaviour behavior : behaviorList) {
				behavior.finish();
			}
		}
		
	}
//	private final class RunnableStart implements Runnable {
//		public void run() {			
//							
//			for (AnimateTicksBehaviour behavior : behaviorList) {
//				behavior.start();
//			}
////			for (AnimateAssertBehaviour assertBehavior : assertBehaviorList) {
////				assertBehavior.setEditPart(_diagramEditPart);	 	
////				assertBehavior.start();
////			}
//		}
//	}



	@Override
	public void engineAboutToStart(final IExecutionEngine<?> engine) {
		_solver = ((AbstractSolverCodeExecutorConcurrentEngine)engine).getSolver();
		loadedResource = engine.getExecutionContext().getResourceModel();
		List<OccurrenceRelation> relations = ((ICCSLSolver) _solver).getLastOccurrenceRelations();

		URI fileUri = engine.getExecutionContext().getResourceModel().getURI();

		Display.getDefault().syncExec(new Runnable() {
			@Override
		    public void run() {
				
		        String platformString = fileUri.toPlatformString(true);
		        IResource modelIFile = ResourcesPlugin.getWorkspace().getRoot().findMember(platformString);
		        
		      //create the gantt chart
		        
		    }
		});
		
		ArrayList<String> reactionNames = new ArrayList<>();
		
		loop1: for(ModelElementReference mer : ((ICCSLSolver)_solver).getAllDiscreteClocks()){
				GemocClockEntity ced = new GemocClockEntity(mer);
				//TODO: fix this ugly comparison !!!
				if (mer.getElementRef().size() != 3)
					continue loop1;
				for (AnimateTicksBehaviour b : behaviorList) {
					if (b._ce == ced)
						continue loop1;
				}
				if(ced.getName().contains("startExecution")) {
					AnimateTicksBehaviour atb = new AnimateTicksBehaviour(ced, loadedResource, (ICCSLSolver) _solver);
					reactionNames.add(atb.getDescription());
					behaviorList.add(atb);
				}
			}
		

		String[] reactionNamesArray = new String[reactionNames.size()];
        System.arraycopy(reactionNames.toArray(), 0, reactionNamesArray, 0, reactionNamesArray.length);
		theChart = new GanttChartForLF("Lingua Franca Execution", fileUri.lastSegment(), reactionNamesArray);
		theChart.pack();
		theChart.setVisible(true);
		for ( AnimateTicksBehaviour b : behaviorList){
			b.setTheChart(theChart);
		}
	}

	
	
	

	
}
