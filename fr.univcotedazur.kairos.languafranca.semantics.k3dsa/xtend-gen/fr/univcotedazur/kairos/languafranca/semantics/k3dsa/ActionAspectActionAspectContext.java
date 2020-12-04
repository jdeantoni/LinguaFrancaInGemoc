package fr.univcotedazur.kairos.languafranca.semantics.k3dsa;

import fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ActionAspectActionAspectProperties;
import java.util.Map;
import org.icyphy.linguaFranca.Action;

@SuppressWarnings("all")
public class ActionAspectActionAspectContext {
  public static final ActionAspectActionAspectContext INSTANCE = new ActionAspectActionAspectContext();
  
  public static ActionAspectActionAspectProperties getSelf(final Action _self) {
    		if (!INSTANCE.map.containsKey(_self))
    			INSTANCE.map.put(_self, new fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ActionAspectActionAspectProperties());
    		return INSTANCE.map.get(_self);
  }
  
  private Map<Action, ActionAspectActionAspectProperties> map = new java.util.WeakHashMap<org.icyphy.linguaFranca.Action, fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ActionAspectActionAspectProperties>();
  
  public Map<Action, ActionAspectActionAspectProperties> getMap() {
    return map;
  }
}
