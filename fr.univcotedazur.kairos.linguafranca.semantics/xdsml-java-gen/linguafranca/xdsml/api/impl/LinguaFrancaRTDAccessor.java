/* GENERATED FILE, do not modify manually                                                    *
 * If you need to modify it, copy it first */
package linguafranca.xdsml.api.impl;
import org.eclipse.emf.ecore.EObject;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.lang.reflect.Method;
import org.eclipse.gemoc.execution.concurrent.ccsljavaxdsml.api.extensions.languages.NotInStateSpace;
import org.eclipse.gemoc.executionframework.engine.commons.K3DslHelper;


public class LinguaFrancaRTDAccessor {
  public static java.lang.Integer getcurrentTime(EObject eObject) {
     java.lang.Integer theProperty = (java.lang.Integer)getAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ModelAspect", "currentTime");
		return (theProperty == null) ? null : new java.lang.Integer((java.lang.Integer)  theProperty);
}
	public static boolean setcurrentTime(EObject eObject, java.lang.Integer newValue) {
     java.lang.Integer theValue = newValue == null ? null: new java.lang.Integer(newValue);		return setAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ModelAspect", "currentTime", theValue);
	}
  public static java.lang.Integer getcurrentMicroStep(EObject eObject) {
     java.lang.Integer theProperty = (java.lang.Integer)getAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ModelAspect", "currentMicroStep");
		return (theProperty == null) ? null : new java.lang.Integer((java.lang.Integer)  theProperty);
}
	public static boolean setcurrentMicroStep(EObject eObject, java.lang.Integer newValue) {
     java.lang.Integer theValue = newValue == null ? null: new java.lang.Integer(newValue);		return setAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ModelAspect", "currentMicroStep", theValue);
	}
  public static fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.EventQueue geteventQueue(EObject eObject) {
     fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.EventQueue theProperty = (fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.EventQueue)getAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ModelAspect", "eventQueue");
		return (theProperty == null) ? null : new fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.EventQueue((fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.EventQueue)  theProperty);
}
	public static boolean seteventQueue(EObject eObject, fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.EventQueue newValue) {
     fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.EventQueue theValue = newValue == null ? null: new fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.EventQueue(newValue);		return setAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ModelAspect", "eventQueue", theValue);
	}
  public static java.lang.Integer getnextSchedule(EObject eObject) {
     java.lang.Integer theProperty = (java.lang.Integer)getAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ActionAspect", "nextSchedule");
		return (theProperty == null) ? null : new java.lang.Integer((java.lang.Integer)  theProperty);
}
	public static boolean setnextSchedule(EObject eObject, java.lang.Integer newValue) {
     java.lang.Integer theValue = newValue == null ? null: new java.lang.Integer(newValue);		return setAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ActionAspect", "nextSchedule", theValue);
	}
  public static java.util.LinkedList getactionBufferedValues(EObject eObject) {
     java.util.LinkedList theProperty = (java.util.LinkedList)getAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ActionAspect", "actionBufferedValues");
		return (theProperty == null) ? null : new java.util.LinkedList((java.util.LinkedList)  theProperty);
}
	public static boolean setactionBufferedValues(EObject eObject, java.util.LinkedList newValue) {
     java.util.LinkedList theValue = newValue == null ? null: new java.util.LinkedList(newValue);		return setAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ActionAspect", "actionBufferedValues", theValue);
	}
  public static java.lang.Boolean getoffsetToDo(EObject eObject) {
     java.lang.Boolean theProperty = (java.lang.Boolean)getAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.TimerAspect", "offsetToDo");
		return (theProperty == null) ? null : new java.lang.Boolean((java.lang.Boolean)  theProperty);
}
	public static boolean setoffsetToDo(EObject eObject, java.lang.Boolean newValue) {
     java.lang.Boolean theValue = newValue == null ? null: new java.lang.Boolean(newValue);		return setAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.TimerAspect", "offsetToDo", theValue);
	}
  public static java.util.LinkedList getbufferedValues(EObject eObject) {
     java.util.LinkedList theProperty = (java.util.LinkedList)getAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ConnectionAspect", "bufferedValues");
		return (theProperty == null) ? null : new java.util.LinkedList((java.util.LinkedList)  theProperty);
}
	public static boolean setbufferedValues(EObject eObject, java.util.LinkedList newValue) {
     java.util.LinkedList theValue = newValue == null ? null: new java.util.LinkedList(newValue);		return setAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ConnectionAspect", "bufferedValues", theValue);
	}
  public static java.lang.Integer getcurrentValue(EObject eObject) {
     java.lang.Integer theProperty = (java.lang.Integer)getAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.VariableAspect", "currentValue");
		return (theProperty == null) ? null : new java.lang.Integer((java.lang.Integer)  theProperty);
}
	public static boolean setcurrentValue(EObject eObject, java.lang.Integer newValue) {
     java.lang.Integer theValue = newValue == null ? null: new java.lang.Integer(newValue);		return setAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.VariableAspect", "currentValue", theValue);
	}
  public static java.lang.Integer getcurrentStateValue(EObject eObject) {
     java.lang.Integer theProperty = (java.lang.Integer)getAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.StateVarAspect", "currentStateValue");
		return (theProperty == null) ? null : new java.lang.Integer((java.lang.Integer)  theProperty);
}
	public static boolean setcurrentStateValue(EObject eObject, java.lang.Integer newValue) {
     java.lang.Integer theValue = newValue == null ? null: new java.lang.Integer(newValue);		return setAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.StateVarAspect", "currentStateValue", theValue);
	}

