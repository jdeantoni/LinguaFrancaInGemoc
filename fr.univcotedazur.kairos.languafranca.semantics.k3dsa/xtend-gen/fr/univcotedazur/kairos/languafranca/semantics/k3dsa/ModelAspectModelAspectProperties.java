package fr.univcotedazur.kairos.languafranca.semantics.k3dsa;

import fr.univcotedazur.kairos.languafranca.semantics.k3dsa.StartedAction;
import java.util.ArrayList;

@SuppressWarnings("all")
public class ModelAspectModelAspectProperties {
  public Integer currentTime = Integer.valueOf(0);
  
  public ArrayList<StartedAction> startedTimers = new ArrayList<StartedAction>();
}
