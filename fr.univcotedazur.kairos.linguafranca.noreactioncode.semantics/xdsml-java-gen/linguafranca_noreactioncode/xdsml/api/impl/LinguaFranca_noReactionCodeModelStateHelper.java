/* GENERATED FILE, do not modify manually                                                    *
 * If you need to modify it, copy it first */
package linguafranca_noreactioncode.xdsml.api.impl;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gemoc.execution.concurrent.ccsljavaengine.extensions.k3.rtd.modelstate.k3ModelState.ElementState;
import org.eclipse.gemoc.execution.concurrent.ccsljavaengine.extensions.k3.rtd.modelstate.k3ModelState.K3ModelState;
import org.eclipse.gemoc.execution.concurrent.ccsljavaengine.extensions.k3.rtd.modelstate.k3ModelState.K3ModelStateFactory;
import org.eclipse.gemoc.execution.concurrent.ccsljavaengine.extensions.k3.dsa.helper.IK3ModelStateHelper;import org.eclipse.gemoc.executionframework.engine.commons.K3DslHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class LinguaFranca_noReactionCodeModelStateHelper implements IK3ModelStateHelper{
	private static class AttributeNameToValue implements Serializable{

		private static final long serialVersionUID = 0;		String name;
		Object value;
		public AttributeNameToValue(String n, Object v) {
			name = n;
			value = v;
		}


		@Override
		public boolean equals(Object obj) {
			if (! (obj instanceof AttributeNameToValue)) {
				return false;
			}
			AttributeNameToValue a2n = (AttributeNameToValue)obj;
			if (this.name.compareTo(a2n.name) != 0) {
				return false;
			}
			if (this.value == null) {
				return a2n.value == null;
			}
			if (!this.value.equals(a2n.value)) {
				return false;
			}
			return true;
		}	}
		K3ModelStateFactory theFactory = K3ModelStateFactory.eINSTANCE; 

	public K3ModelState getK3StateSpaceModelState(EObject model) {
		return getK3ModelState(model, false);
	}
	

	public K3ModelState getK3ModelState(EObject model) {
		return getK3ModelState(model, true);
	}
		
	public K3ModelState getK3ModelState(EObject model, boolean allRTDs) {
		K3ModelState res = theFactory.createK3ModelState();

		Class<?> clazz =null;
		clazz = K3DslHelper.getTarget(fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ConnectionAspect.class);
		if (clazz.isInstance(model)) {
			ElementState elemState = theFactory.createElementState();
			elemState.setModelElement(model);
			res.getOwnedElementstates().add(elemState);
				AttributeNameToValue n2v0 = new AttributeNameToValue("bufferedValues", LinguaFranca_noReactionCodeRTDAccessor.getbufferedValues(model));
				elemState.getSavedRTDs().add(n2v0);
		}
		clazz = K3DslHelper.getTarget(fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.StateVarAspect.class);
		if (clazz.isInstance(model)) {
			ElementState elemState = theFactory.createElementState();
			elemState.setModelElement(model);
			res.getOwnedElementstates().add(elemState);
				AttributeNameToValue n2v0 = new AttributeNameToValue("currentStateValue", LinguaFranca_noReactionCodeRTDAccessor.getcurrentStateValue(model));
				elemState.getSavedRTDs().add(n2v0);
		}
		clazz = K3DslHelper.getTarget(fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ActionAspect.class);
		if (clazz.isInstance(model)) {
			ElementState elemState = theFactory.createElementState();
			elemState.setModelElement(model);
			res.getOwnedElementstates().add(elemState);
				AttributeNameToValue n2v0 = new AttributeNameToValue("nextSchedule", LinguaFranca_noReactionCodeRTDAccessor.getnextSchedule(model));
				elemState.getSavedRTDs().add(n2v0);
				AttributeNameToValue n2v1 = new AttributeNameToValue("actionBufferedValues", LinguaFranca_noReactionCodeRTDAccessor.getactionBufferedValues(model));
				elemState.getSavedRTDs().add(n2v1);
		}
		clazz = K3DslHelper.getTarget(fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.VariableAspect.class);
		if (clazz.isInstance(model)) {
			ElementState elemState = theFactory.createElementState();
			elemState.setModelElement(model);
			res.getOwnedElementstates().add(elemState);
				AttributeNameToValue n2v0 = new AttributeNameToValue("currentValue", LinguaFranca_noReactionCodeRTDAccessor.getcurrentValue(model));
				elemState.getSavedRTDs().add(n2v0);
		}
		clazz = K3DslHelper.getTarget(fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.TimerAspect.class);
		if (clazz.isInstance(model)) {
			ElementState elemState = theFactory.createElementState();
			elemState.setModelElement(model);
			res.getOwnedElementstates().add(elemState);
				AttributeNameToValue n2v0 = new AttributeNameToValue("offsetToDo", LinguaFranca_noReactionCodeRTDAccessor.getoffsetToDo(model));
				elemState.getSavedRTDs().add(n2v0);
		}
		clazz = K3DslHelper.getTarget(fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ModelAspect.class);
		if (clazz.isInstance(model)) {
			ElementState elemState = theFactory.createElementState();
			elemState.setModelElement(model);
			res.getOwnedElementstates().add(elemState);
			if (allRTDs) {  //property not in state space:currentTime
				AttributeNameToValue n2v0 = new AttributeNameToValue("currentTime", LinguaFranca_noReactionCodeRTDAccessor.getcurrentTime(model));
				elemState.getSavedRTDs().add(n2v0);
			}
			if (allRTDs) {  //property not in state space:currentMicroStep
				AttributeNameToValue n2v1 = new AttributeNameToValue("currentMicroStep", LinguaFranca_noReactionCodeRTDAccessor.getcurrentMicroStep(model));
				elemState.getSavedRTDs().add(n2v1);
			}
				AttributeNameToValue n2v2 = new AttributeNameToValue("eventQueue", LinguaFranca_noReactionCodeRTDAccessor.geteventQueue(model));
				elemState.getSavedRTDs().add(n2v2);
		}
		TreeIterator<EObject> allContentIt = model.eAllContents();
		while (allContentIt.hasNext()) {
			EObject elem = allContentIt.next();

			clazz =null;
			clazz = K3DslHelper.getTarget(fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ConnectionAspect.class);
			if (clazz.isInstance(elem)) {
				ElementState elemState = theFactory.createElementState();
				elemState.setModelElement(elem);
				res.getOwnedElementstates().add(elemState);
				AttributeNameToValue n2v0 = new AttributeNameToValue("bufferedValues", LinguaFranca_noReactionCodeRTDAccessor.getbufferedValues(elem));
				elemState.getSavedRTDs().add(n2v0);
			}
			clazz = K3DslHelper.getTarget(fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.StateVarAspect.class);
			if (clazz.isInstance(elem)) {
				ElementState elemState = theFactory.createElementState();
				elemState.setModelElement(elem);
				res.getOwnedElementstates().add(elemState);
				AttributeNameToValue n2v0 = new AttributeNameToValue("currentStateValue", LinguaFranca_noReactionCodeRTDAccessor.getcurrentStateValue(elem));
				elemState.getSavedRTDs().add(n2v0);
			}
			clazz = K3DslHelper.getTarget(fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ActionAspect.class);
			if (clazz.isInstance(elem)) {
				ElementState elemState = theFactory.createElementState();
				elemState.setModelElement(elem);
				res.getOwnedElementstates().add(elemState);
				AttributeNameToValue n2v0 = new AttributeNameToValue("nextSchedule", LinguaFranca_noReactionCodeRTDAccessor.getnextSchedule(elem));
				elemState.getSavedRTDs().add(n2v0);
				AttributeNameToValue n2v1 = new AttributeNameToValue("actionBufferedValues", LinguaFranca_noReactionCodeRTDAccessor.getactionBufferedValues(elem));
				elemState.getSavedRTDs().add(n2v1);
			}
			clazz = K3DslHelper.getTarget(fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.VariableAspect.class);
			if (clazz.isInstance(elem)) {
				ElementState elemState = theFactory.createElementState();
				elemState.setModelElement(elem);
				res.getOwnedElementstates().add(elemState);
				AttributeNameToValue n2v0 = new AttributeNameToValue("currentValue", LinguaFranca_noReactionCodeRTDAccessor.getcurrentValue(elem));
				elemState.getSavedRTDs().add(n2v0);
			}
			clazz = K3DslHelper.getTarget(fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.TimerAspect.class);
			if (clazz.isInstance(elem)) {
				ElementState elemState = theFactory.createElementState();
				elemState.setModelElement(elem);
				res.getOwnedElementstates().add(elemState);
				AttributeNameToValue n2v0 = new AttributeNameToValue("offsetToDo", LinguaFranca_noReactionCodeRTDAccessor.getoffsetToDo(elem));
				elemState.getSavedRTDs().add(n2v0);
			}
			clazz = K3DslHelper.getTarget(fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ModelAspect.class);
			if (clazz.isInstance(elem)) {
				ElementState elemState = theFactory.createElementState();
				elemState.setModelElement(elem);
				res.getOwnedElementstates().add(elemState);
				if (allRTDs) {  //property not in state space:currentTime
				AttributeNameToValue n2v0 = new AttributeNameToValue("currentTime", LinguaFranca_noReactionCodeRTDAccessor.getcurrentTime(elem));
				elemState.getSavedRTDs().add(n2v0);
				}
				if (allRTDs) {  //property not in state space:currentMicroStep
				AttributeNameToValue n2v1 = new AttributeNameToValue("currentMicroStep", LinguaFranca_noReactionCodeRTDAccessor.getcurrentMicroStep(elem));
				elemState.getSavedRTDs().add(n2v1);
				}
				AttributeNameToValue n2v2 = new AttributeNameToValue("eventQueue", LinguaFranca_noReactionCodeRTDAccessor.geteventQueue(elem));
				elemState.getSavedRTDs().add(n2v2);
			}
		}
		return res;
		}

		public void restoreModelState(K3ModelState state) {
		for(ElementState elemState : state.getOwnedElementstates()) {
			for(Object o : elemState.getSavedRTDs()) {
				AttributeNameToValue n2v = (AttributeNameToValue)o;
						Method setter = null;
						setter = getSetter(n2v);
						try {
							setter.invoke(null, elemState.getModelElement(), n2v.value);
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							e.printStackTrace();
						}
				
			}
		}
	}


	private Method getSetter(AttributeNameToValue n2v) {
		Method setter = null;
		try {
			if(n2v.value != null) {
				setter = LinguaFranca_noReactionCodeRTDAccessor.class.getMethod("set"+n2v.name, EObject.class, n2v.value.getClass());
			}else {
				for (Method m : LinguaFranca_noReactionCodeRTDAccessor.class.getMethods()) {
					if (m.getName().compareTo("set"+n2v.name) ==0 && m.getParameterCount() == 2) {
						setter= m;
						break;
					}
				}
			}
			return setter;
		} catch (NoSuchMethodException | SecurityException | IllegalArgumentException e) {
			if(n2v.value != null) {
					List<Class> allTypes = getSuperClasses(n2v.value.getClass());
					allTypes.addAll(Arrays.asList(n2v.value.getClass().getInterfaces()));
					for(Class<?> c : allTypes) {
						try {
							setter = LinguaFranca_noReactionCodeRTDAccessor.class.getMethod("set"+n2v.name, EObject.class, c);
							return setter;
						} catch (NoSuchMethodException | SecurityException | IllegalArgumentException e1) {
						}
					}
					if (setter == null) {
						throw new RuntimeException("no method found for "+n2v.value.getClass().getName()+"::set"+n2v.name);
					}
				}
			}
			return setter;
	}
	
	public static List<Class> getSuperClasses(Class c) {
		List<Class> r = new ArrayList<>();
		List<Class> q = new ArrayList<>();
		q.add(c);
		while (!q.isEmpty()) {
			c = q.remove(0);
			r.add(c);
			if (c.getSuperclass() != null) {
				q.add(c.getSuperclass());
			}
			for (Class i : c.getInterfaces()) {
				q.add(i);
			}
		}
		return r;
	}
};