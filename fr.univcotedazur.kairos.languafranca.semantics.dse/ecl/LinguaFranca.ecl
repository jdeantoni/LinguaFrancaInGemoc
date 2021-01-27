import 'platform:/resource/org.icyphy.linguafranca/model/generated/LinguaFranca.ecore' --'http://www.icyphy.org/LinguaFranca'
import 'http://www.eclipse.org/emf/2002/Ecore'

ECLimport "platform:/plugin/fr.inria.aoste.timesquare.ccslkernel.model/ccsllibrary/kernel.ccslLib"
ECLimport "platform:/plugin/fr.inria.aoste.timesquare.ccslkernel.model/ccsllibrary/CCSL.ccslLib"
ECLimport "platform:/resource/fr.univcotedazur.kairos.linguafranca.semantics.moclib/mocc/LinguaFrancaUtils.moccml"






/**
 * TODO: 
 *    deal correctly with timer (auto restart)
 *    deal with timer offset
 *    correct semantics of multiple inputs for reaction
 *    add priority between untimed and timed
 */












package ecore --ack to retrieve QVTo facilities
	context EObject
		def : allSubobjects() : Collection(EObject) = self.eAllContents().oclAsType(Collection(EObject))
endpackage

--package ocl
--	context AnyType --ack to retrieve QVTo facilities
--		def : allSubobjects() : Collection(ecore::EObject) = self.oclAsType(ecore::EObject).eAllContents().oclAsType(Collection(ecore::EObject))	
--endpackage

package linguaFranca
 

context Model
	def: timeJump : Event = self.timeJump()
	def : untimedAction : Event = self
	def : allTimerAndActionRelease : Event = self
	def : allCanRelease : Event = self

context Instantiation
	def : startExecuting: Event = self   
	def : finishExecuting: Event = self 
	
context Reactor
	def if(self._main) : startExecuting: Event = self   
	def if(self._main) : finishExecuting: Event = self 

context Reaction
	def: synchronousExecution: Event = self 
	
context Variable
	def : updates : Event = self
	
-- not needed it is a variable, can use update
context Timer
	def : starts : Event = self.schedule() --coincides with update since Action is a Variable
	def : releases : Event = self
	def : wait : Event = self
	def if(true) : canRelease : Event = self.canTick()[res] 
			       switch case (res = true) force releases; -- on Model.allInstances()->asSequence()->first().oclAsType(Model).timeJump ; 
					      case (res = false) force wait; --until canRelease; 
context Action
	def : starts : Event = self.schedule() --coincides with update since Action is a Variable
	def : releases  : Event = self
	def if(self.minDelay <> null and self.minDelay.time <> null) : wait : Event = self
	def if(self.minDelay <> null and self.minDelay.time <> null) : canRelease : Event = self.canTick()[res] 
														      switch case (res = true) force releases; -- on Model.allInstances()->asSequence()->first().oclAsType(Model).timeJump ; 
																     case (res = false) force wait; --until canRelease; 


/**
 * PRIORITY
 */

context Model
	inv untimedPriorTimed:
		Prior : self.allCanRelease  prevails on self.timeJump 
	inv untimedPriorTimedExclusion:
		Relation Exclusion(self.allCanRelease, self.timeJump)

	inv ForUntimedPriorTimed2:
		let _allActionCanRelease : Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Action)).oclAsType(Action).canRelease) in
		let _allTimerCanRelease : Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Timer)).oclAsType(Timer).canRelease) in
		let _allTimerAndActionCanRelease : Event = Expression Union(_allActionCanRelease,_allTimerCanRelease) in
		Relation Coincides(_allTimerAndActionCanRelease,allCanRelease)

	inv ForUntimedPriorTimed3:
		let _allActionRelease : Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Action)).oclAsType(Action).releases) in
		let _allTimerRelease : Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Timer)).oclAsType(Timer).releases) in
		let _allTimerAndActionRelease : Event = Expression Union(_allActionRelease,_allTimerRelease) in
		Relation Coincides(_allTimerAndActionRelease,allTimerAndActionRelease)

	inv untimedPriorTimed2:
		Prior : self.untimedAction  prevails on self.allCanRelease
	inv untimedPriorTimed2Exclusion:
		Relation Exclusion(self.untimedAction, self.allCanRelease)

/**
 * TIMED ELEMENTS
 */

context Model
--	inv forFiniteStateSpace1:
--		let firstInternalExec : Event = Expression Inf(self.reactors->select(r | r._main).instantiations.startExecuting) in
--		let lastInternalExec : Event = Expression Sup(self.reactors->select(r | r._main).instantiations.finishExecuting) in
--		Relation AlternatesFSM(firstInternalExec, lastInternalExec) 
	inv timeJumpIfActionRelease:
