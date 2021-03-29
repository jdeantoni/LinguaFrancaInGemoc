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
import org.icyphy.linguaFranca.Connection

class DebugLevel{
	public static int level = 2 //0 -> no log //1 -> normal log //2 all logs
}

class ScheduledAction{
	public var Integer releaseDate
	public var Integer microStep = 0
	
	public val Object variable
	
	new(Object v, int d, int s){
		releaseDate = d
		microStep = s
		variable = v
	}

	override String toString(){
		if (variable instanceof Variable){
			return variable.name+"@("+releaseDate+","+microStep+")"
		}else{
			return "aConnection@("+releaseDate+","+microStep+")"
		}
	}
	
	override boolean equals(Object v){
		if(v instanceof ScheduledAction){
			return 
				(releaseDate == (v as ScheduledAction).releaseDate)
				&&
				(microStep == (v as ScheduledAction).microStep)
				&& 
				variable == (v as ScheduledAction).variable
		}
		return false
	}
}

class EventList extends ArrayList<ScheduledAction>{
	
	new(EventList el) {
		super(el);	
	}
	new() {
		super();	
	}
	
	override boolean equals(Object o){
		if(o instanceof EventList){
			return this.equals(o as EventList)
		}
		return false
	}
	
	def boolean equals(EventList o){
		return this.size() == o.size() && this.containsAll(o) && o.containsAll(this);
	}
	
}


@Aspect(className=Model)
class ModelAspect {
	@NotInStateSpace
	public var Integer currentTime = 0; 
	@NotInStateSpace
	public var Integer microStep = 0; 
	
	/**
	 * This list contains the scheduled action, the time to wait from the previous timer released and the microStep
	 */
	public var EventList startedTimers = new EventList(); //super dense time priority FIFO
	
	
	def void timeJump(){
		if (DebugLevel.level > 1){
			var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model	
			println("startedTimer: "+model.startedTimers)
		}
		if (_self.startedTimers.size() == 0){
			println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~  ERROR ! no time Jump to do (no timer armed)")
			return
		}
		var jumpSize = _self.startedTimers.get(0).releaseDate
		_self.currentTime = jumpSize + _self.currentTime
		_self.microStep = _self.startedTimers.get(0).microStep
		var EventList newEl = new EventList()
		for(ScheduledAction sa : _self.startedTimers){
			if(sa.releaseDate == jumpSize){
				newEl.add(new ScheduledAction(sa.variable, sa.releaseDate - jumpSize, sa.microStep - _self.microStep))
			}else{
				newEl.add(new ScheduledAction(sa.variable, sa.releaseDate - jumpSize, sa.microStep))
			}			
		}
		_self.startedTimers = newEl
		if (DebugLevel.level > 0) println("currentTime: "+_self.currentTime+","+_self.microStep)
		if (DebugLevel.level > 1){
			var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model	
			println("startedTimer: "+model.startedTimers)
		}
	}
	
