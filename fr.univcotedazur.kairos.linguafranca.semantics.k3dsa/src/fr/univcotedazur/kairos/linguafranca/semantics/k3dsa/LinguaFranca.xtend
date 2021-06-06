package fr.univcotedazur.kairos.linguafranca.semantics.k3dsa;

import fr.inria.diverse.k3.al.annotationprocessor.Aspect
import fr.inria.diverse.k3.al.annotationprocessor.InitializeModel
import fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.colors.Colors
import groovy.lang.Binding
import groovy.lang.GroovyShell
import java.util.ArrayList
import java.util.HashMap
import java.util.LinkedList
import java.util.List
import java.util.Map
import org.eclipse.gemoc.execution.concurrent.ccsljavaxdsml.api.extensions.languages.NotInStateSpace
import org.lflang.lf.Action
import org.lflang.lf.Connection
import org.lflang.lf.KeyValuePair
import org.lflang.lf.Model
import org.lflang.lf.Output
import org.lflang.lf.Parameter
import org.lflang.lf.Port
import org.lflang.lf.Reaction
import org.lflang.lf.Reactor
import org.lflang.lf.StateVar
import org.lflang.lf.TimedConcept
import org.lflang.lf.Timer
import org.lflang.lf.TriggerRef
import org.lflang.lf.VarRef
import org.lflang.lf.Variable

import static extension fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ActionAspect.*
import static extension fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ConnectionAspect.*
import static extension fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.ModelAspect.*
import static extension fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.StateVarAspect.*
import static extension fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.TimerAspect.*
import static extension fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.VariableAspect.*

class DebugLevel{
	public static int level =  0  //0 -> no log //1 -> normal log //2 all logs
} 
 
class ScheduledTimeAdvancement{
	public var Integer releaseDate
	public var Integer microStep
	
	public val Object timedConcept
	
	new(Object v, int d, int s){ 
		releaseDate = d
		microStep = s
		timedConcept =  v
	}

	override String toString(){
		if (timedConcept instanceof Variable){
			return timedConcept.name+"@("+releaseDate+","+microStep+")"
		}else{
			return "aConnection@("+releaseDate+","+microStep+")"
		}
	}
	
	override boolean equals(Object v){
		if(v instanceof ScheduledTimeAdvancement){
			return 
				(releaseDate == (v as ScheduledTimeAdvancement).releaseDate)
				&&
				(microStep == (v as ScheduledTimeAdvancement).microStep)
				&& 
				timedConcept == (v as ScheduledTimeAdvancement).timedConcept
		}
		return false
	}
}

class EventQueue extends LinkedList<ScheduledTimeAdvancement>{
	
	new(EventQueue el) {
		super(el);	
	}
	new() {
		super();	
	}
	
	override boolean equals(Object o){
		if(o instanceof EventQueue){
			return this.equals(o as EventQueue)
		}
		return false
	}
	
	def boolean equals(EventQueue o){
		return this.size() == o.size() && this.containsAll(o) && o.containsAll(this);
	}
	