--		let allActionRelease : Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Action)).oclAsType(Action).releases) in
--		let allTimerRelease : Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Timer)).oclAsType(Timer).releases) in
--		let allTimerAndActionRelease : Event = Expression Union(allActionRelease,allTimerRelease) in
		Relation Coincides(self.timeJump, allTimerAndActionRelease)
 
 	inv allUntimedAction:
 		let allSynchronousExecution: Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Reaction)).oclAsType(Reaction).synchronousExecution) in
		let allUpdates: Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Port)).oclAsType(Port).updates) in
		let allStartExecution : Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Instantiation)).oclAsType(Instantiation).startExecuting) in
		--let allTimerCanRelease: Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Timer)).oclAsType(Timer).canRelease) in
		--let allActionCanRelease: Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Action)).oclAsType(Action).canRelease) in
		let allTimerStarts: Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Timer)).oclAsType(Timer).starts) in
		let allActionStarts: Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Action)).oclAsType(Action).starts) in
		let union1_2 : Event = Expression Union(allSynchronousExecution, allUpdates) in
		let union1_2_3 : Event = Expression Union(union1_2, allStartExecution) in
		--let union1_2_3_4 : Event = Expression Union(union1_2_3, allTimerCanRelease) in
		--let union1_2_3_4_5 : Event = Expression Union(union1_2_3_4, allActionCanRelease) in
		let union1_2_3_4_5_6 : Event = Expression Union(union1_2_3, allTimerStarts) in
		let union1_2_3_4_5_6_7 : Event = Expression Union(union1_2_3_4_5_6, allActionStarts) in
		Relation Coincides(self.untimedAction, union1_2_3_4_5_6_7)
		
	inv eitherTimedOrUntimed:
		Relation Exclusion(self.timeJump, self.untimedAction)
 	
context Timer
	inv timerStartsWhenUpdates:
		Relation Coincides(self.updates, self.starts)
		 
	inv timerWaitOrRelease:
	    Relation Exclusion(self.wait, self.releases)

	inv timerSetActionLifeCycle:
		Relation TimerConstraint(self.starts, self.canRelease, self.wait, self.releases) 
		
context Action
	inv actionStartsWhenUpdates:
		Relation Coincides(self.updates, self.starts) 

	inv actionWaitOrRelease:
	(self.minDelay <> null and self.minDelay.time <> null) implies
		Relation Exclusion(self.wait, self.releases) 
	/**
	 * causality stuff
	 */

	inv ActionSourceCoincidesTarget:  
	(self.minDelay = null or self.minDelay.time = null) implies
	let source : Reaction = self.oclAsType(ecore::EObject).eContainer().oclAsType(Reactor).reactions
				->select(r | r.effects->first().variable.name = self.name)->first()   in
	let target : Reaction = self.oclAsType(ecore::EObject).eContainer().oclAsType(Reactor).reactions
				->select(r | r.triggers->first().oclIsKindOf(VarRef) and r.triggers.oclAsType(VarRef)->first().variable.name = self.name)->first()   in
	Relation Precedes(source.synchronousExecution, target.synchronousExecution)
	 
	inv ActionSourceBeforeTarget:  
	(self.minDelay <> null and self.minDelay.time <> null) implies
	let source : Reaction = self.oclAsType(ecore::EObject).eContainer().oclAsType(Reactor).reactions
				->select(r | r.effects->first().variable.name = self.name)->first()   in
	let target : Reaction = self.oclAsType(ecore::EObject).eContainer().oclAsType(Reactor).reactions
				->select(r | r.triggers->first().oclIsKindOf(VarRef) and r.triggers.oclAsType(VarRef)->first().variable.name = self.name)->first()   in
	Relation Precedes(source.synchronousExecution, target.synchronousExecution)
	
	inv ActionTargetWhenReleased:  
	(self.minDelay <> null and self.minDelay.time <> null) implies
	let target : Reaction = self.oclAsType(ecore::EObject).eContainer().oclAsType(Reactor).reactions
				->select(r | r.triggers->first().oclIsKindOf(VarRef) and r.triggers.oclAsType(VarRef)->first().variable.name = self.name)->first()   in
	Relation Precedes(self.releases, target.synchronousExecution)
	 
	/**
	 * time related stuff
	 */
	inv setActionLifeCycle:
	(self.minDelay <> null and self.minDelay.time <> null) implies
		Relation TimerConstraint(self.starts, self.canRelease, self.wait, self.releases) 
		
	inv setNullLifeCycle:
	(self.minDelay = null or self.minDelay.time = null) implies
		Relation Precedes(self.starts, self.releases) 
		
/**
 * UNTIMED ELEMENTS
 */