	def void schedule(Object a, int duration, int s){
		//TODO: add ordering in the micro steps
		if (DebugLevel.level > 1) println("beforeSchedule: of "+a+" for "+duration+" --> "+_self.startedTimers)
		if(_self.startedTimers.isEmpty){
			_self.startedTimers.add(new ScheduledAction(a, duration, s))
			if (DebugLevel.level > 1) println("afterSchedule: "+_self.startedTimers)
			return
		}//else
		for(var int i = 0; i < _self.startedTimers.size; i++){
			if(_self.startedTimers.get(i).releaseDate > duration){
				_self.startedTimers.add(java.lang.Math.max(0, i-1), new ScheduledAction(a, duration, s)) //Max in case i == 0 (add before)
				if (DebugLevel.level > 1) println("afterSchedule (1): "+_self.startedTimers)
				return
			}
			if (i == (_self.startedTimers.size -1)){
				_self.startedTimers.add(new ScheduledAction(a, duration, s))//push to the end
				if (DebugLevel.level > 1) println("afterSchedule: (3)"+_self.startedTimers)
				return
			}
		}
		if (DebugLevel.level > 0) println("\t\tscheduled tasks: "+_self.startedTimers)
	}
	/**
	 *  @Parameters: The parameter element is of the type Action. It specifies the element whose occurrence is needed to be checked in the startedTimer LinkedList.
	 *
	 *	@Return Value: The method returns the index or position of the first occurrence of the element in the list else -1 if the element is not present in the list. The returned value is of integer type.
	 */
	def int getIndexOfTimer(Object v){
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
	public var Boolean offsetToDo = true 
	
	def void release(){
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
		if (indexOfSelf != -1) {
			model.startedTimers.remove(indexOfSelf)
			if (DebugLevel.level > 0) println("\t\t"+_self.name+ " released ("+model.startedTimers+")")
		}else{
			if (DebugLevel.level > 0) println("####################################   error ? Timer already released ("+model.startedTimers+")")
		}
	}
	
	def void schedule(){
		if (DebugLevel.level > 1) println("enter schedule of "+_self.name)
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
		
		var nextTimeHop = -1
		if(_self.offsetToDo){
			if (DebugLevel.level > 1) println("deal with offset of "+_self.name)
			var offset = 0
			if (_self.offset.time !== null){
				offset = _self.offset.time.interval * 1000 //TODO use the unit
			}else{
			//look for the parameter in the instance
				var theInstance = model.reactors.findFirst[r | r.main == true].instantiations.findFirst[i | i.reactorClass.name == (_self.eContainer as Reactor).name]
				offset = theInstance.parameters.findFirst[p | p.lhs.name == _self.offset.parameter.name].rhs.get(0).time.interval * 1000 //TODO use the unit
			}
			_self.offsetToDo = false
			nextTimeHop = offset
		}else{
			var period = 0
			if (_self.period.time !== null){
				period = _self.period.time.interval * 1000 //TODO use the unit
			}else{
			//look for the parameter in the instance
				var theInstance = model.reactors.findFirst[r | r.main == true].instantiations.findFirst[i | i.reactorClass.name == (_self.eContainer as Reactor).name]
				period = theInstance.parameters.findFirst[p | p.lhs.name == _self.period.parameter.name].rhs.get(0).time.interval * 1000 //TODO use the unit
			}
			nextTimeHop = period
		}
		
//	NOT AN ERROR, two starts arrived in the same logical steps
//		if(indexOfSelf != -1){ //a timer is already armed
//				if(model.startedTimers.get(indexOfSelf).releaseDate != nextTimeHop){ //at the same time
//					println("->>>>>>>>>>>>>>>>>>>>>>>>>>>>    ERROR ? this timer is already armed for this deadline: "+model.startedTimers.get(indexOfSelf));
//					return
//				}
//		}
		model.schedule(_self, nextTimeHop, 0)
		if (DebugLevel.level > 1) println("exit schedule of "+_self.name)
	}
	
	def boolean canTick(){
		if (DebugLevel.level > 1){
			var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model	
			println("startedTimer: "+model.startedTimers)
			print("         Timer "+_self.name+".canRelease() ->")
		}
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
		val list = model.startedTimers
		var result = (list.get(indexOfSelf).releaseDate == 0)
		if (DebugLevel.level > 1) println(result)
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
			if (DebugLevel.level > 0) println("\t\t"+_self.name+ " released ("+model.startedTimers+")")
		}else{
			if (DebugLevel.level > 0) println("error ? Action already released ("+model.startedTimers+")")
		}
	}
	
	
	def void schedule(){
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		if(_self.minDelay === null || _self.minDelay.time === null){ //micro step !
			if (DebugLevel.level > 1) println("enter micro step schedule of "+_self.name)
			model.schedule(_self, 0, model.microStep+1)
			if (DebugLevel.level > 1) println("exit micro step schedule of "+_self.name)
			return
			
		};
		
		if (DebugLevel.level > 1) println("enter schedule of "+_self.name)
		
		val indexOfSelf = model.getIndexOfTimer(_self)
		if(indexOfSelf != -1){ //a timer is already armed -> not an error if at the same logical step
//				if(model.startedTimers.get(indexOfSelf).releaseDate != _self.minDelay.time.interval){ //at the same time
//					println("->>>>>>>>>>>>>>>>>>>>>>>>>>>>    ERROR ? this timed action is already armed for this deadline: "+model.startedTimers.get(indexOfSelf));
//					return
//				}
		}
		model.schedule(_self, _self.minDelay.time.interval * 1000, 0) //TODO use the unit
		if (DebugLevel.level > 1) println("exit schedule of "+_self.name)
	}
	
	def boolean canTick(){
		if (DebugLevel.level > 1) print("Action "+_self.name+".canRelease() ->")
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
		val list = model.startedTimers
		var result = (list.get(indexOfSelf).releaseDate == 0)
		if (DebugLevel.level > 1) println(result)
		return result
	}
	

}

@Aspect(className=Connection)
class ConnectionAspect{
	def void release(){
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
		if (indexOfSelf != -1) {
			model.startedTimers.remove(indexOfSelf)
			if (DebugLevel.level > 0) println("\t\t"+ "connection released ("+model.startedTimers+")")
		}else{
			if (DebugLevel.level > 0) println("####################################   error ? Connection already released ("+model.startedTimers+")")
		}
	}
	
	def void schedule(){
		if (DebugLevel.level > 1) println("enter schedule of a connection"+_self)
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
		
		var period = 0
		if (_self.delay !== null){
			period = _self.delay.interval
		}
		if(indexOfSelf != -1){ //a timer is already armed
			if(model.startedTimers.get(indexOfSelf).releaseDate != period){ //at the same time
				println("->>>>>>>>>>>>>>>>>>>>>>>>>>>>    ERROR ? this timer is already armed for this deadline: "+model.startedTimers.get(indexOfSelf));
				return
			}
		}
		model.schedule(_self, period*1000, 0) //TODO: use unit
		if (DebugLevel.level > 1) println("exit schedule of "+_self)
	}
	
	def boolean canTick(){
		if (DebugLevel.level > 1) print("Connection "+_self+".canRelease() ->")
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
		val list = model.startedTimers
		var result = (list.get(indexOfSelf).releaseDate == 0)
		if (DebugLevel.level > 1) println(result)
		return result
	}
	
}
