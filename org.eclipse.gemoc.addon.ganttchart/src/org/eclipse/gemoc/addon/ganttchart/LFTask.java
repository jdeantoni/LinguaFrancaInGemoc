package org.eclipse.gemoc.addon.ganttchart;

import org.jfree.data.gantt.Task;
import org.jfree.data.time.TimePeriod;
import org.jfree.data.xy.XYIntervalDataItem;
import org.lflang.lf.Reaction;

public class LFTask extends XYIntervalDataItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 966233643765751986L;
	public Reaction reaction;

	public LFTask(Reaction associatedReaction, int lineIndex, int start, int duration) {
		super(lineIndex, start, duration);
		reaction = associatedReaction;
	}

}
