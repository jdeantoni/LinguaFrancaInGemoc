/*******************************************************************************
 * Copyright (c) 2017 I3S laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     I3S laboratory - initial API and implementation
 *******************************************************************************/
package org.eclipse.gemoc.addon.klighdanimator;

import fr.inria.aoste.timesquare.backend.manager.visible.ClockEntity;
import fr.inria.aoste.timesquare.ccslkernel.model.TimeModel.CCSLModel.ClockExpressionAndRelation.ConcreteEntity;
import fr.inria.aoste.trace.ModelElementReference;

public class GemocClockEntity extends ClockEntity {

	public ConcreteEntity _ce;
	public ModelElementReference _mer;
	
	public GemocClockEntity(ModelElementReference clock) {
		super(clock);
		_mer = clock;
		int size = clock.getElementRef().size();
		_ce = (ConcreteEntity) clock.getElementRef().get(size -1);
		
	}

}
