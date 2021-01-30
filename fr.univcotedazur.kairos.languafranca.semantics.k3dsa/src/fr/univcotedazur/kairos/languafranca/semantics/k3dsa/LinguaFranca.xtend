package fr.univcotedazur.kairos.languafranca.semantics.k3dsa;

import fr.inria.diverse.k3.al.annotationprocessor.Aspect
import org.eclipse.gemoc.execution.concurrent.ccsljavaxdsml.api.extensions.languages.NotInStateSpace
import org.icyphy.linguaFranca.Model
import org.icyphy.linguaFranca.Action


import static extension fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspect.*;
import org.icyphy.linguaFranca.Timer
import org.icyphy.linguaFranca.Variable
import org.icyphy.linguaFranca.Reactor
import java.util.ArrayList

class StartedAction{
	public var Integer releaseDate
	public val Variable variable
	
	new(Variable v, int d){
		releaseDate = d
		variable = v
	}

	override String toString(){
		return "("+variable.name+"@"+releaseDate+")"
	}
	
	override boolean equals(Object v){
		if(v instanceof StartedAction){
			return 
				(releaseDate == (v as StartedAction).releaseDate)
				&& 
				variable == (v as StartedAction).variable
		}
		return false
	}
}

class DebugLevel{
	public static int level = 0
}

@Aspect(className=Model)
class ModelAspect {
	@NotInStateSpace
	public var Integer currentTime = 0; //0 -> no log
	/**
	 * This list contains the started timer and the time to wait after the previous timer released
	 */
	public var ArrayList<StartedAction> startedTimers = new ArrayList(); //for now only Actions but it should also encompass Timer and their first common superclass is Variable
	
	
	def void timeJump(){
		if (_self.startedTimers.size() == 0){
			println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~  ERROR ? not time Jump to do (no timer armed)")
			return
		}
		var jumpSize = _self.startedTimers.get(0).releaseDate
		_self.currentTime = jumpSize + _self.currentTime
		for(StartedAction sa : _self.startedTimers){
			sa.releaseDate = sa.releaseDate - jumpSize			
		}
		if (DebugLevel.level > 0) println("currentTime is now "+_self.currentTime)
	}
	
	def void schedule(Variable a, int duration){
		if (DebugLevel.level > 0) println("beforeSchedule: of "+a.name+" for "+duration+" --> "+_self.startedTimers)
		if(_self.startedTimers.isEmpty){
			_self.startedTimers.add(new StartedAction(a, duration))
			if (DebugLevel.level > 0) println("afterSchedule: "+_self.startedTimers)
			return
		}//else
		for(var int i = 0; i < _self.startedTimers.size; i++){
			if(_self.startedTimers.get(i).releaseDate > duration){
				_self.startedTimers.add(java.lang.Math.max(0, i-1), new StartedAction(a, duration)) //Max in case i == 0 (add before)
				if (DebugLevel.level > 0) println("startedTimer (1): "+_self.startedTimers)
				return
			}
			if (i == (_self.startedTimers.size -1)){
				_self.startedTimers.add(new StartedAction(a, duration))//push to the end
				if (DebugLevel.level > 0) println("startedTimer: (3)"+_self.startedTimers)
				return
			}
		}
	}
	/**
	 *  @Parameters: The parameter element is of the type Action. It specifies the element whose occurrence is needed to be checked in the startedTimer LinkedList.
	 *
	 *	@Return Value: The method returns the index or position of the first occurrence of the element in the list else -1 if the element is not present in the list. The returned value is of integer type.
	 */
	def int getIndexOfTimer(Variable v){
		for(var int i = 0; i < _self.startedTimers.size ; i++){
			if (_self.startedTimers.get(i).variable == v){
				return i;
			}
		}
		return -1 ; //not found
	}
}

@Aspect(className=Timer)
class TimerAspect{
	def void release(){
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
		if (indexOfSelf != -1) {
			model.startedTimers.remove(indexOfSelf)
			if (DebugLevel.level > 0) println("Timer released ("+model.startedTimers+")")
		}else{
			if (DebugLevel.level > 0) println("####################################   error ? Timer already released ("+model.startedTimers+")")
		}
	}
	
	def void schedule(){
		if (DebugLevel.level > 0) println("enter schedule of "+_self.name)
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
		
		//TODO: deal with offset
		var period = 0
		if (_self.period.time !== null){
			period = _self.period.time.interval
		}else{
			//look for the parameter in the instance
			var theInstance = model.reactors.findFirst[r | r.main == true].instantiations.findFirst[i | i.reactorClass.name == (_self.eContainer as Reactor).name]
			period = theInstance.parameters.findFirst[p | p.lhs.name == _self.period.parameter.name].rhs.get(0).time.interval
		}
		if(indexOfSelf != -1){ //a timer is already armed
				if(model.startedTimers.get(indexOfSelf).releaseDate != period){ //at the same time
					println("->>>>>>>>>>>>>>>>>>>>>>>>>>>>    ERROR ? this timer is already armed for this deadline: "+model.startedTimers.get(indexOfSelf));
					return
				}
		}
		model.schedule(_self, period)
		if (DebugLevel.level > 0) println("exit schedule of "+_self.name)
	}
	
	def boolean canTick(){
		if (DebugLevel.level > 0) print("Timer "+_self.name+".canRelease() ->")
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
		val list = model.startedTimers
		var result = (list.get(indexOfSelf).releaseDate == 0)
		if (DebugLevel.level > 0) println(result)
		return result
	}
	
}

@Aspect(className=Action)
class ActionAspect{
	def void release(){
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
		if (indexOfSelf != -1) {
			model.startedTimers.remove(indexOfSelf)
			if (DebugLevel.level > 0) println("Action released ("+model.startedTimers+")")
		}else{
			if (DebugLevel.level > 0) println("error ? Action already released ("+model.startedTimers+")")
		}
	}
	
	
	def void schedule(){
		if(_self.minDelay === null || _self.minDelay.time === null) return;
		
		if (DebugLevel.level > 0) println("enter schedule of "+_self.name)
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
		if(indexOfSelf != -1){ //a timer is already armed
				if(model.startedTimers.get(indexOfSelf).releaseDate != _self.minDelay.time.interval){ //at the same time
					println("->>>>>>>>>>>>>>>>>>>>>>>>>>>>    ERROR ? this timed action is already armed for this deadline: "+model.startedTimers.get(indexOfSelf));
					return
				}
		}
		model.schedule(_self, _self.minDelay.time.interval)
		if (DebugLevel.level > 0) println("exit schedule of "+_self.name)
	}
	
	def boolean canTick(){
		if (DebugLevel.level > 0) print("Action "+_self.name+".canRelease() ->")
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
		val list = model.startedTimers
		var result = (list.get(indexOfSelf).releaseDate == 0)
		if (DebugLevel.level > 0) println(result)
		return result
	}
	

}