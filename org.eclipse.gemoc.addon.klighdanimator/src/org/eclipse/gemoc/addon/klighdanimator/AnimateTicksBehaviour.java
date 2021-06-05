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
import linguafranca.xdsml.api.impl.LinguaFrancaRTDAccessor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.swt.widgets.Display;
import org.lflang.lf.VarRef;
import org.lflang.lf.Variable;

import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.kgraph.KGraphElement;
import de.cau.cs.kieler.klighd.kgraph.KLabel;
import de.cau.cs.kieler.klighd.kgraph.KLabeledGraphElement;
import de.cau.cs.kieler.klighd.kgraph.KPort;
import de.cau.cs.kieler.klighd.kgraph.util.KGraphUtil;
import de.cau.cs.kieler.klighd.krendering.Colors;
import de.cau.cs.kieler.klighd.krendering.KForeground;
import de.cau.cs.kieler.klighd.krendering.KRenderingFactory;
import de.cau.cs.kieler.klighd.krendering.KRoundedRectangle;
import fr.inria.aoste.timesquare.backend.manager.visible.ClockEntity;
import fr.inria.aoste.timesquare.ccslkernel.model.TimeModel.EventKind;
import fr.inria.aoste.timesquare.trace.util.adapter.AdapterRegistry;
import fr.inria.aoste.timesquare.trace.util.adapter.IModelAdapter.EventEnumerator;

public class AnimateTicksBehaviour {
	
	final ClockEntity _ce;
	private EventEnumerator ek = null;
	private ArrayList<Highlighting> _associatedHighlighting;
	private String initialLabel = null;
	private EObject associatedObject;
	private ViewContext _vc;
	private Resource engineResource;
	
	public ClockEntity getClock() {
		return _ce;
	}
	
	
	private Colors getEKColor(EventEnumerator ek) {
		switch (ek) {
		case START:
			return Colors.RED;
		case SEND:
			return Colors.GREEN;
		case PRODUCE:
			return Colors.BLUE;
		case FINISH:
			return Colors.GRAY;
		case CONSUME:
			return Colors.BLUE_VIOLET;
		case RECEIVE:
			return Colors.GREEN_4;
		case UNDEFINED:
			return Colors.RED;
		}
		return Colors.RED;
	}
	
	private KForeground createHighlightStyle(EventEnumerator ek) {
		KForeground style = KRenderingFactory.eINSTANCE.createKForeground();
		style.setColor(getEKColor(ek));
		style.setPropagateToChildren(true);
        return style;
    }
	
	
	
	public AnimateTicksBehaviour(ClockEntity ce, ViewContext vc, Resource resInEngine) {
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
		
		for(EObject diagramElement : diagramElements ) {
				Highlighting hl = new Highlighting(diagramElement, createHighlightStyle(ek));
				_associatedHighlighting.add(hl); 
				
		}
		
		
		
		
	}

	public String getDescription() {	
		return "KlighD Animator";
	}

	public void start() {

		label : for(Highlighting hl : _associatedHighlighting) {
			if (associatedObject.eClass().getName().contains("Variable")) { //crappy but useful
				final Variable vRef = (Variable)associatedObject;
				Variable vRefInKlighRes = (Variable) ((EObject)_vc.getInputModel()).eResource().getEObject(EcoreUtil.getURI(vRef).fragment());
				Collection<EObject> vRefDiagramElements = _vc.getTargetElements(vRefInKlighRes);
				Variable vRefInEngine = (Variable) engineResource.getEObject(EcoreUtil.getURI(vRef).fragment());
				if (ek == EventEnumerator.PRODUCE)
					for(EObject de : vRefDiagramElements ) {
						if (de instanceof KLabel) {
							Integer value = LinguaFrancaRTDAccessor.getcurrentValue(vRefInEngine);
							if(initialLabel == null) {
								initialLabel  = ((KLabel)de).getText();
							}
							((KLabel)de).setText(initialLabel+"("+((value == null) ? "null" : value.toString())+")");
							break label;
						}
					}
			}
		}
		for(Highlighting hl : _associatedHighlighting) {
			hl.apply();
		}
	}

	void finish() {
		for(Highlighting hl : _associatedHighlighting) {
			hl.remove();
		}
	}

	

}