	override def String toString(){
		var StringBuilder sb = new StringBuilder("[")
		var String sep = ""
		for(sa : this){
//			if(sa.microStep == 0){
//				sb.append(sep+Colors.RED+sa+Colors.RESET)
//			}else{
//				sb.append(sep+Colors.GREEN+sa+Colors.RESET)
//			}
			sb.append(sep+sa)
			sep=", "
		}
//		sb.append(Colors.RESET+"]")
		sb.append("]")
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
	 * This list contains the scheduled timed action, the time to wait from the previous timer released and the microStep
	 */
	public var EventQueue eventQueue = new EventQueue(); //super dense time priority queue
	
	
	@InitializeModel
	def void init(String[] s){
		
		for(KeyValuePair p : _self.target.config.pairs){
			if (p.name == "logging"){
				DebugLevel.level = Integer.parseInt(p.value.literal);
			}
		}
		
		
		if (DebugLevel.level > 0) println(Colors.RED+"currentTime: "+_self.currentTime+" micro seconds"+Colors.RESET+"   ---   "+Colors.GREEN+_self.currentMicroStep+" micro steps"+Colors.RESET)
		for(Reactor r : _self.reactors){
			for(StateVar sv : r.stateVars){
				if (sv.init.size() == 0 || sv.init.get(0).literal === null){
					return
				}
				sv.currentStateValue = Integer.valueOf(sv.init.get(0).literal)
			}
		}
		
		var it = _self.eAllContents
		while(it.hasNext()){
			val elem = it.next
			if(elem instanceof Timer){
				if (DebugLevel.level > 1) println("deal with offset of "+elem.name)
				var offset = elem.getOffset(_self)
				if(offset == 0){
					_self.schedule(elem, offset)
					elem.offsetToDo = false
				}
			}
		}
		
	}
	
//	def int getOffset(Timer elem) {
//		if (elem.offset.time !== null){
//			return elem.offset.time.interval * 1000 //TODO use the unit
//		}else
//			if (elem.offset.literal !== null){
//				return Integer.parseInt(elem.offset.literal) * 1000 //TODO use the unit
//			}else
//				{
//				//look for the parameter in the instance
//					var theInstance = _self.reactors.findFirst[r | r.main == true].instantiations.findFirst[i | i.reactorClass.name == (elem.eContainer as Reactor).name]
//					return theInstance.parameters.findFirst[p | p.lhs.name == elem.offset.parameter.name].rhs.get(0).time.interval * 1000 //TODO use the unit
//				}
//	}
	
	def void timeJump(){
		if (DebugLevel.level > 1){
			var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model	
			println("startedTimer: "+model.eventQueue)
		}
		if (_self.eventQueue.size() == 0){
			println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~  ERROR ! no time Jump to do (no timer armed)")
			return
		}
		var jumpSize = _self.eventQueue.get(0).releaseDate
		_self.currentTime = jumpSize + _self.currentTime
		_self.currentMicroStep = _self.eventQueue.get(0).microStep
		var EventQueue newEl = new EventQueue()
		for(ScheduledTimeAdvancement sa : _self.eventQueue){
			if(sa.releaseDate == jumpSize){
				newEl.add(new ScheduledTimeAdvancement(sa.timedConcept, sa.releaseDate - jumpSize, sa.microStep - _self.currentMicroStep))
			}else{
				newEl.add(new ScheduledTimeAdvancement(sa.timedConcept, sa.releaseDate - jumpSize, sa.microStep))
			}			
		}
		_self.eventQueue = newEl
		if (DebugLevel.level > 0) println(Colors.RED+"currentTime: "+_self.currentTime+" micro seconds"+Colors.RESET+"   ---   "+Colors.GREEN+_self.currentMicroStep+" micro steps"+Colors.RESET)
		if (DebugLevel.level > 1){
			var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model	
			println("startedTimer: "+model.eventQueue)
		}
	}
	
	def void schedule(TimedConcept tc, int rlt){
		if (DebugLevel.level > 1) println("beforeSchedule: of "+tc+" for "+rlt+" --> "+_self.eventQueue)
		if(_self.eventQueue.isEmpty){
			_self.eventQueue.add(new ScheduledTimeAdvancement(tc, rlt, 0))
			if (DebugLevel.level > 1) println("afterSchedule: "+_self.eventQueue)
			if (DebugLevel.level > 0) println("\t\t scheduled time advancements: "+_self.eventQueue)
			return
		}
		var int lastMS = -1;
		for(var int i = 0; i < _self.eventQueue.size; i++){
			var staAtI= _self.eventQueue.get(i);
			if(staAtI.timedConcept == tc && staAtI.releaseDate == rlt){ //micro steps ordering since already exists
				lastMS = staAtI.microStep
				if (DebugLevel.level > 1) println("same ta founded with microStep: "+lastMS)
			}
			if(staAtI.releaseDate > rlt){
				if (rlt == 0 && lastMS == -1){
					lastMS = _self.currentMicroStep
				}
				_self.eventQueue.add(i, new ScheduledTimeAdvancement(tc, rlt, (lastMS == -1) ? 0 : lastMS+1))
				if (DebugLevel.level > 1) println("afterSchedule middle of list (1): "+_self.eventQueue)
				if (DebugLevel.level > 0) println("\t\t scheduled time advancements: "+_self.eventQueue)
				return
			}
		}
		_self.eventQueue.addLast(new ScheduledTimeAdvancement(tc, rlt, 0))//add to the end
		if (DebugLevel.level > 1) println("afterSchedule end of list: (2)"+_self.eventQueue)
		if (DebugLevel.level > 0) println("\t\t scheduled time advancements: "+_self.eventQueue)
		return
	}
	/**
	 *  @Parameters: The parameter element is of the type TimedConcept. It specifies the element whose occurrence is needed to be checked in the startedTimers LinkedList.
	 *
	 *	@Return Value: The method returns the index or position of the first occurrence of the element in the list else -1 if the element is not present in the list. The returned value is of integer type.
	 */
	def int getIndexOfTimer(Object v){
		for(var int i = 0; i < _self.eventQueue.size ; i++){
			if (_self.eventQueue.get(i).timedConcept == v){
				return i;
			}
		}
		return -1 ; //not found
	}
	
	
		/**
	 *  @Parameters: The parameter element is of the type TimedConcept. It specifies the element whose occurrence is needed to be checked in the startedTimers LinkedList.
	 *
	 *	@Return Value: The method returns the index or position of the last occurrence of the element in the list else -1 if the element is not present in the list. The returned value is of integer type.
	 */
	def int getLastIndexOfTimer(Object v, int duration){
		for(var int i = _self.eventQueue.size-1; i >= 0  ; i--){
			if (_self.eventQueue.get(i).timedConcept == v && _self.eventQueue.get(i).releaseDate == duration){
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
			model.eventQueue.remove(indexOfSelf)
			if (DebugLevel.level > 0) println(Colors.RED+"\t\t"+_self.name+ " released "+Colors.RESET)
		}else{
			if (DebugLevel.level > 0) println("####################################   error ? Timer already released ("+model.eventQueue+")")
		}
	}
	
	def void schedule(){
		if (DebugLevel.level > 1) println("enter schedule of "+_self.name)
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model		
		var nextTimeHop = -1
		if(_self.offsetToDo){
			if (DebugLevel.level > 1) println("deal with offset of "+_self.name)
			var offset = _self.getOffset(model)
			_self.offsetToDo = false
			nextTimeHop = offset
		}else{
			var period = _self.getPeriod(model)
			nextTimeHop = period
		}
		
		model.schedule(_self, nextTimeHop)
		if (DebugLevel.level > 1) println("exit schedule of "+_self.name)
		if (DebugLevel.level > 0) println(Colors.BLUE+"\t\t"+_self.name+" starts: "+Colors.RESET+model.eventQueue)
	}
	
	protected def int getPeriod(Timer _self, Model model) {
		if (_self.period.time !== null){
			return _self.period.time.interval * 1000 //TODO use the unit
		}else
			if (_self.offset.literal !== null){
				return  Integer.parseInt(_self.period.literal) * 1000 //TODO use the unit
			}else{
				//look for the parameter in the instance
					var theInstance = model.reactors.findFirst[r | r.main == true].instantiations.findFirst[i | i.reactorClass.name == (_self.eContainer as Reactor).name]
					return  theInstance.parameters.findFirst[p | p.lhs.name == _self.period.parameter.name].rhs.get(0).time.interval * 1000 //TODO use the unit
				}
	}
	
	protected def int getOffset(Timer _self, Model model) {
		if (_self.offset.time !== null){
			return _self.offset.time.interval * 1000 //TODO use the unit
		}else
			if (_self.offset.literal !== null){
				return Integer.parseInt(_self.offset.literal) * 1000 //TODO use the unit
			}else
				{
				//look for the parameter in the instance
					var theInstance = model.reactors.findFirst[r | r.main == true].instantiations.findFirst[i | i.reactorClass.name == (_self.eContainer as Reactor).name]
					return theInstance.parameters.findFirst[p | p.lhs.name == _self.offset.parameter.name].rhs.get(0).time.interval * 1000 //TODO use the unit
				}
	}
	
	def boolean canTick(){
		if (DebugLevel.level > 1){
			var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model	
			println("startedTimer: "+model.eventQueue)
		}
		if (DebugLevel.level > 0) print(Colors.PURPLE+"\t\tTimer "+_self.name+".canRelease() ->")
		
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
		
		if(indexOfSelf == -1){
			return false;
		}
		var result = (model.eventQueue.get(indexOfSelf).releaseDate == 0)
		if (DebugLevel.level > 0) println(result+Colors.RESET)
		return result
	}
	
}

@Aspect(className=Action)
class ActionAspect{
	
	public Integer nextSchedule = -1
	public LinkedList<Object> actionBufferedValues = new LinkedList<Object>()
	
	def void release(){
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
		if (indexOfSelf != -1) {
			model.eventQueue.remove(indexOfSelf)
			if (DebugLevel.level > 0) println(((_self.minDelay === null || _self.minDelay.time === null)? Colors.GREEN : Colors.RED)+"\t\t"+_self.name+ " released "+Colors.RESET)
		}else{
			if (DebugLevel.level > 0) println("##########################  error ? Action already released ("+model.eventQueue+")")
		}
	}
	
	
	def void schedule(){
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		var int rlt = 0
		if (_self.nextSchedule != -1){
			rlt = _self.nextSchedule
		}else{
			if(_self.minDelay !== null && _self.minDelay.time !== null){ //not a micro step required
				rlt = _self.minDelay.time.interval * 1000 //TODO use the unit
			}
		}
		model.schedule(_self, rlt)
		_self.nextSchedule = -1
				
		if (DebugLevel.level > 0) println(Colors.BLUE+"\t\t"+_self.name+" starts: "+Colors.RESET+model.eventQueue)
		return
	}
	
	def boolean canTick(){
		if (DebugLevel.level > 0) print(Colors.PURPLE+"\t\tAction "+_self.name+".canRelease() ->")
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
		
		if (indexOfSelf == -1){
			if (DebugLevel.level > 0) println("false"+Colors.RESET)
			return false
		}
		var result = (model.eventQueue.get(indexOfSelf).releaseDate == 0)
		if (DebugLevel.level > 0) println(result+Colors.RESET)
		return result
	}
	

}

@Aspect(className=Connection)
class ConnectionAspect{
	
	public LinkedList<Object> bufferedValues = new LinkedList<Object>()
	
	
	def void release(){
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
		if (indexOfSelf != -1) {
			model.eventQueue.remove(indexOfSelf)
			_self.rightPorts.get(0).variable.currentValue = _self.bufferedValues.removeFirst() as Integer
			if (DebugLevel.level > 0) println(Colors.RED+"\t\t connection released "+Colors.RESET)
		}else{
			if (DebugLevel.level > 0) println("####################################   error ? Connection already released ("+model.eventQueue+")")
		}
	}
	
	def void schedule(){
		if (DebugLevel.level > 1) println("enter schedule of a connection"+_self)
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
				
		var delay = 0
		if (_self.delay !== null){
			delay = _self.delay.interval
		}
		model.schedule(_self, delay*1000) //TODO: use unit
		if (DebugLevel.level > 1) println("exit schedule of "+_self)
		if (DebugLevel.level > 0) println(Colors.BLUE+"\t "+_self.leftPorts.get(0).variable.name+"_to_"+_self.rightPorts.get(0).variable.name+" starts: "+Colors.RESET+model.eventQueue)
	}
	
	def boolean canTick(){
		if (DebugLevel.level > 0) print(Colors.PURPLE+"\t\tConnection "+_self+".canRelease() ->")
		var Model model = _self.eResource.allContents.findFirst[eo | eo instanceof Model] as Model
		val indexOfSelf = model.getIndexOfTimer(_self)
		if (indexOfSelf == -1){
			return false
		}
		var result = (model.eventQueue.get(indexOfSelf).releaseDate == 0)
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
	
	def void absent(){
		_self.currentValue = null
	}
	
	def void present(){
		if (_self instanceof Output){
			var List<Connection> allConnections = _self.eResource.contents.get(0).eAllContents.filter[eo | eo instanceof Connection].map[eo | eo as Connection].toList
			
			for(Connection c : allConnections){
				if (c.delay === null && c.leftPorts.get(0).variable == _self){
					c.rightPorts.get(0).variable.currentValue= _self.currentValue
				}
				if (c.delay !== null && c.leftPorts.get(0).variable == _self && _self.currentValue !== null){
					c.bufferedValues.add(_self.currentValue)
					if (DebugLevel.level > 1) println('bufferedValues of '+c.leftPorts.get(0).variable+'->'+c.rightPorts.get(0).variable+'='+c.bufferedValues)
				}
			}
			_self.currentValue = null	
		}
		if (_self instanceof Action){
			if (_self.currentValue !== null){
				_self.actionBufferedValues.add(_self.currentValue)
				if (DebugLevel.level > 1) println('bufferedValues of '+_self.name+'='+_self.actionBufferedValues)
			}
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
			return (_self.variable as Action).nextSchedule != -1 
		}
		return false
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
    public Map<Object, Object> newValuedSchedules = new HashMap<Object,Object>()
    
    public Map<Object, Object> varToValue = new HashMap<Object,Object>()
}


@Aspect(className=Reaction)
class ReactionAspect{
	@NotInStateSpace
	public static final String lfGroovyFunctions = '''	
	void SET(Object port, Object val){
	    //println 'SET ' + val +' on ' + port.variable.name 
	    context.outAssignements.put(port.variable, val)
	}
	
	void schedule(Object action, Object val){
	    //println 'Schedule ' + action.variable +' in ' + val 
	    context.newSchedules.put(action.variable, val)
	}
	
	void schedule(Object action, Object timeVal, Object val){
	    //println 'Schedule ' + action.variable +' in ' + timeVal 
	    schedule(action, timeVal)
	    SET(action,val)
	}
	
	Boolean isPresent(Object v){
	  //  println 'isPresent ' + v +' is ' + context.varToValue+'\n\tisPresent '+context.varToValue.get(v.variable)
	    return context.varToValue.get(v.variable) != null
	}
	
	Object valueOf(Object v){
	    //println 'isPresent ' + v +' is ' + context.varToValue+'\n\tisPresent '+context.varToValue.get(v.variable)
	    return context.varToValue.get(v.variable)
	}
	
	def getLogicalTime(){
		return [currentTime, currentMicroStep]
	}
	
	//org.lflang.lf.VarRef.metaClass.isPresent =  {println('-------'+context.varToValue+'\n\tisPresent '+delegate.variable+'      '+context.varToValue.get(delegate.variable)); return context.varToValue.get(delegate.variable) != null}
	//org.lflang.lf.Input.metaClass.isPresent =  {println('+++++++'+context.varToValue+'\n\tisPresent '+delegate+'      '+context.varToValue.get(delegate)); return context.varToValue.get(delegate) != null}
	//org.lflang.lf.VarRef.metaClass.propertyMissing = {String name -> if(name=='value'){return context.varToValue.get(delegate.variable)}else{throw new RuntimeException("on "+delegate.class+", property not valid: "+name)} }
	//org.lflang.lf.Input.metaClass.propertyMissing = {String name -> if(name=='value'){return context.varToValue.get(delegate)}else{throw new RuntimeException("on "+delegate.class+", property not valid: "+name)} }
	
	//org.lflang.lf.Reaction.metaClass.propertyMissing = {String name -> if(name=='id'){return delegate.eContainer().name} }
	
	'''	
	
	def void exec(){
		if (DebugLevel.level > 0) {
			print(Colors.BLUE+"\t\tReaction "+(_self.eContainer() as Reactor).name+"."+((_self.name !== null) ? _self.name : (_self.eContainer as Reactor).reactions.indexOf(_self)) +" executed (")
			var sep=""
			for(StateVar sv : (_self.eContainer as Reactor).stateVars) {
				print(sep+sv.name+'='+sv.currentStateValue)
				sep=', '
			}
			println(")"+Colors.RESET)
		}
		var Binding binding = new Binding()
		binding.setVariable("self", _self.eContainer() )
		var ReactionExecutionContext context = new ReactionExecutionContext()
		binding.setVariable("context", context )
		var Model theModel = _self.eResource.contents.get(0) as Model
		binding.setVariable("currentTime", theModel.currentTime)
		binding.setVariable("currentMicroStep", theModel.currentMicroStep)
		
		for(Parameter p : (_self.eContainer() as Reactor).parameters){
			binding.setVariable(p.name, p.init.get(0).literal ) //no links to instances :-(
		}
		
		for(VarRef vRef :  _self.sources +_self.effects) { 
			binding.setVariable(vRef.variable.name, vRef)
		}
		for(TriggerRef tRef : _self.triggers) { 
			if (tRef instanceof VarRef && (tRef as VarRef).variable instanceof Port){
				binding.setVariable((tRef as VarRef).variable.name, (tRef as VarRef))
				context.varToValue.put((tRef as VarRef).variable, (tRef as VarRef).variable.currentValue)
				if (DebugLevel.level > 1) println(' in reaction, '+(tRef as VarRef).variable.name+ '=' +(tRef as VarRef).variable.currentValue)
			}
			if (tRef instanceof VarRef && (tRef as VarRef).variable instanceof Action){
				binding.setVariable((tRef as VarRef).variable.name, (tRef as VarRef))
				if (! ((tRef as VarRef).variable as Action).actionBufferedValues.empty){ //not always a valued action
					context.varToValue.put((tRef as VarRef).variable, ((tRef as VarRef).variable as Action).actionBufferedValues.removeFirst() as Integer)
				}
				if (DebugLevel.level > 1) println(' in reaction, '+(tRef as VarRef).variable.name+ '=' +(tRef as VarRef).variable.currentValue)
			}
		}
		
		var String returnStatement = '\n return ['
		var String sep = ""	
		for(StateVar sv : (_self.eContainer as Reactor).stateVars) {
			binding.setVariable(sv.name, sv.currentStateValue)
			returnStatement = returnStatement + sep + sv.name
			sep=","
		}
		returnStatement = returnStatement + ']'
							
		val ucl = ReactionAspect.classLoader
		val shell = new GroovyShell(ucl,binding)
		print(Colors.BG_YELLOW)
		var res = shell.evaluate(lfGroovyFunctions+ 
								_self.code.body +
								returnStatement)
				as ArrayList<Object>
		print(Colors.RESET)

		for(VarRef vRef : _self.sources + _self.effects) {
			if (context.newSchedules.containsKey(vRef.variable)){
				(vRef.variable as Action).nextSchedule = context.newSchedules.get(vRef.variable) as Integer *1000 //TODO: use unit
				if (DebugLevel.level > 1) println("#########  scheduled in reaction :"+vRef.variable.name+" in "+ (vRef.variable as Action).nextSchedule)
			}else{
				if (vRef.variable instanceof Action) {
					vRef.variable.currentValue = null
				}
			}
			if (context.outAssignements.containsKey(vRef.variable)){
				vRef.variable.currentValue = context.outAssignements.get(vRef.variable)
				if (DebugLevel.level > 1) println("#########  assigned in reaction  "+vRef.variable.name+" = "+ vRef.variable.currentValue)
			}else{
				vRef.variable.currentValue = null
			}
		}	
		var i = 0
		for(StateVar sv : (_self.eContainer as Reactor).stateVars) {
			sv.currentStateValue = res.get(i++) as Integer 
		}

		for(TriggerRef tRef : _self.triggers) { 
			if (tRef instanceof VarRef && (tRef as VarRef).variable instanceof Port){
				(tRef as VarRef).variable.currentValue = null
			}
		}
		
	}
}
