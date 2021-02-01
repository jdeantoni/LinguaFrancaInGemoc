package fr.univcotedazur.kairos.languafranca.semantics.k3dsa;

import fr.univcotedazur.kairos.languafranca.semantics.k3dsa.StartedAction;
import java.util.ArrayList;

@SuppressWarnings("all")
public class EventList extends ArrayList<StartedAction> {
  public EventList(final EventList el) {
    super(el);
  }
  
  public EventList() {
    super();
  }
  
  @Override
  public boolean equals(final Object o) {
    if ((o instanceof EventList)) {
      return this.equals(((EventList) o));
    }
    return false;
  }
  
  public boolean equals(final EventList o) {
    return (((this.size() == o.size()) && this.containsAll(o)) && o.containsAll(this));
  }
}
