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
import java.util.Collection;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.lflang.lf.Model;
import org.lflang.lf.Reactor;
import org.lflang.lf.VarRef;

import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.kgraph.KLabel;
import de.cau.cs.kieler.klighd.kgraph.KNode;
import de.cau.cs.kieler.klighd.krendering.Colors;
import de.cau.cs.kieler.klighd.krendering.KText;
import fr.inria.aoste.timesquare.backend.manager.visible.ClockEntity;
import fr.inria.aoste.timesquare.trace.util.adapter.AdapterRegistry;
import fr.inria.aoste.timesquare.trace.util.adapter.IModelAdapter.EventEnumerator;
import fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.EventQueue;
import linguafranca.xdsml.api.impl.LinguaFrancaRTDAccessor;

public class DataRepresentationBehaviour {
	
	final ClockEntity _ce;
	private EventEnumerator ek = null;
	private ArrayList<Highlighting> _associatedHighlighting;
	private String initialLabel = null;
	private EObject associatedObject;
	private ViewContext _vc;
	private Resource engineResource;
	private VarRef vRefInEngine;
	private KLabel label;
	private Model modelInEngine;
	private KText text;
	
	public ClockEntity getClock() {
		return _ce;
	}
	
		
	public DataRepresentationBehaviour(ClockEntity ce, ViewContext vc, Resource resInEngine) {
		super();
		_vc = vc;
		engineResource = resInEngine;
		_ce = ce;		
		ek=AdapterRegistry.getAdapter(_ce.getClock()).getEventkind(_ce.getClock());
		_associatedHighlighting = new ArrayList<Highlighting>();
		if (_ce.getReferencedElement().size() == 0) {
			return;
		}
		associatedObject = _ce.getReferencedElement().get(_ce.getReferencedElement().size()-1);
		if(associatedObject instanceof EOperation){
			associatedObject = _ce.getReferencedElement().get(_ce.getReferencedElement().size()-2);
		}
		EObject associatedObjectInKlighRes = null;
		try {
			associatedObjectInKlighRes = ((EObject)vc.getInputModel()).eResource().getEObject(EcoreUtil.getURI(associatedObject).fragment());
		}
		catch (java.lang.IllegalArgumentException e) {
			System.err.println(e);
			return;
		}
		Collection<EObject> diagramElements = vc.getTargetElements(associatedObjectInKlighRes);
		
		if (associatedObject.eClass().getName().contains("VarRef")) { //crappy but useful
			VarRef vRef = (VarRef)associatedObject;
			VarRef vRefInKlighRes = (VarRef) ((EObject)_vc.getInputModel()).eResource().getEObject(EcoreUtil.getURI(vRef).fragment());
			Collection<EObject> vRefDiagramElements = _vc.getTargetElements(vRefInKlighRes.getVariable());
			vRefInEngine = (VarRef) engineResource.getEObject(EcoreUtil.getURI(vRef).fragment());
			if (ek == EventEnumerator.PRODUCE || ek == EventEnumerator.FINISH)
				for(EObject de : vRefDiagramElements ) {
					if (de instanceof KLabel) {
						Integer value = LinguaFrancaRTDAccessor.getcurrentValue(vRefInEngine.getVariable());
						label = ((KLabel)de);
						initialLabel  = label.getText();
						break;
					}
				}
		}
		
		if (associatedObject.eClass().getName().contains("Model")) { //crappy but useful
			Model model = (Model)associatedObject;
			Reactor mainReactor = (Reactor) model.getReactors().stream().filter(r -> r.isMain()).toArray()[0];
			Reactor mainReactorInKlighRes = (Reactor) ((EObject)_vc.getInputModel()).eResource().getEObject(EcoreUtil.getURI(mainReactor).fragment());
			Collection<EObject> mainReactorDiagramElements = _vc.getTargetElements(mainReactorInKlighRes);
			modelInEngine = (Model) engineResource.getEObject(EcoreUtil.getURI(model).fragment());
			if (ek == EventEnumerator.START || true)
				for(EObject de : mainReactorDiagramElements ) {
					if (de instanceof KNode) {
						text = null;
						TreeIterator<EObject> it = de.eAllContents();
						while(it.hasNext()) {
							EObject eo = it.next();
							if(eo instanceof KText) {
								text = (KText) eo;
								if (text.getText() != null) {
									break;
								}
							}
						}
					}
				}
		}
		
		
	}

	public String getDescription() {	
		return "Data Representation Behaviour";
	}

	public void start() {

		/**
		 * crappy way to specify the animation
		 */
		if (associatedObject.eClass().getName().contains("VarRef")) { //crappy but useful
			Integer value = LinguaFrancaRTDAccessor.getcurrentValue(vRefInEngine.getVariable());
			if (initialLabel == null) {
				return;
			}
			if (initialLabel.contains("(")) {
				return;
			}
			label.setText(initialLabel+"("+((value == null) ? "┴" : value.toString())+")");
		}
		
		if (associatedObject.eClass().getName().contains("Model")) { //crappy but useful
			Integer ct = LinguaFrancaRTDAccessor.getcurrentTime(modelInEngine);
			Integer cms = LinguaFrancaRTDAccessor.getcurrentMicroStep(modelInEngine);
			EventQueue eq = LinguaFrancaRTDAccessor.geteventQueue(modelInEngine);
			text.setText(" @"+ct.toString()+","+cms.toString()+" -- "+eq);
		}
			
	}

	void finish() {
		
	}

}
