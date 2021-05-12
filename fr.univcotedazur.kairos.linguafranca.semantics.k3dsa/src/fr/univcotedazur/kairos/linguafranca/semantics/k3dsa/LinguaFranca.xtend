package fr.univcotedazur.kairos.linguafranca.semantics.k3dsa;

import fr.inria.diverse.k3.al.annotationprocessor.Aspect
import org.eclipse.gemoc.execution.concurrent.ccsljavaxdsml.api.extensions.languages.NotInStateSpace


import static extension fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ModelAspect.*;
import static extension fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.VariableAspect.*;
import static extension fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.StateVarAspect.*;
import static extension fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ActionAspect.*;

import org.lflang.lf.Model
import org.lflang.lf.Reaction
import org.lflang.lf.Reactor
import org.lflang.lf.Variable
import org.lflang.lf.Timer

import fr.inria.diverse.k3.al.annotationprocessor.InitializeModel
import fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.colors.Colors
import java.util.ArrayList
import org.lflang.lf.Action
import org.lflang.lf.Connection
import groovy.lang.GroovyShell
import groovy.lang.Binding
import org.lflang.lf.VarRef
import java.util.Map
import java.util.HashMap
import org.lflang.lf.StateVar
import org.lflang.lf.TriggerRef
import org.lflang.lf.Output
import org.lflang.lf.Port

class DebugLevel{
	public static int level =  1  //0 -> no log //1 -> normal log //2 all logs
} 

class ScheduledAction{
	public var Integer releaseDate
	public var Integer microStep
	
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
	
	override def String toString(){
		var StringBuilder sb = new StringBuilder("[")
		var String sep = ""
		for(sa : this){
			if(sa.microStep == 0){
				sb.append(sep+Colors.RED+sa+Colors.RESET)
			}else{
				sb.append(sep+Colors.GREEN+sa+Colors.RESET)
			}
			sep=", "
		}
		sb.append(Colors.RESET+"]")
		return sb.toString()
	}
	
}


@Aspect(className=Model)
class ModelAspect {
	
	@NotInStateSpace
	public var Integer currentTime = 0; 
	@NotInStateSpace
	public var Integer currentMicroStep = 0; 
	
	/**
	 * This list contains the scheduled action, the time to wait from the previous timer released and the microStep
	 */
	public var EventList startedTimers = new EventList(); //super dense time priority FIFO
	
	
	@InitializeModel
	def void init(String[] s){
		if (DebugLevel.level > 0) println(Colors.RED+"currentTime: "+_self.currentTime+" micro seconds"+Colors.RESET+"   ---   "+Colors.GREEN+_self.currentMicroStep+" micro steps"+Colors.RESET)
		for(Reactor r : _self.reactors){
			for(StateVar sv : r.stateVars){
				sv.currentStateValue = Integer.valueOf(sv.init.get(0).literal)
				//println ("init "+sv.name+"="+sv.currentValue)
			}
		}
	}
	
	def void timeJump(){
		if (DebugLevel.level > 1){
			var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model	
			println("\u001B[34m startedTimer: "+model.startedTimers)
		}
		if (_self.startedTimers.size() == 0){
			println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~  ERROR ! no time Jump to do (no timer armed)")
			return
		}
		var jumpSize = _self.startedTimers.get(0).releaseDate
		_self.currentTime = jumpSize + _self.currentTime
		_self.currentMicroStep = _self.startedTimers.get(0).microStep
		var EventList newEl = new EventList()
		for(ScheduledAction sa : _self.startedTimers){
			if(sa.releaseDate == jumpSize){
				newEl.add(new ScheduledAction(sa.variable, sa.releaseDate - jumpSize, sa.microStep - _self.currentMicroStep))
			}else{
				newEl.add(new ScheduledAction(sa.variable, sa.releaseDate - jumpSize, sa.microStep))
			}			
		}
		_self.startedTimers = newEl
		if (DebugLevel.level > 0) println(Colors.RED+"currentTime: "+_self.currentTime+" micro seconds"+Colors.RESET+"   ---   "+Colors.GREEN+_self.currentMicroStep+" micro steps"+Colors.RESET)
		if (DebugLevel.level > 1){
			var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model	
			println("startedTimer: "+model.startedTimers)
		}
	}
	
