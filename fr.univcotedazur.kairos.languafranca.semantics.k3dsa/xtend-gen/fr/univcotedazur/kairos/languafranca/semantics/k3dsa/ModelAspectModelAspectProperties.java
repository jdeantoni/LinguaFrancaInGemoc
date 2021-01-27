package fr.univcotedazur.kairos.languafranca.semantics.k3dsa;

import fr.univcotedazur.kairos.languafranca.semantics.k3dsa.StartedAction;
import java.util.LinkedList;

@SuppressWarnings("all")
public class ModelAspectModelAspectProperties {
  public Integer currentTime = Integer.valueOf(0);
  
  public LinkedList<StartedAction> startedTimers = new LinkedList<StartedAction>();
}
