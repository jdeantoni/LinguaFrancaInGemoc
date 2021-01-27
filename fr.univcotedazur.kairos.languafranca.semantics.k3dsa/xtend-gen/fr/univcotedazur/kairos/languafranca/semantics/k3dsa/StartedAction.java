package fr.univcotedazur.kairos.languafranca.semantics.k3dsa;

import org.icyphy.linguaFranca.Variable;

@SuppressWarnings("all")
public class StartedAction {
  public Integer releaseDate;
  
  public final Variable variable;
  
  public StartedAction(final Variable v, final int d) {
    this.releaseDate = Integer.valueOf(d);
    this.variable = v;
  }
  
  @Override
  public String toString() {
    String _name = this.variable.getName();
    String _plus = ("(" + _name);
    String _plus_1 = (_plus + "@");
    String _plus_2 = (_plus_1 + this.releaseDate);
    return (_plus_2 + ")");
  }
}