	public static Object getAspectProperty(EObject eObject, String languageName, String aspectName, String propertyName) {
		List<Class<?>> aspects = K3DslHelper.getAspectsOn(languageName, eObject.getClass());
		Class<?> aspect = null;
		for (Class<?> a : aspects) {
			try {
				if (Class.forName(aspectName).isAssignableFrom(a)) {
					aspect = a;
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (aspect == null) {
			return null;
		}
		Object res = null;
		 try {
			res = aspect.getDeclaredMethod(propertyName, ((fr.inria.diverse.k3.al.annotationprocessor.Aspect)aspect.getAnnotations()[0]).className()).invoke(eObject, eObject);
			return res;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	
public static boolean setAspectProperty(EObject eObject, String languageName, String aspectName, String propertyName, Object newValue) {
		List<Class<?>> aspects = K3DslHelper.getAspectsOn(languageName, eObject.getClass());
		Class<?> aspect = null;
		for (Class<?> a : aspects) {
			try {
				if (Class.forName(aspectName).isAssignableFrom(a)) {
					aspect = a;
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return false;
			}
		}
		if (aspect == null) {
			return false;
		}
		 Method m = getSetter(propertyName,newValue,aspect);
		 try {
			m.invoke(eObject, eObject, newValue);
			return true;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}			
		return false;
}
	
	private static Method getSetter(String propertyName, Object value, Class<?> aspect) {
		Method setter = null;
		try {
			if(value != null) {
				setter = aspect.getMethod(propertyName, ((fr.inria.diverse.k3.al.annotationprocessor.Aspect)aspect.getAnnotations()[0]).className(), value.getClass());
			}else {
				for (Method m : aspect.getMethods()) {
					if (m.getName().compareTo(propertyName) ==0 && m.getParameterCount() == 2) {
						setter= m;
						return setter;
					}
				}
				throw new NoSuchMethodException();
			}
			return setter;
		} catch (NoSuchMethodException | SecurityException | IllegalArgumentException e) {
			
				for(Class<?> c : ((fr.inria.diverse.k3.al.annotationprocessor.Aspect)aspect.getAnnotations()[0]).getClass().getInterfaces()) {
					try {
					if(value != null) {
						setter = aspect.getMethod(propertyName, c, value.getClass());
						return setter;
					}else {
						for (Method m : aspect.getMethods()) {
							if (m.getName().compareTo(propertyName) ==0 && m.getParameterCount() == 2) {
								setter= m;
								return setter;
							}
						}
					}
					} catch (NoSuchMethodException | SecurityException | IllegalArgumentException e1) {
					}
					if (setter == null) {
						throw new RuntimeException("not method found for "+value.getClass().getName()+"::set"+propertyName);
					}
				}
			}
		return setter;
	}};