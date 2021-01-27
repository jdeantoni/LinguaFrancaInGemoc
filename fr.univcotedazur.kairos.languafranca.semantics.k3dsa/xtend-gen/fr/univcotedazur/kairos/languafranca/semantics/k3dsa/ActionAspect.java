package fr.univcotedazur.kairos.languafranca.semantics.k3dsa;

import com.google.common.base.Objects;
import fr.inria.diverse.k3.al.annotationprocessor.Aspect;
import fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ActionAspectActionAspectProperties;
import fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspect;
import fr.univcotedazur.kairos.languafranca.semantics.k3dsa.StartedAction;
import java.util.LinkedList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;
import org.icyphy.linguaFranca.Action;
import org.icyphy.linguaFranca.Model;

@Aspect(className = Action.class)
@SuppressWarnings("all")
public class ActionAspect {
  public static void release(final Action _self) {
    final fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ActionAspectActionAspectProperties _self_ = fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ActionAspectActionAspectContext.getSelf(_self);
    // #DispatchPointCut_before# void release()
    if (_self instanceof org.icyphy.linguaFranca.Action){
    	fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ActionAspect._privk3_release(_self_, (org.icyphy.linguaFranca.Action)_self);
    };
  }
  
  public static void schedule(final Action _self) {
    final fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ActionAspectActionAspectProperties _self_ = fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ActionAspectActionAspectContext.getSelf(_self);
    // #DispatchPointCut_before# void schedule()
    if (_self instanceof org.icyphy.linguaFranca.Action){
    	fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ActionAspect._privk3_schedule(_self_, (org.icyphy.linguaFranca.Action)_self);
    };
  }
  
  public static boolean canTick(final Action _self) {
    final fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ActionAspectActionAspectProperties _self_ = fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ActionAspectActionAspectContext.getSelf(_self);
    Object result = null;
    // #DispatchPointCut_before# boolean canTick()
    if (_self instanceof org.icyphy.linguaFranca.Action){
    	result = fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ActionAspect._privk3_canTick(_self_, (org.icyphy.linguaFranca.Action)_self);
    };
    return (boolean)result;
  }
  
  protected static void _privk3_release(final ActionAspectActionAspectProperties _self_, final Action _self) {
    final Function1<EObject, Boolean> _function = new Function1<EObject, Boolean>() {
      @Override
      public Boolean apply(final EObject eo) {
        return Boolean.valueOf((eo instanceof Model));
      }
    };
    EObject _findFirst = IteratorExtensions.<EObject>findFirst(_self.eResource().getAllContents(), _function);
    Model model = ((Model) _findFirst);
    final int indexOfSelf = ModelAspect.getIndexOfTimer(model, _self);
    ModelAspect.startedTimers(model).remove(indexOfSelf);
    LinkedList<StartedAction> _startedTimers = ModelAspect.startedTimers(model);
    String _plus = ("Action released (" + _startedTimers);
    String _plus_1 = (_plus + ")");
    InputOutput.<String>println(_plus_1);
  }
  
  protected static void _privk3_schedule(final ActionAspectActionAspectProperties _self_, final Action _self) {
    if (((_self.getMinDelay() == null) || (_self.getMinDelay().getTime() == null))) {
      return;
    }
    String _name = _self.getName();
    String _plus = ("enter schedule of " + _name);
    InputOutput.<String>println(_plus);
    final Function1<EObject, Boolean> _function = new Function1<EObject, Boolean>() {
      @Override
      public Boolean apply(final EObject eo) {
        return Boolean.valueOf((eo instanceof Model));
      }
    };
    EObject _findFirst = IteratorExtensions.<EObject>findFirst(_self.eResource().getAllContents(), _function);
    Model model = ((Model) _findFirst);
    ModelAspect.schedule(model, _self, _self.getMinDelay().getTime().getInterval());
    String _name_1 = _self.getName();
    String _plus_1 = ("exit schedule of " + _name_1);
    InputOutput.<String>println(_plus_1);
  }
  
  protected static boolean _privk3_canTick(final ActionAspectActionAspectProperties _self_, final Action _self) {
    String _name = _self.getName();
    String _plus = ("Action " + _name);
    String _plus_1 = (_plus + ".canRelease() ->");
    InputOutput.<String>print(_plus_1);
    final Function1<EObject, Boolean> _function = new Function1<EObject, Boolean>() {
      @Override
      public Boolean apply(final EObject eo) {
        return Boolean.valueOf((eo instanceof Model));
      }
    };
    EObject _findFirst = IteratorExtensions.<EObject>findFirst(_self.eResource().getAllContents(), _function);
    Model model = ((Model) _findFirst);
    final int indexOfSelf = ModelAspect.getIndexOfTimer(model, _self);
    final LinkedList<StartedAction> list = ModelAspect.startedTimers(model);
    Integer _currentTime = ModelAspect.currentTime(model);
    boolean result = Objects.equal(_currentTime, list.get(indexOfSelf).releaseDate);
    InputOutput.<Boolean>println(Boolean.valueOf(result));
    return result;
  }
}
