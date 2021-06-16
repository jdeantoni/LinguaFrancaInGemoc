/*******************************************************************************
 * Copyright (c) 2021 Universite Cote d'Azur and others.
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

import java.util.List;
import java.util.Set;

/**
 *
 */
public class Transition {
    long logicalTime;
    long microStep;
    private List<String> triggers;
    private Transition nextTransition;
    private Transition previousTransition;
    private boolean isTimeAdvancement;
    private Set<String> pendingEffects;


    //Here for the next version...
    public Set<String> getPendingEffects() {
        return pendingEffects;
    }

    public void setPendingEffects(Set<String> pendingEffects) {
        this.pendingEffects = pendingEffects;
    }

    public Transition(long logicalTime, long microStep, List<String> triggers, Transition nextTransition,
                      Transition previousTransition, boolean isTimeAdvancement) {
        this.logicalTime = logicalTime;
        this.microStep = microStep;
        this.triggers = triggers;
        this.nextTransition = nextTransition;
        this.previousTransition = previousTransition;
        this.isTimeAdvancement = isTimeAdvancement;

    }

    public long getLogicalTime() {
        return logicalTime;
    }

    public long getMicroStep() {
        return microStep;
    }

    public List<String> getTriggers() {
        return triggers;
    }

    public Transition getNextTransition() {
        return nextTransition;
    }

    public Transition getPreviousTransition() {
        return previousTransition;
    }

    public boolean isTimeAdvancement() {
        return isTimeAdvancement;
    }

    public void setLogicalTime(long logicalTime) {
        this.logicalTime = logicalTime;
    }

    public void setMicroStep(long microStep) {
        this.microStep = microStep;
    }

    public void setTriggers(List<String> triggers) {
        this.triggers = triggers;
    }

    public void setNextTransition(Transition nextTransition) {
        this.nextTransition = nextTransition;
    }

    public void setPreviousTransition(Transition previousTransition) {
        this.previousTransition = previousTransition;
    }

    public void setTimeAdvancement(boolean timeAdvancement) {
        isTimeAdvancement = timeAdvancement;
    }
}
