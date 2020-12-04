package fr.univcotedazur.kairos.languafranca.semantics.k3dsa;

import com.google.common.base.Objects;
import fr.inria.diverse.k3.al.annotationprocessor.Aspect;
import fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspect;
import fr.univcotedazur.kairos.languafranca.semantics.k3dsa.StartedAction;
import fr.univcotedazur.kairos.languafranca.semantics.k3dsa.TimerAspectTimerAspectProperties;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;
import org.icyphy.linguaFranca.Assignment;
import org.icyphy.linguaFranca.Instantiation;
import org.icyphy.linguaFranca.Model;
import org.icyphy.linguaFranca.Reactor;
import org.icyphy.linguaFranca.Time;
import org.icyphy.linguaFranca.Timer;

@Aspect(className = Timer.class)
@SuppressWarnings("all")
public class TimerAspect {
  public static void schedule(final Timer _self) {
    final fr.univcotedazur.kairos.languafranca.semantics.k3dsa.TimerAspectTimerAspectProperties _self_ = fr.univcotedazur.kairos.languafranca.semantics.k3dsa.TimerAspectTimerAspectContext.getSelf(_self);
    // #DispatchPointCut_before# void schedule()
    if (_self instanceof org.icyphy.linguaFranca.Timer){
    	fr.univcotedazur.kairos.languafranca.semantics.k3dsa.TimerAspect._privk3_schedule(_self_, (org.icyphy.linguaFranca.Timer)_self);
    };
  }
  
  public static boolean canTick(final Timer _self) {
    final fr.univcotedazur.kairos.languafranca.semantics.k3dsa.TimerAspectTimerAspectProperties _self_ = fr.univcotedazur.kairos.languafranca.semantics.k3dsa.TimerAspectTimerAspectContext.getSelf(_self);
    Object result = null;
    // #DispatchPointCut_before# boolean canTick()
    if (_self instanceof org.icyphy.linguaFranca.Timer){
    	result = fr.univcotedazur.kairos.languafranca.semantics.k3dsa.TimerAspect._privk3_canTick(_self_, (org.icyphy.linguaFranca.Timer)_self);
    };
    return (boolean)result;
  }
  
  protected static void _privk3_schedule(final TimerAspectTimerAspectProperties _self_, final Timer _self) {
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
    Time _time = _self.getPeriod().getTime();
    boolean _tripleNotEquals = (_time != null);
    if (_tripleNotEquals) {
      ModelAspect.schedule(model, _self, _self.getPeriod().getTime().getInterval());
    } else {
      final Function1<Reactor, Boolean> _function_1 = new Function1<Reactor, Boolean>() {
        @Override
        public Boolean apply(final Reactor r) {
          boolean _isMain = r.isMain();
          return Boolean.valueOf((_isMain == true));
        }
      };
      final Function1<Instantiation, Boolean> _function_2 = new Function1<Instantiation, Boolean>() {
        @Override
        public Boolean apply(final Instantiation i) {
          String _name = i.getReactorClass().getName();
          EObject _eContainer = _self.eContainer();
          String _name_1 = ((Reactor) _eContainer).getName();
          return Boolean.valueOf(Objects.equal(_name, _name_1));
        }
      };
      Instantiation theInstance = IterableExtensions.<Instantiation>findFirst(IterableExtensions.<Reactor>findFirst(model.getReactors(), _function_1).getInstantiations(), _function_2);
      final Function1<Assignment, Boolean> _function_3 = new Function1<Assignment, Boolean>() {
        @Override
        public Boolean apply(final Assignment p) {
          String _name = p.getLhs().getName();
          String _name_1 = _self.getPeriod().getParameter().getName();
          return Boolean.valueOf(Objects.equal(_name, _name_1));
        }
      };
      int period = IterableExtensions.<Assignment>findFirst(theInstance.getParameters(), _function_3).getRhs().get(0).getTime().getInterval();
      ModelAspect.schedule(model, _self, period);
    }
    String _name_1 = _self.getName();
    String _plus_1 = ("exit schedule of " + _name_1);
    InputOutput.<String>println(_plus_1);
  }
  
  protected static boolean _privk3_canTick(final TimerAspectTimerAspectProperties _self_, final Timer _self) {
    String _name = _self.getName();
    String _plus = (_name + ".canTick() ->");
    InputOutput.<String>print(_plus);
    final Function1<EObject, Boolean> _function = new Function1<EObject, Boolean>() {
      @Override
      public Boolean apply(final EObject eo) {
        return Boolean.valueOf((eo instanceof Model));
      }
    };
    EObject _findFirst = IteratorExtensions.<EObject>findFirst(_self.eResource().getAllContents(), _function);
    Model model = ((Model) _findFirst);
    final int indexOfSelf = ModelAspect.getIndexOfTimer(model, _self);
    boolean result = false;
    boolean _matched = false;
    if (Objects.equal(indexOfSelf, (-1))) {
      _matched=true;
      throw new RuntimeException("ERROR: TimerAspect::canTick() -> timer is not started !");
    }
    if (!_matched) {
      if (Objects.equal(indexOfSelf, 0)) {
        _matched=true;
        result = true;
      }
    }
    if (!_matched) {
      if ((indexOfSelf > 0)) {
        _matched=true;
        result = ((ModelAspect.startedTimer(model).get(indexOfSelf).delta == 0) && IterableExtensions.<StartedAction>forall(ModelAspect.startedTimer(model).subList(1, indexOfSelf), new Function1<StartedAction, Boolean>() {
          @Override
          public Boolean apply(final StartedAction e) {
            return Boolean.valueOf((e.delta == 0));
          }
        }));
      }
    }
    InputOutput.<Boolean>println(Boolean.valueOf(result));
    return result;
  }
}
