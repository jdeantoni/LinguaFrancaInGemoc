package fr.univcotedazur.kairos.languafranca.semantics.k3dsa;

import com.google.common.base.Objects;
import fr.inria.diverse.k3.al.annotationprocessor.Aspect;
import fr.univcotedazur.kairos.languafranca.semantics.k3dsa.DebugLevel;
import fr.univcotedazur.kairos.languafranca.semantics.k3dsa.EventList;
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
  public static void release(final Timer _self) {
    final fr.univcotedazur.kairos.languafranca.semantics.k3dsa.TimerAspectTimerAspectProperties _self_ = fr.univcotedazur.kairos.languafranca.semantics.k3dsa.TimerAspectTimerAspectContext.getSelf(_self);
    // #DispatchPointCut_before# void release()
    if (_self instanceof org.icyphy.linguaFranca.Timer){
    	fr.univcotedazur.kairos.languafranca.semantics.k3dsa.TimerAspect._privk3_release(_self_, (org.icyphy.linguaFranca.Timer)_self);
    };
  }
  
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
  
  protected static void _privk3_release(final TimerAspectTimerAspectProperties _self_, final Timer _self) {
    final Function1<EObject, Boolean> _function = new Function1<EObject, Boolean>() {
      @Override
      public Boolean apply(final EObject eo) {
        return Boolean.valueOf((eo instanceof Model));
      }
    };
    EObject _findFirst = IteratorExtensions.<EObject>findFirst(_self.eResource().getAllContents(), _function);
    Model model = ((Model) _findFirst);
    final int indexOfSelf = ModelAspect.getIndexOfTimer(model, _self);
    if ((indexOfSelf != (-1))) {
      ModelAspect.startedTimers(model).remove(indexOfSelf);
      if ((DebugLevel.level > 0)) {
        EventList _startedTimers = ModelAspect.startedTimers(model);
        String _plus = ("Timer released (" + _startedTimers);
        String _plus_1 = (_plus + ")");
        InputOutput.<String>println(_plus_1);
      }
    } else {
      if ((DebugLevel.level > 0)) {
        EventList _startedTimers_1 = ModelAspect.startedTimers(model);
        String _plus_2 = ("####################################   error ? Timer already released (" + _startedTimers_1);
        String _plus_3 = (_plus_2 + ")");
        InputOutput.<String>println(_plus_3);
      }
    }
  }
  
  protected static void _privk3_schedule(final TimerAspectTimerAspectProperties _self_, final Timer _self) {
    if ((DebugLevel.level > 0)) {
      String _name = _self.getName();
      String _plus = ("enter schedule of " + _name);
      InputOutput.<String>println(_plus);
    }
    final Function1<EObject, Boolean> _function = new Function1<EObject, Boolean>() {
      @Override
      public Boolean apply(final EObject eo) {
        return Boolean.valueOf((eo instanceof Model));
      }
    };
    EObject _findFirst = IteratorExtensions.<EObject>findFirst(_self.eResource().getAllContents(), _function);
    Model model = ((Model) _findFirst);
    final int indexOfSelf = ModelAspect.getIndexOfTimer(model, _self);
    int period = 0;
    Time _time = _self.getPeriod().getTime();
    boolean _tripleNotEquals = (_time != null);
    if (_tripleNotEquals) {
      period = _self.getPeriod().getTime().getInterval();
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
      period = IterableExtensions.<Assignment>findFirst(theInstance.getParameters(), _function_3).getRhs().get(0).getTime().getInterval();
    }
    if ((indexOfSelf != (-1))) {
      if (((ModelAspect.startedTimers(model).get(indexOfSelf).releaseDate).intValue() != period)) {
        StartedAction _get = ModelAspect.startedTimers(model).get(indexOfSelf);
        String _plus_1 = ("->>>>>>>>>>>>>>>>>>>>>>>>>>>>    ERROR ? this timer is already armed for this deadline: " + _get);
        InputOutput.<String>println(_plus_1);
        return;
      }
    }
    ModelAspect.schedule(model, _self, period);
    if ((DebugLevel.level > 0)) {
      String _name_1 = _self.getName();
      String _plus_2 = ("exit schedule of " + _name_1);
      InputOutput.<String>println(_plus_2);
    }
  }
  
  protected static boolean _privk3_canTick(final TimerAspectTimerAspectProperties _self_, final Timer _self) {
    if ((DebugLevel.level > 0)) {
      String _name = _self.getName();
      String _plus = ("Timer " + _name);
      String _plus_1 = (_plus + ".canRelease() ->");
      InputOutput.<String>print(_plus_1);
    }
    final Function1<EObject, Boolean> _function = new Function1<EObject, Boolean>() {
      @Override
      public Boolean apply(final EObject eo) {
        return Boolean.valueOf((eo instanceof Model));
      }
    };
    EObject _findFirst = IteratorExtensions.<EObject>findFirst(_self.eResource().getAllContents(), _function);
    Model model = ((Model) _findFirst);
    final int indexOfSelf = ModelAspect.getIndexOfTimer(model, _self);
    final EventList list = ModelAspect.startedTimers(model);
    boolean result = ((list.get(indexOfSelf).releaseDate).intValue() == 0);
    if ((DebugLevel.level > 0)) {
      InputOutput.<Boolean>println(Boolean.valueOf(result));
    }
    return result;
  }
}