	def void schedule(Object a, int duration, int s){
		if (DebugLevel.level > 1) println("beforeSchedule: of "+a+" for "+duration+" --> "+_self.startedTimers)
		if(_self.startedTimers.isEmpty){
			_self.startedTimers.add(new ScheduledAction(a, duration, s))
			if (DebugLevel.level > 1) println("afterSchedule: "+_self.startedTimers)
			return
		}//else
		for(var int i = 0; i < _self.startedTimers.size; i++){
			if(_self.startedTimers.get(i).releaseDate == duration && _self.startedTimers.get(i).microStep > s){ //micro steps ordering
				_self.startedTimers.add(java.lang.Math.max(0, i-1), new ScheduledAction(a, duration, s)) //Max in case i == 0 (add before)
				if (DebugLevel.level > 1) println("afterSchedule (0): "+_self.startedTimers)
				return
			}
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
		if (DebugLevel.level > 0) println("\t\t scheduled actions: "+_self.startedTimers)
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
			if (DebugLevel.level > 0) println(Colors.RED+"\t\t"+_self.name+ " released "+Colors.RESET)
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
		
		if(indexOfSelf == -1){ //the same timer is not already armed
					model.schedule(_self, nextTimeHop, 0)
		}
		if (DebugLevel.level > 1) println("exit schedule of "+_self.name)
		if (DebugLevel.level > 0) println(Colors.BLUE+"\t\t"+_self.name+" starts: "+Colors.RESET+model.startedTimers)
	}
	
	def boolean canTick(){
		if (DebugLevel.level > 1){
			var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model	
			println("startedTimer: "+model.startedTimers)
		}
		if (DebugLevel.level > 0) print(Colors.PURPLE+"\t\tTimer "+_self.name+".canRelease() ->")
		
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
		val list = model.startedTimers
		var result = (list.get(indexOfSelf).releaseDate == 0)
		if (DebugLevel.level > 0) println(result+Colors.RESET)
		return result
	}
	
}

@Aspect(className=Action)
class ActionAspect{
	
	public Integer nextSchedule = -1
	
	def void release(){
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
		if (indexOfSelf != -1) {
			model.startedTimers.remove(indexOfSelf)
			if (DebugLevel.level > 0) println(((_self.minDelay === null || _self.minDelay.time === null)? Colors.GREEN : Colors.RED)+"\t\t"+_self.name+ " released "+Colors.RESET)
		}else{
			if (DebugLevel.level > 0) println("##########################  error ? Action already released ("+model.startedTimers+")")
		}
	}
	
	
	def void schedule(){
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
		
		if(_self.minDelay === null || _self.minDelay.time === null){ //micro step !
			if(indexOfSelf == -1){ //the same action is not already armed
				if (_self.nextSchedule == -1){
					if (DebugLevel.level > 1) println("enter micro step schedule of "+_self.name)
					model.schedule(_self, 0, model.currentMicroStep+1)
					if (DebugLevel.level > 1) println("exit micro step schedule of "+_self.name)
				}else{
					if (DebugLevel.level > 1) println("enter action scheduled from code "+_self.name)
					if (_self.nextSchedule == 0){
						model.schedule(_self, 0, model.currentMicroStep+1)	
					}else{
						model.schedule(_self, _self.nextSchedule, 0)
					}
					_self.nextSchedule = -1
					if (DebugLevel.level > 1) println("exit action scheduled from code "+_self.name)
				}
			}
			if (DebugLevel.level > 0) println(Colors.BLUE+"\t\t"+_self.name+" starts: "+Colors.RESET+model.startedTimers)
			return
			
		};
		
		if (DebugLevel.level > 1) println("enter schedule of "+_self.name)
		
		
		if(indexOfSelf == -1){ //the same action is not already armed
			model.schedule(_self, _self.minDelay.time.interval * 1000, 0) //TODO use the unit
		}
		
		if (DebugLevel.level > 1) println("exit schedule of "+_self.name)
		if (DebugLevel.level > 0) println(Colors.BLUE+"\t\t"+_self.name+" starts: "+Colors.RESET+model.startedTimers)
	}
	
	def boolean canTick(){
		if (DebugLevel.level > 0) print(Colors.PURPLE+"\t\tAction "+_self.name+".canRelease() ->")
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
		val list = model.startedTimers
		var result = (list.get(indexOfSelf).releaseDate == 0)
		if (DebugLevel.level > 0) println(result+Colors.RESET)
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
			if (DebugLevel.level > 0) println(Colors.RED+"\t\t connection released "+Colors.RESET)
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
		if(indexOfSelf == -1){ //the connection is not already armed
			model.schedule(_self, period*1000, 0) //TODO: use unit
		}
		if (DebugLevel.level > 1) println("exit schedule of "+_self)
		if (DebugLevel.level > 0) println(Colors.BLUE+"\t "+_self.leftPorts.get(0).variable.name+"_to_"+_self.rightPorts.get(0).variable.name+" starts: "+Colors.RESET+model.startedTimers)
	}
	
	def boolean canTick(){
		if (DebugLevel.level > 0) print(Colors.PURPLE+"\t\tConnection "+_self+".canRelease() ->")
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
		val list = model.startedTimers
		var result = (list.get(indexOfSelf).releaseDate == 0)
		if (DebugLevel.level > 0) println(result+Colors.RESET)
		return result
	}
	
}

@Aspect(className=Variable)
class VariableAspect{
	public Integer currentValue
	
