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

import java.util.Map;

import fr.inria.aoste.timesquare.ccslkernel.model.TimeModel.Clock;
import grph.path.Path;

public class ResultsHolder {

	static public boolean isInUse = false;
	
	static public String[] observedClockNames;
	static public Map<String,Clock> clockNameToClock;
//	static public String physicalClockName = "";
	static public int maxTraceTime = 0;
	static public Path maxPath;
	static public String originalCCSLFilePath;
	
}
