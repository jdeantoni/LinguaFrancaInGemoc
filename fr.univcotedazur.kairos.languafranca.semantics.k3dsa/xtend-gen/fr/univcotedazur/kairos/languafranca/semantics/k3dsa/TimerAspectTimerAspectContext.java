package fr.univcotedazur.kairos.languafranca.semantics.k3dsa;

import fr.univcotedazur.kairos.languafranca.semantics.k3dsa.TimerAspectTimerAspectProperties;
import java.util.Map;
import org.icyphy.linguaFranca.Timer;

@SuppressWarnings("all")
public class TimerAspectTimerAspectContext {
  public static final TimerAspectTimerAspectContext INSTANCE = new TimerAspectTimerAspectContext();
  
  public static TimerAspectTimerAspectProperties getSelf(final Timer _self) {
    		if (!INSTANCE.map.containsKey(_self))
    			INSTANCE.map.put(_self, new fr.univcotedazur.kairos.languafranca.semantics.k3dsa.TimerAspectTimerAspectProperties());
    		return INSTANCE.map.get(_self);
  }
  
  private Map<Timer, TimerAspectTimerAspectProperties> map = new java.util.WeakHashMap<org.icyphy.linguaFranca.Timer, fr.univcotedazur.kairos.languafranca.semantics.k3dsa.TimerAspectTimerAspectProperties>();
  
  public Map<Timer, TimerAspectTimerAspectProperties> getMap() {
    return map;
  }
}