	def Boolean isPresent(){
		return _self.currentValue !== null
	}
	
	def void updates(){
		if (_self instanceof Output){
			//TODO
			//retrieved the connected input and update its (next) currentValue
		}
	}
}

@Aspect(className=VarRef)
class VarRefAspect{
	
	def Boolean getTrue(){
		return true
	}
	
	def Boolean isPresent(){
		if (_self.variable instanceof Port){
			return _self.variable.currentValue !== null
		}
		if (_self.variable instanceof Action){
			return (_self.variable as Action).nextSchedule !== null
		}
		return null
	}
}

@Aspect(className=StateVar)
class StateVarAspect{
	public Integer currentStateValue
}

class ReactionExecutionContext{
    /**
     * first object, the actual port, second Object, the value to be assigned 
     **/
    public Map<Object, Integer> outAssignements = new HashMap<Object,Integer>()
    /**
     * first object, the actual action to schedule, second Object, the value to be scheduled (May be more precise than the Object type) 
     **/
    public Map<Object, Object> newSchedules = new HashMap<Object,Object>()
    
    public Map<VarRef, Object> varRefToValue = new HashMap<VarRef,Object>()
}


@Aspect(className=Reaction)
class ReactionAspect{
	public static final String lfGroovyFunctions = '''	
	void SET(Object port, Object val){
	    //println 'SET ' + val +' on ' + port.variable.name 
	    context.outAssignements.put(port.variable, val)
	}
	
	void schedule(Object action, Object val){
	    //println 'Schedule ' + action.variable +' in ' + val 
	    context.newSchedules.put(action.variable, val)
	}
	
	org.lflang.lf.VarRef.metaClass.isPresent =  {delegate.variable != null}
	org.lflang.lf.VarRef.metaClass.propertyMissing = {String name -> if(name=='value'){return context.varRefToValue.get(delegate)}else{throw new RuntimeException("on "+delegate.class+", property not valid: "+name)} }
	
	'''	
	
	def void exec(){
		if (DebugLevel.level > 0) {
			print(Colors.BLUE+"\t\tReaction "+_self.name+" executed (")
			var sep=""
			for(StateVar sv : (_self.eContainer as Reactor).stateVars) {
				print(sep+sv.name+'='+sv.currentStateValue)
				sep=', '
			}
			println(")"+Colors.RESET)
		}
		var Binding binding = new Binding()
		binding.setVariable("self", _self )
		var ReactionExecutionContext context = new ReactionExecutionContext()
		binding.setVariable("context", context )
		
		for(VarRef vRef : _self.sources + _self.effects) { 
			binding.setVariable(vRef.variable.name, vRef)
		}
		for(TriggerRef tRef : _self.triggers) { 
			if (tRef instanceof VarRef){
				binding.setVariable((tRef as VarRef).variable.name, (tRef as VarRef))
				context.varRefToValue.put((tRef as VarRef), (tRef as VarRef).variable.currentValue)
//				println(' in reaction, '+(tRef as VarRef).variable.name+ '=' +(tRef as VarRef).variable)
			}
		}
		
		var String returnStatement = '\n return ['
		var String sep = ""	
		for(StateVar sv : (_self.eContainer as Reactor).stateVars) {
			binding.setVariable(sv.name, sv.currentStateValue)
			returnStatement = returnStatement + sep + sv.name
		}
		returnStatement = returnStatement + ']'
			
//		println("return Statement -> "+returnStatement)
				
		val ucl = ReactionAspect.classLoader
		val shell = new GroovyShell(ucl,binding)
		var res = shell.evaluate(lfGroovyFunctions+ 
								_self.code.body +
								returnStatement)
				as ArrayList<Object>
		
//		println("context from RW= "+ context.outAssignements)
//		println("context from RW= "+ context.newSchedules)
		
		for(VarRef vRef : _self.sources + _self.effects) {
			if (context.outAssignements.containsKey(vRef.variable)){
				vRef.variable.currentValue = context.outAssignements.get(vRef.variable)
//				println("#########    "+vRef.variable.name+" = "+ vRef.variable.currentValue)
			}else{
				vRef.variable.currentValue = null
			}
			if (context.newSchedules.containsKey(vRef.variable)){
				(vRef.variable as Action).nextSchedule = context.newSchedules.get(vRef.variable) as Integer *1000 //TODO: use unit
//				println("#########    sched "+vRef.variable.name+" in "+ (vRef.variable as Action).nextSchedule)
			}
			
		}	
		var i = 0
		for(StateVar sv : (_self.eContainer as Reactor).stateVars) {
			sv.currentStateValue = res.get(i++) as Integer 
		}

		
	}
	
}
