package fr.univcotedazur.kairos.languafranca.semantics.k3dsa;

import fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspectModelAspectProperties;
import java.util.Map;
import org.icyphy.linguaFranca.Model;

@SuppressWarnings("all")
public class ModelAspectModelAspectContext {
  public static final ModelAspectModelAspectContext INSTANCE = new ModelAspectModelAspectContext();
  
  public static ModelAspectModelAspectProperties getSelf(final Model _self) {
    		if (!INSTANCE.map.containsKey(_self))
    			INSTANCE.map.put(_self, new fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspectModelAspectProperties());
    		return INSTANCE.map.get(_self);
  }
  
  private Map<Model, ModelAspectModelAspectProperties> map = new java.util.WeakHashMap<org.icyphy.linguaFranca.Model, fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspectModelAspectProperties>();
  
  public Map<Model, ModelAspectModelAspectProperties> getMap() {
    return map;
  }
}
