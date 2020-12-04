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
	public var int delta
	public val Variable variable
	
	new(Variable v, int d){
		delta = d
		variable = v
	}
	
	override String toString(){
		return "("+variable.name+" in "+delta+")"
	}
}

@Aspect(className=Model)
class ModelAspect { 
	public Integer currentTime = 0;
	/**
	 * This list contains the started timer and the time to wait after the previous timer released
	 */
	public LinkedList<StartedAction> startedTimer = new LinkedList() //for now only Actions but it should also encompass Timer and their first common superclass is Variable
	
	
	def void timeJump(){
		val jumpSize = _self.startedTimer.first.delta
		_self.startedTimer.removeFirst
		_self.currentTime = _self.currentTime + jumpSize
		var int indexUntilWhichToRemoveElemExcluded = 0
		while( 
			indexUntilWhichToRemoveElemExcluded < _self.startedTimer.size
			&& _self.startedTimer.get(indexUntilWhichToRemoveElemExcluded).delta == 0
			){
				indexUntilWhichToRemoveElemExcluded++
			}; 
		
		
		var tempList = new LinkedList()
		for(var int i = indexUntilWhichToRemoveElemExcluded; i < _self.startedTimer.size ; i++){
			tempList.add(_self.startedTimer.get(i))
		}
		_self.startedTimer = tempList
		println("currentTime is now "+_self.currentTime)
	}
	
	def void schedule(Variable a, int duration){
		var int summedDelta = 0
		if(_self.startedTimer.isEmpty){
			_self.startedTimer.add(new StartedAction(a, duration))
			println("startedTimer: "+_self.startedTimer)
			return
		}//else
		for(var int i = 0; i < _self.startedTimer.size; i++){
			summedDelta += _self.startedTimer.get(i).delta
			if(summedDelta > duration){
				val int aDelta = duration - (summedDelta - _self.startedTimer.get(i).delta)
				_self.startedTimer.add(java.lang.Math.max(0, i-1), new StartedAction(a, aDelta)) //Max in case i == 0 (add before)
				for(var int j = i; j < _self.startedTimer.size; j++){ //offset the rest of the list
					_self.startedTimer.get(j).delta -= aDelta
				}
				println("startedTimer: "+_self.startedTimer)
				return
			}
			if (summedDelta == duration && i < (_self.startedTimer.size -1)){
				val int aDelta = 0
				_self.startedTimer.add(i, new StartedAction(a, aDelta)) //add after
				for(var int j = i; j < _self.startedTimer.size; j++){ //offset the rest of the list
					_self.startedTimer.get(j).delta -= aDelta
				}
				println("startedTimer: "+_self.startedTimer)
				return
			}
			if (i == (_self.startedTimer.size -1)){
				val int aDelta = duration - summedDelta
				_self.startedTimer.add(new StartedAction(a, aDelta))//push to the end
				println("startedTimer: "+_self.startedTimer)
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
		for(var int i = 0; i < _self.startedTimer.size ; i++){
			if (_self.startedTimer.get(i).variable == v){
				return i;
			}
		}
		return -1 ; //not found
	}
}

@Aspect(className=Timer)
class TimerAspect{
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
		print(_self.name+".canTick() ->")
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
		var result = false;
		switch ( indexOfSelf ){
			case -1 : throw new RuntimeException("ERROR: TimerAspect::canTick() -> timer is not started !")
			case 0  : result = true
			case indexOfSelf > 0: result = (model.startedTimer.get(indexOfSelf).delta == 0) && model.startedTimer.subList(1, indexOfSelf).forall[e | e.delta == 0]
		}
		println(result)
		return result
	}
	
}

@Aspect(className=Action)
class ActionAspect{
	
	def void schedule(){
		if(_self.minDelay === null || _self.minDelay.time === null) return;
		
		println("enter schedule of "+_self.name)
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		model.schedule(_self, _self.minDelay.time.interval)
		println("exit schedule of "+_self.name)
	}
	
	def boolean canTick(){
		print(_self.name+".canTick() ->")
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
		var result = false;
		switch ( indexOfSelf ){
			case -1 : throw new RuntimeException("ERROR: ActionAspect::canTick() -> timer is not started !")
			case 0  : result = true
			case indexOfSelf > 0: result = (model.startedTimer.get(indexOfSelf).delta == 0) && model.startedTimer.subList(1, indexOfSelf).forall[e | e.delta == 0]
		}
		println(result)
		return result
	}
	

}