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
package org.eclipse.gemoc.addon.klighdanimator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.gemoc.execution.concurrent.ccsljavaengine.concurrentmse.FeedbackMSE;
import org.eclipse.gemoc.execution.concurrent.ccsljavaengine.engine.AbstractSolverCodeExecutorConcurrentEngine;
import org.eclipse.gemoc.execution.concurrent.ccsljavaengine.engine.MoccmlExecutionEngine;
import org.eclipse.gemoc.execution.concurrent.ccsljavaxdsml.api.moc.ICCSLSolver;
import org.eclipse.gemoc.execution.concurrent.ccsljavaxdsml.api.moc.ISolver;
import org.eclipse.gemoc.trace.commons.model.helper.StepHelper;
import org.eclipse.gemoc.trace.commons.model.trace.MSEOccurrence;
import org.eclipse.gemoc.trace.commons.model.trace.Step;
import org.eclipse.gemoc.xdsmlframework.api.core.IExecutionEngine;
import org.eclipse.gemoc.xdsmlframework.api.engine_addon.IEngineAddon;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import de.cau.cs.kieler.klighd.IViewer;
import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.ui.view.DiagramView;
import fr.inria.aoste.timesquare.ccslkernel.model.TimeModel.Clock;
import fr.inria.aoste.timesquare.ccslkernel.model.TimeModel.EventKind;
import fr.inria.aoste.timesquare.ccslkernel.model.TimeModel.CCSLModel.ClockExpressionAndRelation.ConcreteEntity;
import fr.inria.aoste.timesquare.launcher.core.inter.CCSLInfo;
import fr.inria.aoste.trace.ModelElementReference;

public class KLighDAnimatorBehaviorManager implements IEngineAddon {

	private ViewContext _viewContext = null;
	final private List<AnimateTicksBehaviour> behaviorList = new ArrayList<AnimateTicksBehaviour>();
//	final private List<AnimateAssertBehaviour> assertBehaviorList = new ArrayList<AnimateAssertBehaviour>();
//	private CCSLInfo ccslhelper = null;	
	private ISolver _solver;

	



	@Override
	public void stepExecuted(IExecutionEngine<?> engine, Step<?> logicalStepExecuted){
		for ( AnimateTicksBehaviour b : behaviorList){
			b.finish();
		}
		
		for(MSEOccurrence occ : StepHelper.collectAllMSEOccurrences(logicalStepExecuted)){
			if(occ.getMse() instanceof FeedbackMSE){
				Clock c = (Clock) ((FeedbackMSE)occ.getMse()).getFeedbackModelSpecificEvent().getSolverEvent();
				
				for ( AnimateTicksBehaviour b : behaviorList){
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
		_viewContext = null;
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

	/**
     * Returns the diagram view context.
     * 
     * @return the diagram view context
     */
    protected ViewContext getDiagramViewContext() {
    	 
    	
    	List<DiagramView> diagramViews = DiagramView.getAllDiagramViews();
        if (diagramViews != null && !diagramViews.isEmpty()) {
            DiagramView viewPart = diagramViews.get(diagramViews.size()-1);
            IViewer viewer = viewPart.getViewer();
            return viewer.getViewContext();
        }
        return null;
    }

	@Override
	public void engineAboutToStart(final IExecutionEngine<?> engine) {
		_solver = ((AbstractSolverCodeExecutorConcurrentEngine)engine).getSolver();
		Resource resInEngine = ((MoccmlExecutionEngine)engine).getExecutionContext().getResourceModel();
		Display.getDefault().syncExec(new Runnable() {
		    @Override
		    public void run() {
				//ensure the editor is open and active for klighd
				IWorkbench wb = PlatformUI.getWorkbench();
		        IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
		        IWorkbenchPage page = window.getActivePage();
		        
		        URI fileUri = engine.getExecutionContext().getResourceModel().getURI();
		        String platformString = fileUri.toPlatformString(true);
		        IResource modelIFile = ResourcesPlugin.getWorkspace().getRoot().findMember(platformString);
		        
		        
		        IEditorDescriptor desc = PlatformUI.getWorkbench().
		       	        getEditorRegistry().getDefaultEditor(modelIFile.getName());
		       	try {
					page.openEditor(new FileEditorInput((IFile) modelIFile), desc.getId());
				} catch (PartInitException e) {
					e.printStackTrace();
				}
		    }
		});
		
		
		
		
		_viewContext = getDiagramViewContext();
		
		loop1: for(ModelElementReference mer : ((ICCSLSolver)_solver).getAllDiscreteClocks()){
				GemocClockEntity ced = new GemocClockEntity(mer);
				//TODO: fix this ugly comparison !!!
				if (mer.getElementRef().size() != 3)
					continue loop1;
				for (AnimateTicksBehaviour b : behaviorList) {
					if (b._ce == ced)
						continue loop1;
				}
				AnimateTicksBehaviour atb = new AnimateTicksBehaviour(ced, _viewContext, resInEngine);
				behaviorList.add(atb);
			}
	}

	
	
	

	
}
