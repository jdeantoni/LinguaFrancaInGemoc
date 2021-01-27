package fr.univcotedazur.kairos.languafranca.semantics.k3dsa;

import fr.inria.diverse.k3.al.annotationprocessor.Aspect
import org.icyphy.linguaFranca.Model
import org.icyphy.linguaFranca.Action
import java.util.LinkedList


import static extension fr.univcotedazur.kairos.languafranca.semantics.k3dsa.ModelAspect.*;
import org.icyphy.linguaFranca.Timer
import org.icyphy.linguaFranca.Variable
import org.icyphy.linguaFranca.Reactor

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
}

@Aspect(className=Model)
class ModelAspect { 
	public Integer currentTime = 0;
	/**
	 * This list contains the started timer and the time to wait after the previous timer released
	 */
	public LinkedList<StartedAction> startedTimers = new LinkedList() //for now only Actions but it should also encompass Timer and their first common superclass is Variable
	
	
	def void timeJump(){
		_self.currentTime = _self.startedTimers.first.releaseDate
		//_self.startedTimers.removeFirst
//		_self.currentTime = _self.currentTime + jumpSize
//		var int indexUntilWhichToRemoveElemExcluded = 0
//		while( 
//			indexUntilWhichToRemoveElemExcluded < _self.startedTimers.size
//			&& _self.startedTimers.get(indexUntilWhichToRemoveElemExcluded).delta == 0
//			){
//				indexUntilWhichToRemoveElemExcluded++
//			}; 
//		
//		
//		var tempList = new LinkedList()
//		for(var int i = indexUntilWhichToRemoveElemExcluded; i < _self.startedTimers.size ; i++){
//			tempList.add(_self.startedTimers.get(i))
//		}
//		_self.startedTimers = tempList
		println("currentTime is now "+_self.currentTime)
	}
	
	def void schedule(Variable a, int duration){
		println("beforeSchedule: of "+a.name+" for "+duration+" --> "+_self.startedTimers)
//		var int summedDelta = 0
		if(_self.startedTimers.isEmpty){
			_self.startedTimers.add(new StartedAction(a, _self.currentTime+duration))
			println("afterSchedule: "+_self.startedTimers)
			return
		}//else
		for(var int i = 0; i < _self.startedTimers.size; i++){
//			summedDelta += _self.startedTimers.get(i).delta
			if(_self.startedTimers.get(i).releaseDate > _self.currentTime+duration){
//				val int aDelta = duration - (summedDelta - _self.startedTimers.get(i).delta)
				_self.startedTimers.add(java.lang.Math.max(0, i-1), new StartedAction(a, _self.currentTime+duration)) //Max in case i == 0 (add before)
//				for(var int j = i+1; j < _self.startedTimers.size; j++){ //offset the rest of the list
//					if (_self.startedTimers.get(j).delta != 0){
//						_self.startedTimers.get(j).delta -= aDelta
//					}
//				}
				println("startedTimer (1): "+_self.startedTimers)
				return
			}
//			if (summedDelta == duration && i < (_self.startedTimers.size -1)){
//				val int aDelta = 0
//				_self.startedTimers.add(i, new StartedAction(a, aDelta)) //add after
//				for(var int j = i; j < _self.startedTimers.size; j++){ //offset the rest of the list
//					_self.startedTimers.get(j).delta -= aDelta
//				}
//				println("startedTimer: (2)"+_self.startedTimers)
//				return
//			}
			if (i == (_self.startedTimers.size -1)){
//				val int aDelta = duration - summedDelta
				_self.startedTimers.add(new StartedAction(a, _self.currentTime+duration))//push to the end
				println("startedTimer: (3)"+_self.startedTimers)
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
		model.startedTimers.remove(indexOfSelf)
		println("Timer released ("+model.startedTimers+")")
	}
	
	def void schedule(){
		println("enter schedule of "+_self.name)
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		//TODO: deal with offset
		
		if (_self.period.time !== null){
			model.schedule(_self, _self.period.time.interval)
		}else{
			//look for the parameter in the instance
			var theInstance = model.reactors.findFirst[r | r.main == true].instantiations.findFirst[i | i.reactorClass.name == (_self.eContainer as Reactor).name]
			var period = theInstance.parameters.findFirst[p | p.lhs.name == _self.period.parameter.name].rhs.get(0).time.interval
			model.schedule(_self, period)
		}
		println("exit schedule of "+_self.name)
	}
	
	def boolean canTick(){
		print("Timer "+_self.name+".canRelease() ->")
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
//		var result = false;
//		switch ( indexOfSelf ){
//			case -1 : throw new RuntimeException("ERROR: TimerAspect::canTick() -> timer is not started !")
//			case 0  : result = true
//			case indexOfSelf > 0: result = (model.startedTimers.get(indexOfSelf).delta == 0) && model.startedTimers.subList(1, indexOfSelf).forall[e | e.delta == 0]
//		}
		val list = model.startedTimers
		var result = (model.currentTime == list.get(indexOfSelf).releaseDate)
		println(result)
		return result
	}
	
}

@Aspect(className=Action)
class ActionAspect{
	def void release(){
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
		model.startedTimers.remove(indexOfSelf)
		println("Action released ("+model.startedTimers+")")
	}
	
	
	def void schedule(){
		if(_self.minDelay === null || _self.minDelay.time === null) return;
		
		println("enter schedule of "+_self.name)
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		model.schedule(_self, _self.minDelay.time.interval)
		println("exit schedule of "+_self.name)
	}
	
	def boolean canTick(){
		print("Action "+_self.name+".canRelease() ->")
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
//		var result = false;
//		switch ( indexOfSelf ){
//			case -1 : throw new RuntimeException("ERROR: TimerAspect::canTick() -> timer is not started !")
//			case 0  : result = true
//			case indexOfSelf > 0: result = (model.startedTimers.get(indexOfSelf).delta == 0) && model.startedTimers.subList(1, indexOfSelf).forall[e | e.delta == 0]
//		}
		val list = model.startedTimers
		var result = (model.currentTime == list.get(indexOfSelf).releaseDate)
		println(result)
		return result
	}
	

}