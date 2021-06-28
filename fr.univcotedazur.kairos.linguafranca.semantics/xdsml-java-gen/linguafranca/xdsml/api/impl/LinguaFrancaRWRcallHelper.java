/* GENERATED FILE, do not modify manually                                                    *
 * If you need to modify it, copy it first */
package linguafranca.xdsml.api.impl;
import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gemoc.executionframework.engine.commons.K3DslHelper;


public class LinguaFrancaRWRcallHelper {
  
	public void callSchedule(String onObjectName, String timedConceptName, int param2) {
		return;
	}
	
	public static Method getAspectMethod(EObject eObject, String languageName, String aspectName, String methodName) {
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
		Method res = null;
		 try {
			res = aspect.getDeclaredMethod(methodName, ((fr.inria.diverse.k3.al.annotationprocessor.Aspect)aspect.getAnnotations()[0]).className());
			return res;
		} catch (IllegalArgumentException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}

		return null;
	}
	
}