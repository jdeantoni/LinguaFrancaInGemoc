/* GENERATED FILE, do not modify manually                                                    *
 * If you need to modify it, copy it first */

import org.eclipse.emf.ecore.EObject;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.lang.reflect.Method;
import org.eclipse.gemoc.execution.concurrent.ccsljavaxdsml.api.extensions.languages.NotInStateSpace;
import org.eclipse.gemoc.executionframework.engine.commons.K3DslHelper;


public class LinguaFrancaRTDAccessor {
  public static java.lang.Integer getcurrentTime(EObject eObject) {
		return new java.lang.Integer((java.lang.Integer)  getAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ModelAspect", "currentTime"));
	}
	public static boolean setcurrentTime(EObject eObject, java.lang.Integer newValue) {
		return setAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ModelAspect", "currentTime", new java.lang.Integer(newValue));
	}
  public static java.lang.Integer getcurrentMicroStep(EObject eObject) {
		return new java.lang.Integer((java.lang.Integer)  getAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ModelAspect", "currentMicroStep"));
	}
	public static boolean setcurrentMicroStep(EObject eObject, java.lang.Integer newValue) {
		return setAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ModelAspect", "currentMicroStep", new java.lang.Integer(newValue));
	}
  public static fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.EventList getstartedTimers(EObject eObject) {
		return new fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.EventList((fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.EventList)  getAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ModelAspect", "startedTimers"));
	}
	public static boolean setstartedTimers(EObject eObject, fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.EventList newValue) {
		return setAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ModelAspect", "startedTimers", new fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.EventList(newValue));
	}
  public static java.lang.Integer getnextSchedule(EObject eObject) {
		return new java.lang.Integer((java.lang.Integer)  getAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ActionAspect", "nextSchedule"));
	}
	public static boolean setnextSchedule(EObject eObject, java.lang.Integer newValue) {
		return setAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ActionAspect", "nextSchedule", new java.lang.Integer(newValue));
	}
  public static java.lang.Boolean getoffsetToDo(EObject eObject) {
		return new java.lang.Boolean((java.lang.Boolean)  getAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.TimerAspect", "offsetToDo"));
	}
	public static boolean setoffsetToDo(EObject eObject, java.lang.Boolean newValue) {
		return setAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.TimerAspect", "offsetToDo", new java.lang.Boolean(newValue));
	}
  public static java.lang.Integer getcurrentValue(EObject eObject) {
	  	Object theProperty = getAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.VariableAspect", "currentValue");
		return (theProperty == null) ? null : new java.lang.Integer((java.lang.Integer) theProperty );
	}
	public static boolean setcurrentValue(EObject eObject, java.lang.Integer newValue) {
		int theValue = newValue == null ? null: new java.lang.Integer(newValue);
		return setAspectProperty(eObject, "fr.univcotedazur.kairos.linguafranca.semantics.LinguaFranca", "fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.VariableAspect", "currentValue", theValue);
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
			if (res != null) {
				return res;
			}else {
				return null;
			}
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
			 try {
				 aspect.getMethod(propertyName, ((fr.inria.diverse.k3.al.annotationprocessor.Aspect)aspect.getAnnotations()[0]).className(), newValue.getClass()).invoke(eObject, eObject, newValue);
				return true;
				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					Method m = null;
					for(Class<?> c : ((fr.inria.diverse.k3.al.annotationprocessor.Aspect)aspect.getAnnotations()[0]).getClass().getInterfaces()) {
						
						try {
							aspect.getMethod(propertyName, c, newValue.getClass()).invoke(eObject, eObject, newValue);
							return true;
						} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
						}
						if (m == null) {
							throw new RuntimeException("not method found for "+((fr.inria.diverse.k3.al.annotationprocessor.Aspect)aspect.getAnnotations()[0]).className()+"::set"+propertyName);
						}
					}
				}
			return false;
	}
};