context Reactor
	def : theModel : Model = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Model))->asSequence()->first().oclAsType(Model)
	def : instancesWithNoInput: Collection(Instantiation) = self.instantiations->select(
						i | theModel.oclAsType(ecore::EObject).allSubobjects()->select(r | r.oclIsKindOf(Reactor) and r.oclAsType(Reactor).name = i.reactorClass.name)->asSequence()->first().oclAsType(Reactor).inputs->size() = 0
						).oclAsType(Instantiation)
	
	inv mainReactorStartNoInputInstanceWhenStartsPart1:
	 (self._main and instancesWithNoInput->size() > 1) implies
		Relation Coincides(instancesWithNoInput.startExecuting)
	
	inv mainReactorStartNoInputInstanceWhenStartsPart2:
	 (self._main) implies
		Relation Coincides(instancesWithNoInput->asSequence()->first().startExecuting, self.startExecuting)
	
	inv mainReactorNeverFinish:
	 (self._main) implies
		Relation NeverTick(self.finishExecuting)


context Instantiation 
	def : theModel : Model = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Model))->asSequence()->first().oclAsType(Model)
 	def : type : Reactor = theModel.oclAsType(ecore::EObject).allSubobjects()->select(r | r.oclIsKindOf(Reactor) and r.oclAsType(Reactor).name = self.reactorClass.name).oclAsType(Reactor)->asSequence()->first()
	
	inv startComputeTimerOnly:
		(    (type.timers->size() > 0)
		 and (type.inputs->size() = 0)	
		) implies
		let allTimers : Event = Expression Union(type.timers.starts) in
		Relation Coincides(self.startExecuting, allTimers)
		
	inv startComputeInputOnly:
		(    (type.timers->size() = 0)
		 and (type.inputs->size() > 0)
		) implies
		let allInputUpdates : Event = Expression Union(type.inputs.updates) in
		Relation Precedes(allInputUpdates, self.startExecuting)

	inv startComputeInputAndTimer:
		(    (type.timers->size() > 0)
		 and (type.inputs->size() > 0)
		) implies
		let allTimers2 : Event = Expression Union(type.timers.updates) in
		let allInputUpdates2 : Event = Expression Union(type.inputs.updates) in
		let allTriggers : Event = Expression Union(allTimers2, allInputUpdates2) in
		Relation Precedes(allTriggers, self.startExecuting)
		
	inv startInternalReactionWhenStart:
		(type.reactions->size() > 0 and type.timers->size() = 0) implies
		let firstReactionExec : Event = Expression Inf(type.reactions.synchronousExecution) in
		Relation Precedes(self.startExecuting, firstReactionExec)
	
	inv FinishInternalReactionWhenFinish:
		(type.reactions->size() > 0) implies
		let lastReactionExec : Event = Expression Sup(type.reactions.synchronousExecution) in
		Relation Coincides(lastReactionExec, self.finishExecuting)
	 
context Connection 
	inv ConnectorSourceBeforeTarget:
		Relation Precedes(self.leftPorts.variable->first().updates, self.rightPorts.variable->first().updates)


context Reaction  --we consider that we have either input or timer but not a mixed of them
				  --we consider a reaction can be triggered only by one timer.
				  --we consider that triggers are always VarRefs
				  
	inv startWhenAnyInputArrivedNoTimer:
		(self.triggers.oclAsType(VarRef).variable->select(t|t.oclIsKindOf(Variable) and (not t.oclIsKindOf(Timer)) and (not t.oclIsKindOf(Action)))->size() > 0) implies
		let anyInput : Event = Expression Union(self.triggers.oclAsType(VarRef).variable->select(t|t.oclIsKindOf(Variable) and (not t.oclIsKindOf(Timer)) and (not t.oclIsKindOf(Action))).oclAsType(Variable).updates) in
		Relation Precedes(anyInput, self.synchronousExecution)
	
	inv startWhenTheTimerRelease:
		(self.triggers.oclAsType(VarRef).variable->select(t| t.oclIsKindOf(Timer))->size() > 0) implies
		Relation Precedes(self.triggers.oclAsType(VarRef).variable->select(t| t.oclIsKindOf(Timer))->first().oclAsType(Timer).releases, self.synchronousExecution)	
	
	inv startWhenTheActionRelease:
		(self.triggers.oclAsType(VarRef).variable->select(t| t.oclIsKindOf(Action))->size() > 0) implies
		Relation Precedes(self.triggers.oclAsType(VarRef).variable->select(t| t.oclIsKindOf(Action))->first().oclAsType(Action).releases, self.synchronousExecution)	
	
	
	inv UpdatesOutVarAllTogether:
		(self.effects->size() > 1) implies
		Relation Coincides(self.effects.variable.updates)
	
	inv UpdatesOutVarOnExecute: -- for now consider a single effect 
		(self.effects->size()  > 0
--			and
--		 not self.effects->first().oclIsKindOf(Action)
		) implies
		Relation Coincides(self.effects.variable->first().updates, self.synchronousExecution)
	
--	inv startOutTimedActionOnExecute: -- for now consider a single effect
--		(self.effects->size()  > 0
--			and
--		 self.effects->first().oclIsKindOf(Action)
--		) implies
--		Relation Coincides(self.effects.variable->first().oclAsType(Action).updates, self.synchronousExecution)

endpackage