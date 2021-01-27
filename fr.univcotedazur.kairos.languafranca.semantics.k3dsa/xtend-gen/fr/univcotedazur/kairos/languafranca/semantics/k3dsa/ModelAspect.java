package fr.univcotedazur.kairos.languafranca.semantics.k3dsa;

import com.google.common.base.Objects;
import fr.inria.diverse.k3.al.annotationprocessor.Aspect;
import fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspectModelAspectProperties;
import fr.univcotedazur.kairos.languafranca.semantics.k3dsa.StartedAction;
import java.util.LinkedList;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.icyphy.linguaFranca.Model;
import org.icyphy.linguaFranca.Variable;

@Aspect(className = Model.class)
@SuppressWarnings("all")
public class ModelAspect {
  public static void timeJump(final Model _self) {
    final fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspectModelAspectProperties _self_ = fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspectModelAspectContext.getSelf(_self);
    // #DispatchPointCut_before# void timeJump()
    if (_self instanceof org.icyphy.linguaFranca.Model){
    	fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspect._privk3_timeJump(_self_, (org.icyphy.linguaFranca.Model)_self);
    };
  }
  
  public static void schedule(final Model _self, final Variable a, final int duration) {
    final fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspectModelAspectProperties _self_ = fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspectModelAspectContext.getSelf(_self);
    // #DispatchPointCut_before# void schedule(Variable,int)
    if (_self instanceof org.icyphy.linguaFranca.Model){
    	fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspect._privk3_schedule(_self_, (org.icyphy.linguaFranca.Model)_self,a,duration);
    };
  }
  
  /**
   * @Parameters: The parameter element is of the type Action. It specifies the element whose occurrence is needed to be checked in the startedTimer LinkedList.
   * 
   * 	@Return Value: The method returns the index or position of the first occurrence of the element in the list else -1 if the element is not present in the list. The returned value is of integer type.
   */
  public static int getIndexOfTimer(final Model _self, final Variable v) {
    final fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspectModelAspectProperties _self_ = fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspectModelAspectContext.getSelf(_self);
    Object result = null;
    // #DispatchPointCut_before# int getIndexOfTimer(Variable)
    if (_self instanceof org.icyphy.linguaFranca.Model){
    	result = fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspect._privk3_getIndexOfTimer(_self_, (org.icyphy.linguaFranca.Model)_self,v);
    };
    return (int)result;
  }
  
  public static Integer currentTime(final Model _self) {
    final fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspectModelAspectProperties _self_ = fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspectModelAspectContext.getSelf(_self);
    Object result = null;
    // #DispatchPointCut_before# Integer currentTime()
    if (_self instanceof org.icyphy.linguaFranca.Model){
    	result = fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspect._privk3_currentTime(_self_, (org.icyphy.linguaFranca.Model)_self);
    };
    return (java.lang.Integer)result;
  }
  
  public static void currentTime(final Model _self, final Integer currentTime) {
    final fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspectModelAspectProperties _self_ = fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspectModelAspectContext.getSelf(_self);
    // #DispatchPointCut_before# void currentTime(Integer)
    if (_self instanceof org.icyphy.linguaFranca.Model){
    	fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspect._privk3_currentTime(_self_, (org.icyphy.linguaFranca.Model)_self,currentTime);
    };
  }
  
  public static LinkedList<StartedAction> startedTimers(final Model _self) {
    final fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspectModelAspectProperties _self_ = fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspectModelAspectContext.getSelf(_self);
    Object result = null;
    // #DispatchPointCut_before# LinkedList<StartedAction> startedTimers()
    if (_self instanceof org.icyphy.linguaFranca.Model){
    	result = fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspect._privk3_startedTimers(_self_, (org.icyphy.linguaFranca.Model)_self);
    };
    return (java.util.LinkedList<fr.univcotedazur.kairos.languafranca.semantics.k3dsa.StartedAction>)result;
  }
  
