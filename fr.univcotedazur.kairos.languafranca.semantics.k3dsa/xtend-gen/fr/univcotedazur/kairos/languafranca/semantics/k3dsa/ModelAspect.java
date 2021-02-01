package fr.univcotedazur.kairos.languafranca.semantics.k3dsa;

import com.google.common.base.Objects;
import fr.inria.diverse.k3.al.annotationprocessor.Aspect;
import fr.univcotedazur.kairos.languafranca.semantics.k3dsa.DebugLevel;
import fr.univcotedazur.kairos.languafranca.semantics.k3dsa.EventList;
import fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspectModelAspectProperties;
import fr.univcotedazur.kairos.languafranca.semantics.k3dsa.StartedAction;
import org.eclipse.gemoc.execution.concurrent.ccsljavaxdsml.api.extensions.languages.NotInStateSpace;
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
  
  @NotInStateSpace
  public static Integer currentTime(final Model _self) {
    final fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspectModelAspectProperties _self_ = fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspectModelAspectContext.getSelf(_self);
    Object result = null;
    // #DispatchPointCut_before# Integer currentTime()
    if (_self instanceof org.icyphy.linguaFranca.Model){
    	result = fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspect._privk3_currentTime(_self_, (org.icyphy.linguaFranca.Model)_self);
    };
    return (java.lang.Integer)result;
  }
  
  @NotInStateSpace
  public static void currentTime(final Model _self, final Integer currentTime) {
    final fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspectModelAspectProperties _self_ = fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspectModelAspectContext.getSelf(_self);
    // #DispatchPointCut_before# void currentTime(Integer)
    if (_self instanceof org.icyphy.linguaFranca.Model){
    	fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspect._privk3_currentTime(_self_, (org.icyphy.linguaFranca.Model)_self,currentTime);
    };
  }
  
  public static EventList startedTimers(final Model _self) {
    final fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspectModelAspectProperties _self_ = fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspectModelAspectContext.getSelf(_self);
    Object result = null;
    // #DispatchPointCut_before# EventList startedTimers()
    if (_self instanceof org.icyphy.linguaFranca.Model){
    	result = fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspect._privk3_startedTimers(_self_, (org.icyphy.linguaFranca.Model)_self);
    };
    return (fr.univcotedazur.kairos.languafranca.semantics.k3dsa.EventList)result;
  }
  
  public static void startedTimers(final Model _self, final EventList startedTimers) {
    final fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspectModelAspectProperties _self_ = fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspectModelAspectContext.getSelf(_self);
    // #DispatchPointCut_before# void startedTimers(EventList)
    if (_self instanceof org.icyphy.linguaFranca.Model){
    	fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspect._privk3_startedTimers(_self_, (org.icyphy.linguaFranca.Model)_self,startedTimers);
    };
  }
  
  protected static void _privk3_timeJump(final ModelAspectModelAspectProperties _self_, final Model _self) {
    int _size = ModelAspect.startedTimers(_self).size();
    boolean _equals = (_size == 0);
    if (_equals) {
      InputOutput.<String>println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~  ERROR ? not time Jump to do (no timer armed)");
      return;
    }
    Integer jumpSize = ModelAspect.startedTimers(_self).get(0).releaseDate;
    Integer _currentTime = ModelAspect.currentTime(_self);
    int _plus = ((jumpSize).intValue() + (_currentTime).intValue());
    ModelAspect.currentTime(_self, Integer.valueOf(_plus));
    EventList _startedTimers = ModelAspect.startedTimers(_self);
    for (final StartedAction sa : _startedTimers) {
      sa.releaseDate = Integer.valueOf(((sa.releaseDate).intValue() - (jumpSize).intValue()));
    }
    if ((DebugLevel.level > 0)) {
      Integer _currentTime_1 = ModelAspect.currentTime(_self);
      String _plus_1 = ("currentTime is now " + _currentTime_1);
      InputOutput.<String>println(_plus_1);
    }
  }
  
  protected static void _privk3_schedule(final ModelAspectModelAspectProperties _self_, final Model _self, final Variable a, final int duration) {
    if ((DebugLevel.level > 0)) {
      String _name = a.getName();
      String _plus = ("beforeSchedule: of " + _name);
      String _plus_1 = (_plus + " for ");
      String _plus_2 = (_plus_1 + Integer.valueOf(duration));
      String _plus_3 = (_plus_2 + " --> ");
      EventList _startedTimers = ModelAspect.startedTimers(_self);
      String _plus_4 = (_plus_3 + _startedTimers);
      InputOutput.<String>println(_plus_4);
    }
    boolean _isEmpty = ModelAspect.startedTimers(_self).isEmpty();
    if (_isEmpty) {
      EventList _startedTimers_1 = ModelAspect.startedTimers(_self);
      StartedAction _startedAction = new StartedAction(a, duration);
      _startedTimers_1.add(_startedAction);
      if ((DebugLevel.level > 0)) {
        EventList _startedTimers_2 = ModelAspect.startedTimers(_self);
        String _plus_5 = ("afterSchedule: " + _startedTimers_2);
        InputOutput.<String>println(_plus_5);
      }
      return;
    }
    for (int i = 0; (i < ModelAspect.startedTimers(_self).size()); i++) {
      {
        if (((ModelAspect.startedTimers(_self).get(i).releaseDate).intValue() > duration)) {
          EventList _startedTimers_3 = ModelAspect.startedTimers(_self);
          int _max = Math.max(0, (i - 1));
          StartedAction _startedAction_1 = new StartedAction(a, duration);
          _startedTimers_3.add(_max, _startedAction_1);
          if ((DebugLevel.level > 0)) {
            EventList _startedTimers_4 = ModelAspect.startedTimers(_self);
            String _plus_6 = ("startedTimer (1): " + _startedTimers_4);
            InputOutput.<String>println(_plus_6);
          }
          return;
        }
        int _size = ModelAspect.startedTimers(_self).size();
        int _minus = (_size - 1);
        boolean _equals = (i == _minus);
        if (_equals) {
          EventList _startedTimers_5 = ModelAspect.startedTimers(_self);
          StartedAction _startedAction_2 = new StartedAction(a, duration);
          _startedTimers_5.add(_startedAction_2);
          if ((DebugLevel.level > 0)) {
            EventList _startedTimers_6 = ModelAspect.startedTimers(_self);
            String _plus_7 = ("startedTimer: (3)" + _startedTimers_6);
            InputOutput.<String>println(_plus_7);
          }
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
  
  protected static EventList _privk3_startedTimers(final ModelAspectModelAspectProperties _self_, final Model _self) {
    try {
    	for (java.lang.reflect.Method m : _self.getClass().getMethods()) {
    		if (m.getName().equals("getStartedTimers") &&
    			m.getParameterTypes().length == 0) {
    				Object ret = m.invoke(_self);
    				if (ret != null) {
    					return (fr.univcotedazur.kairos.languafranca.semantics.k3dsa.EventList) ret;
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
  
  protected static void _privk3_startedTimers(final ModelAspectModelAspectProperties _self_, final Model _self, final EventList startedTimers) {
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
