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

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.gemoc.execution.concurrent.ccsljavaxdsml.api.moc.ICCSLSolver;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYIntervalSeries;
import org.lflang.lf.Reaction;
import org.lflang.lf.Reactor;
import org.lflang.lf.TriggerRef;
import org.lflang.lf.VarRef;

import fr.inria.aoste.timesquare.backend.manager.visible.ClockEntity;
import fr.inria.aoste.timesquare.ccslkernel.model.TimeModel.Clock;
import fr.inria.aoste.timesquare.instantrelation.CCSLRelationModel.OccurrenceRelation;
import fr.inria.aoste.timesquare.instantrelation.CCSLRelationModel.Precedence;
import fr.inria.aoste.timesquare.trace.util.adapter.AdapterRegistry;
import fr.inria.aoste.timesquare.trace.util.adapter.IModelAdapter.EventEnumerator;
import fr.inria.aoste.trace.ModelElementReference;
import linguafranca.xdsml.api.impl.LinguaFrancaRTDAccessor;

public class AnimateTicksBehaviour {
	
	final ClockEntity _ce;
	private EventEnumerator ek = null;
	private XYIntervalSeries lfReactions;
	private GanttChartForLF theChart = null;


	private EObject modelRoot = null;
	private int taskSize = 50;
	private String name;
	private Resource loadedResource = null;
	private ICCSLSolver solver = null;
	
	public Set<OccurrenceRelation> instantRelations = new HashSet<OccurrenceRelation>();
	private Reaction theReaction;
	private int lineIndex;
	
	public ClockEntity getClock() {
		return _ce;
	}
	
	
	public AnimateTicksBehaviour(ClockEntity ce, Resource resource, ICCSLSolver s) {
		super();
		_ce = ce;		
		loadedResource = resource;
		solver = s;
		ek=AdapterRegistry.getAdapter(_ce.getClock()).getEventkind(_ce.getClock());
		if (_ce.getReferencedElement().size() == 0) {
			return;
		}
		EObject associatedObject = _ce.getReferencedElement().get(_ce.getReferencedElement().size()-1);
		if(associatedObject instanceof EOperation){
			associatedObject = _ce.getReferencedElement().get(_ce.getReferencedElement().size()-2);
		}
		name = ((Reactor)associatedObject.eContainer()).getName()+"."+((((Reaction)associatedObject).getName() != null) ? ((Reaction)associatedObject).getName() : ((Reactor)associatedObject.eContainer()).getReactions().indexOf(associatedObject));
		
		modelRoot = loadedResource.getContents().get(0);
		EList<EObject> ce_er = _ce.getModelElementReference().getElementRef();
		Clock taskClock = (Clock) ce_er.get(ce_er.size()-1);
		theReaction = (Reaction) taskClock.getTickingEvent().getReferencedObjectRefs().get(0);
		
	}

	public String getDescription() {	
		return name;
	}

	public void setTheChart(GanttChartForLF theChart) {
		this.theChart = theChart;
		lfReactions = new XYIntervalSeries(name);
		theChart.theDatatSet.addSeries(lfReactions);
		lineIndex = theChart.theDatatSet.getSeriesCount()-1;
	}
		
	public void start() {
		Integer ct = LinguaFrancaRTDAccessor.getcurrentTime(modelRoot);
		LFTask tmp = new LFTask(theReaction, lineIndex, ct, taskSize);
		lfReactions.add(tmp, true);
		lookForOR: for (OccurrenceRelation r : instantRelations) {
			if (r instanceof Precedence) {

				
				EList<EObject> target_er = ((ModelElementReference)((Precedence)r).getTarget().getReferedElement()).getElementRef();
				if(target_er.size() == 0) {
					continue;
				}
				if (! (target_er.get(target_er.size()-1) instanceof Clock)) {
					continue; //is an Expression
				}
				Clock targetClock = (Clock) target_er.get(target_er.size()-1);
				EObject varRef = targetClock.getTickingEvent().getReferencedObjectRefs().get(0);
				
				
				if (! (varRef instanceof VarRef)) {
					continue;
				}
				boolean isATrigger = false;
				for(TriggerRef tr : theReaction.getTriggers()) {
					if (tr instanceof VarRef) {
						if (((VarRef) tr).getVariable() == ((VarRef)varRef).getVariable()) {
							isATrigger = true;
							break;
						}
					}
				}
				if (! isATrigger) {
					continue;
				}
				EList<EObject> source_er = ((ModelElementReference)((Precedence)r).getSource().getReferedElement()).getElementRef();
				if(source_er.size() == 0) {
					continue;
				}
				if (! (source_er.get(source_er.size()-1) instanceof Clock)) {
					continue;
				}
				Clock sourceClock = (Clock) source_er.get(source_er.size()-1);
				EObject sourceVarRef = sourceClock.getTickingEvent().getReferencedObjectRefs().get(0);
				
				
				if (! (sourceVarRef instanceof VarRef)) {
					continue;
				}
				LFTask sourceTask = null;
				List<XYIntervalSeries> allSeries = (List<XYIntervalSeries>)theChart.theDatatSet.data;
				for(XYIntervalSeries ts : allSeries) {
					for(int i = 0; i <ts.getItemCount(); i++) {
						LFTask t = (LFTask) ts.getDataItem(i);
						for(TriggerRef tr : t.reaction.getEffects()) {
							if (tr instanceof VarRef) {
								if (((VarRef) tr).getVariable() == ((VarRef)sourceVarRef).getVariable()) {
									sourceTask = t; //store the last one
									break;
								}
							}
						}
					}
				}
				
				
				if (sourceTask != null) {
					System.out.println("checked !");
					XYPlot plot = (XYPlot) theChart.chart.getPlot();
//					if(sourceTask.getYLowValue() == tmp.getYLowValue()) {
//						tmp.setYValue(tmp.getYLowValue()+taskSize);
//						plot.setDataset(plot.getDataset());
//					}
					
					XYLineAnnotation c = new XYLineAnnotation(sourceTask.getX(),sourceTask.getYHighValue(),tmp.getX(), tmp.getYValue(), new BasicStroke(2.0f), Color.red);
					
					plot.addAnnotation(c);
				}
				
				
			}
		}
		instantRelations.clear();
	}

	void finish() {
//		for(Highlighting hl : _associatedHighlighting) {
//			hl.remove();
//		}
	}

	

}
