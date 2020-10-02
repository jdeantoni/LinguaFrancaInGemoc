/**
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://rtsys.informatik.uni-kiel.de/kieler
 * 
 * Copyright 2017 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * @authors: Kielers,
 * modified by Julien Deantoni on October 2020
 */
package org.eclipse.gemoc.addon.klighdanimator


import de.cau.cs.kieler.klighd.kgraph.KLabeledGraphElement
import de.cau.cs.kieler.klighd.krendering.KContainerRendering
import de.cau.cs.kieler.klighd.krendering.KForeground
import de.cau.cs.kieler.klighd.krendering.KStyle
import de.cau.cs.kieler.klighd.krendering.KText
import java.util.List
import org.eclipse.elk.graph.properties.Property
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.util.EcoreUtil
import de.cau.cs.kieler.klighd.kgraph.KGraphElement
import de.cau.cs.kieler.klighd.kgraph.KNode

/**
 * @author aas
 *
 */
class Highlighting {
    /**
     * A property to mark that a style is used for highlighting.
     */
    protected val HIGHLIGHTING_MARKER = new Property<Object>("highlighting");
    
    public EObject element
    public KForeground foreground
    public EObject eObject
    
    /**
     * Constructor
     * 
     */
    new(EObject element, KForeground foreground) {
        this.element = element
        this.foreground = foreground
    }
    
    /**
     * Constructor
     * 
     */
    new(EObject element, KForeground foreground, EObject eObject) {
        this.element = element
        this.foreground = foreground
        this.eObject = eObject
    }
    
    /**
     * Highlights the element with the foreground.
     */
    public def void apply() {
        // Remember that this style is to highlight the diagram.
        // This is used to filter for highlighting styles when they should be removed.
        foreground.setProperty(HIGHLIGHTING_MARKER, this)
        // Highlight container of this element
        if (element instanceof KGraphElement){
        	val ren = (element as KGraphElement).getData(typeof(KContainerRendering))
        	if (ren !== null) {
        		ren.styles.add(EcoreUtil.copy(foreground))
        	}
        }else{
        	(element as KContainerRendering).styles.add(EcoreUtil.copy(foreground))
        }
        
        // Highlight label of this element
        if (element instanceof KLabeledGraphElement && !(element as KLabeledGraphElement).labels.isNullOrEmpty) {
            val label = (element as KLabeledGraphElement).labels.get(0)
            val ren2 = label.getData(typeof(KText))
            if(ren2 !== null) {
                ren2.styles.add(EcoreUtil.copy(foreground))    
            }
        }
    }
    
    /**
     * Removes the highlighting.
     */
    public def void remove() {
        // Remove highlighting from container of this element
        if (element instanceof KGraphElement){
        	val ren = (element as KGraphElement).getData(typeof(KContainerRendering))
        	if (ren != null) removeHighlighting(ren.styles)
        }else{
        	removeHighlighting((element as KContainerRendering).styles)
        }
        // Remove highlighting from label of this element
        if (element instanceof KLabeledGraphElement && !(element as KLabeledGraphElement).labels.isNullOrEmpty) {
            val label = (element as KLabeledGraphElement).labels.get(0)
            val ren2 = label.getData(typeof(KText));
            if(ren2 !== null) {
                removeHighlighting(ren2.styles)    
            }
        }
    }
    
    /**
     * Removes all highlighting styles from the list.
     */
    protected def void removeHighlighting(List<KStyle> styles) {
        for(s : styles.clone) {
            if(s.isHighlighting) {
                styles.remove(s)    
            }
        }
    }
    
    /**
     * Checks if the given style has been used to highlight the diagram. 
     */
    protected def boolean isHighlighting(KStyle style) {
        return style.getProperty(HIGHLIGHTING_MARKER) == this
    }
}