  public static void startedTimers(final Model _self, final LinkedList<StartedAction> startedTimers) {
    final fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspectModelAspectProperties _self_ = fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspectModelAspectContext.getSelf(_self);
    // #DispatchPointCut_before# void startedTimers(LinkedList<StartedAction>)
    if (_self instanceof org.icyphy.linguaFranca.Model){
    	fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspect._privk3_startedTimers(_self_, (org.icyphy.linguaFranca.Model)_self,startedTimers);
    };
  }
  
  protected static void _privk3_timeJump(final ModelAspectModelAspectProperties _self_, final Model _self) {
    final int jumpSize = ModelAspect.startedTimers(_self).getFirst().delta;
    ModelAspect.startedTimers(_self).removeFirst();
    Integer _currentTime = ModelAspect.currentTime(_self);
    int _plus = ((_currentTime).intValue() + jumpSize);
    ModelAspect.currentTime(_self, Integer.valueOf(_plus));
    int indexUntilWhichToRemoveElemExcluded = 0;
    while (((indexUntilWhichToRemoveElemExcluded < ModelAspect.startedTimers(_self).size()) && (ModelAspect.startedTimers(_self).get(indexUntilWhichToRemoveElemExcluded).delta == 0))) {
      indexUntilWhichToRemoveElemExcluded++;
    }
    LinkedList<StartedAction> tempList = new LinkedList<StartedAction>();
    for (int i = indexUntilWhichToRemoveElemExcluded; (i < ModelAspect.startedTimers(_self).size()); i++) {
      tempList.add(ModelAspect.startedTimers(_self).get(i));
    }
    ModelAspect.startedTimers(_self, tempList);
    Integer _currentTime_1 = ModelAspect.currentTime(_self);
    String _plus_1 = ("currentTime is now " + _currentTime_1);
    InputOutput.<String>println(_plus_1);
  }
  
  protected static void _privk3_schedule(final ModelAspectModelAspectProperties _self_, final Model _self, final Variable a, final int duration) {
    String _name = a.getName();
    String _plus = ("beforeSchedule: of " + _name);
    String _plus_1 = (_plus + " for ");
    String _plus_2 = (_plus_1 + Integer.valueOf(duration));
    String _plus_3 = (_plus_2 + " --> ");
    LinkedList<StartedAction> _startedTimers = ModelAspect.startedTimers(_self);
    String _plus_4 = (_plus_3 + _startedTimers);
    InputOutput.<String>println(_plus_4);
    int summedDelta = 0;
    boolean _isEmpty = ModelAspect.startedTimers(_self).isEmpty();
    if (_isEmpty) {
      LinkedList<StartedAction> _startedTimers_1 = ModelAspect.startedTimers(_self);
      StartedAction _startedAction = new StartedAction(a, duration);
      _startedTimers_1.add(_startedAction);
      LinkedList<StartedAction> _startedTimers_2 = ModelAspect.startedTimers(_self);
      String _plus_5 = ("afterSchedule: " + _startedTimers_2);
      InputOutput.<String>println(_plus_5);
      return;
    }
    for (int i = 0; (i < ModelAspect.startedTimers(_self).size()); i++) {
      {
        int _summedDelta = summedDelta;
        summedDelta = (_summedDelta + ModelAspect.startedTimers(_self).get(i).delta);
        if ((summedDelta > duration)) {
          final int aDelta = (duration - (summedDelta - ModelAspect.startedTimers(_self).get(i).delta));
          LinkedList<StartedAction> _startedTimers_3 = ModelAspect.startedTimers(_self);
          int _max = Math.max(0, (i - 1));
          StartedAction _startedAction_1 = new StartedAction(a, aDelta);
          _startedTimers_3.add(_max, _startedAction_1);
          for (int j = (i + 1); (j < ModelAspect.startedTimers(_self).size()); j++) {
            if ((ModelAspect.startedTimers(_self).get(j).delta != 0)) {
              int _delta = ModelAspect.startedTimers(_self).get(j).delta;
              ModelAspect.startedTimers(_self).get(j).delta = (_delta - aDelta);
            }
          }
          LinkedList<StartedAction> _startedTimers_4 = ModelAspect.startedTimers(_self);
          String _plus_6 = ("startedTimer (1): " + _startedTimers_4);
          InputOutput.<String>println(_plus_6);
          return;
        }
        int _size = ModelAspect.startedTimers(_self).size();
        int _minus = (_size - 1);
        boolean _equals = (i == _minus);
        if (_equals) {
          final int aDelta_1 = (duration - summedDelta);
          LinkedList<StartedAction> _startedTimers_5 = ModelAspect.startedTimers(_self);
          StartedAction _startedAction_2 = new StartedAction(a, aDelta_1);
          _startedTimers_5.add(_startedAction_2);
          LinkedList<StartedAction> _startedTimers_6 = ModelAspect.startedTimers(_self);
          String _plus_7 = ("startedTimer: (3)" + _startedTimers_6);
          InputOutput.<String>println(_plus_7);
          return;
        }
      }
    }
  }
  
  protected static int _privk3_getIndexOfTimer(final ModelAspectModelAspectProperties _self_, final Model _self, final Variable v) {
    for (int i = 0; (i < ModelAspect.startedTimers(_self).size()); i++) {
      boolean _equals = Objects.equal(ModelAspect.startedTimers(_self).get(i).variable, v);
      if (_equals) {
        return i;
      }
    }
    return (-1);
  }
  
  protected static Integer _privk3_currentTime(final ModelAspectModelAspectProperties _self_, final Model _self) {
    try {
    	for (java.lang.reflect.Method m : _self.getClass().getMethods()) {
    		if (m.getName().equals("getCurrentTime") &&
    			m.getParameterTypes().length == 0) {
    				Object ret = m.invoke(_self);
    				if (ret != null) {
    					return (java.lang.Integer) ret;
    				} else {
    					return null;
    				}
    		}
    	}
    } catch (Exception e) {
    	// Chut !
    }
    return _self_.currentTime;
  }
  
  protected static void _privk3_currentTime(final ModelAspectModelAspectProperties _self_, final Model _self, final Integer currentTime) {
    boolean setterCalled = false;
    try {
    	for (java.lang.reflect.Method m : _self.getClass().getMethods()) {
    		if (m.getName().equals("setCurrentTime")
    				&& m.getParameterTypes().length == 1) {
    			m.invoke(_self, currentTime);
    			setterCalled = true;
    		}
    	}
    } catch (Exception e) {
    	// Chut !
    }
    if (!setterCalled) {
    	_self_.currentTime = currentTime;
    }
  }
  
  protected static LinkedList<StartedAction> _privk3_startedTimers(final ModelAspectModelAspectProperties _self_, final Model _self) {
    try {
    	for (java.lang.reflect.Method m : _self.getClass().getMethods()) {
    		if (m.getName().equals("getStartedTimers") &&
    			m.getParameterTypes().length == 0) {
    				Object ret = m.invoke(_self);
    				if (ret != null) {
    					return (java.util.LinkedList) ret;
    				} else {
    					return null;
    				}
    		}
    	}
    } catch (Exception e) {
    	// Chut !
    }
    return _self_.startedTimers;
  }
  
  protected static void _privk3_startedTimers(final ModelAspectModelAspectProperties _self_, final Model _self, final LinkedList<StartedAction> startedTimers) {
    boolean setterCalled = false;
    try {
    	for (java.lang.reflect.Method m : _self.getClass().getMethods()) {
    		if (m.getName().equals("setStartedTimers")
    				&& m.getParameterTypes().length == 1) {
    			m.invoke(_self, startedTimers);
    			setterCalled = true;
    		}
    	}
    } catch (Exception e) {
    	// Chut !
    }
    if (!setterCalled) {
    	_self_.startedTimers = startedTimers;
    }
  }
}