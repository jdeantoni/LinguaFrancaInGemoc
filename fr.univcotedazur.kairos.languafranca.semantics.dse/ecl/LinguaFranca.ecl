import 'platform:/resource/org.icyphy.linguafranca/model/generated/LinguaFranca.ecore' --'http://www.icyphy.org/LinguaFranca'
import 'http://www.eclipse.org/emf/2002/Ecore'

ECLimport "platform:/plugin/fr.inria.aoste.timesquare.ccslkernel.model/ccsllibrary/kernel.ccslLib"
ECLimport "platform:/plugin/fr.inria.aoste.timesquare.ccslkernel.model/ccsllibrary/CCSL.ccslLib"
ECLimport "platform:/resource/fr.univcotedazur.kairos.linguafranca.semantics.moclib/mocc/LinguaFrancaUtils.moccml"





/**
 * TODO: 
 *    deal with timer offset
 *    correct semantics of multiple inputs for reaction ?
 */






package ecore --hack to retrieve QVTo facilities
	context EObject
		def : allSubobjects() : Collection(EObject) = self.eAllContents().oclAsType(Collection(EObject))
endpackage


package linguaFranca
 

context Model
	def : timeJump : Event = self.timeJump()
	def : untimedAction : Event = self
	def : allTimerAndActionRelease : Event = self
	def : allCanRelease : Event = self
	def : allWait : Event = self
	
--	def : allTimedElement

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
	
context Timer
	def : starts : Event = self.schedule() --coincides with update since Action is a Variable
	def : releases : Event = self.release()
	def : wait : Event = self
	def if(true) : canRelease : Event = self.canTick()[res] 
			       switch case (res = true) force releases; -- on Model.allInstances()->asSequence()->first().oclAsType(Model).timeJump ; 
					      case (res = false) force wait; --until canRelease; 
context Action
	def : starts : Event = self.schedule() --coincides with update since Action is a Variable
	def : releases  : Event = self.release()
	def if(self.minDelay <> null and self.minDelay.time <> null) : wait : Event = self
	def if(self.minDelay <> null and self.minDelay.time <> null) : canRelease : Event = self.canTick()[res] 
														      switch case (res = true) force releases; -- on Model.allInstances()->asSequence()->first().oclAsType(Model).timeJump ; 
																     case (res = false) force wait; --until canRelease; 

context Connection
	def if(self.delay <> null and self.delay.time <> null) : starts : Event = self.schedule() --coincides with update since Action is a Variable
	def if(self.delay <> null and self.delay.time <> null) : releases  : Event = self.release()
	def if(self.delay <> null and self.delay.time <> null) : wait : Event = self
	def if(self.delay <> null and self.delay.time <> null) : canRelease : Event = self.canTick()[res] 
														      switch case (res = true) force releases; -- on Model.allInstances()->asSequence()->first().oclAsType(Model).timeJump ; 
																     case (res = false) force wait; --until canRelease; 


/**
 * PRIORITY
 */

context Model
	inv untimedPriorTimed:
		Prior : self.untimedAction prevails on self.timeJump
	inv eitherTimedOrUntimed:
		Relation Exclusion(self.timeJump, self.untimedAction)
		
	inv releasePriorUntimed:
		Prior : self.allTimerAndActionRelease prevails on self.untimedAction
	inv untimedPriorTimed2Exclusion:
		Relation Exclusion(self.untimedAction, allTimerAndActionRelease)
		
	inv canReleasePriorUntimed:
		Prior : self.allCanRelease prevails on self.allTimerAndActionRelease
	inv untimedPriorTimed3Exclusion:
		Relation Exclusion(self.untimedAction, allCanRelease)
 
/**BEGIN For priority propagation (see paper@RIVF019 */
 
context Reaction
	def : theModel : Model = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Model))->asSequence()->first().oclAsType(Model)
	inv propagatesPrio1:
		Relation SubClock(self.synchronousExecution, theModel.untimedAction)

context Port
	def : theModel : Model = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Model))->asSequence()->first().oclAsType(Model)
	inv propagatesPrio2:
		Relation SubClock(self.updates, theModel.untimedAction)

context Instantiation
	def : theModel : Model = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Model))->asSequence()->first().oclAsType(Model)
	inv propagatesPrio3:
		Relation SubClock(self.startExecuting, theModel.untimedAction)
	inv propagatesPrio4:
		Relation SubClock(self.finishExecuting, theModel.untimedAction)

context Timer
	def : theModel : Model = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Model))->asSequence()->first().oclAsType(Model)
	inv propagatesPrio5:
		Relation SubClock(self.starts, theModel.untimedAction)
	inv propagatesPrio7:
		Relation SubClock(self.canRelease, theModel.allCanRelease)
	inv propagatesPrio9:
		Relation SubClock(self.wait, theModel.allWait)

context Action
	def : theModel : Model = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Model))->asSequence()->first().oclAsType(Model)
	inv propagatesPrio6:
		Relation SubClock(self.starts, theModel.untimedAction)
 	inv propagatesPrio8:
 	(self.minDelay <> null and self.minDelay.time <> null) implies
		Relation SubClock(self.canRelease, theModel.allCanRelease)
	inv propagatesPrio10:
	(self.minDelay <> null and self.minDelay.time <> null) implies
		Relation SubClock(self.wait, theModel.allWait)
		
context Connection --only timed ones
	def : theModel : Model = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Model))->asSequence()->first().oclAsType(Model)
	inv propagatesPrio11:
	(self.delay <> null and self.delay.time <> null) implies
		Relation SubClock(self.starts, theModel.untimedAction)
 	inv propagatesPrio12:
 	(self.delay <> null and self.delay.time <> null) implies
		Relation SubClock(self.canRelease, theModel.allCanRelease)
	inv propagatesPrio13:
	(self.delay <> null and self.delay.time <> null) implies
		Relation SubClock(self.wait, theModel.allWait)

/**BEGIN For priority propagation (see paper@RIVF2019 */


/** 
 * TIMED ELEMENTS
 */ 

context Model
	/**
	 * state space shrinking
	 */
	 def : allTimers : Collection(Timer) = self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Timer)).oclAsType(Timer)
	 def : allActions : Collection(Action) = self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Action)).oclAsType(Action)
	 def : allTimedConnections : Collection(Connection) = self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Connection) and eo.oclAsType(Connection).delay <> null and eo.oclAsType(Connection).delay.time <> null).oclAsType(Connection)

	 
	 inv onlyOneCanTickAtATime1:
		Relation Exclusion(allTimers.canRelease)	
		
	 inv onlyOneCanTickAtATime2:
		Relation Exclusion(allActions.canRelease)	
	
	 inv onlyOneCanTickAtATime3:
	 (allActions->size() > 0) implies 
	 	let allTimerCanTick : Event = Expression Union(allTimers.canRelease) in
		let allActionCanTick : Event = Expression Union(allActions.canRelease) in
		Relation Exclusion(allTimerCanTick, allActionCanTick)	
		
		
	 inv onlyOneReleaseAtATime1:
		Relation Exclusion(allTimers.releases)	
		
	 inv onlyOneReleaseAtATime2:
	  (allActions->size() > 0) implies
		Relation Exclusion(allActions.releases)	
	
		
	 inv onlyOneReleaseAtATime3:
  	 (allActions->size() > 0) implies
	 	let allTimerRelease : Event = Expression Union(allTimers.releases) in
	 	let allActionRelease : Event = Expression Union(allActions.releases) in
		Relation Exclusion(allTimerRelease, allActionRelease)	
	
	inv onlyOneWaitAtATime1:
		Relation Exclusion(allTimers.wait)	
	
	inv onlyOneWaitAtATime2:
		Relation Exclusion(allActions.wait)	
		
	 inv onlyOneWaitAtATime3:
	 (allActions->size() > 0) implies
	 	let allTimerWait : Event = Expression Union(allTimers.wait) in
	 	let allActionWait : Event = Expression Union(allActions.wait) in
		Relation Exclusion(allTimerWait, allActionWait)	
	

	inv canReleasePriorRelease:
		Prior : self.allCanRelease prevails on self.allTimerAndActionRelease					
	inv canReleasePriorReleaseExclusion:
		Relation Exclusion(self.allCanRelease, self.allTimerAndActionRelease)
	
	inv canReleasePriorWait:
		Prior : self.allCanRelease prevails on self.allWait						
	inv canReleasePriorWaitExclusion:
		Relation Exclusion(self.allCanRelease, self.allWait)
	
	 /**
	  * end state space shrinking
	  */

--	in the following defs, check if all are defined, i.e. in Distributed.lf, there is no Actions

 	inv defUntimedAction:
 		(allActions->size() > 0) implies
 		let allSynchronousExecution: Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Reaction)).oclAsType(Reaction).synchronousExecution) in
		let allUpdates: Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Port)).oclAsType(Port).updates) in
		let allStartExecution : Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Instantiation)).oclAsType(Instantiation).startExecuting) in
		let allFinishExecution : Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Instantiation)).oclAsType(Instantiation).finishExecuting) in
		let allTimerStarts: Event = Expression Union(allTimers.starts) in
		let allActionStarts: Event = Expression Union(allActions.starts) in
		let union1_2 : Event = Expression Union(allSynchronousExecution, allUpdates) in
		let union1_2_3 : Event = Expression Union(union1_2, allStartExecution) in
		let union1_2_3Prime : Event = Expression Union(union1_2_3, allFinishExecution) in
		let union1_2_3_4 : Event = Expression Union(union1_2_3Prime, allTimerStarts) in
		let union1_2_3_4_5 : Event = Expression Union(union1_2_3_4, allActionStarts) in
		Relation Coincides(self.untimedAction, union1_2_3_4_5)
		
	inv defUntimedActionNoAction:
 		(allActions->size()=  0) implies
 		let allSynchronousExecution2: Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Reaction)).oclAsType(Reaction).synchronousExecution) in
		let allUpdates2: Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Port)).oclAsType(Port).updates) in
		let allStartExecution2 : Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Instantiation)).oclAsType(Instantiation).startExecuting) in
		let allFinishExecution2 : Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Instantiation)).oclAsType(Instantiation).finishExecuting) in
		let allTimerStarts2: Event = Expression Union(allTimers.starts) in
		let allTimedConnectionStarts2: Event = Expression Union(allTimedConnections.starts) in
		let union21_2 : Event = Expression Union(allSynchronousExecution2, allUpdates2) in
		let union21_2_3 : Event = Expression Union(union21_2, allStartExecution2) in
		let union21_2_3Prime : Event = Expression Union(union21_2_3, allFinishExecution2) in
		let union21_2_3_4 : Event = Expression Union(union21_2_3Prime, allTimerStarts2) in
		let union21_2_3_4_5 : Event = Expression Union(union21_2_3_4, allTimedConnectionStarts2) in
		Relation Coincides(self.untimedAction, union21_2_3_4_5)
		
		
	inv defAllCanRelease:
	(allActions->size() > 0) implies
		let _allActionCanRelease1 : Event = Expression Union(allActions.canRelease) in
		let _allTimerCanRelease1 : Event = Expression Union(allTimers.canRelease) in
		let _allTimerAndActionCanRelease1 : Event = Expression Union(_allActionCanRelease1,_allTimerCanRelease1) in
		Relation Coincides(_allTimerAndActionCanRelease1,allCanRelease)
		
	inv defAllCanReleaseNoAction:
	(allActions->size() = 0) implies
		let _allTimerCanRelease2 : Event = Expression Union(allTimers.canRelease) in
		let _allConnectionCanRelease2 : Event = Expression Union(allTimedConnections.canRelease) in
		let _allTimerAndConnectionCanRelease2 : Event = Expression Union(_allTimerCanRelease2,_allConnectionCanRelease2) in
		Relation Coincides(_allTimerAndConnectionCanRelease2,allCanRelease)

	inv defAllTimerAndActionRelease:
	(allActions->size() > 0) implies
		let _allActionRelease1 : Event = Expression Union(allActions.releases) in
		let _allTimerRelease1 : Event = Expression Union(allTimers.releases) in
		let _allTimerAndActionRelease1 : Event = Expression Union(_allActionRelease1,_allTimerRelease1) in
		Relation Coincides(_allTimerAndActionRelease1,allTimerAndActionRelease)
		
	inv defAllTimerAndActionReleaseNoAction:
	(allActions->size() = 0) implies
		let _allTimerRelease2 : Event = Expression Union(allTimers.releases) in
		let _allConnectionRelease2 : Event = Expression Union(allTimedConnections.releases) in
		let _allTimerAndConnectionRelease2 : Event = Expression Union(_allTimerRelease2,_allConnectionRelease2) in
		Relation Coincides(self.allTimerAndActionRelease,_allTimerAndConnectionRelease2 ) 
 	
 	inv defAllWait:
 	(allActions->size() > 0) implies
 		let allTimersWait : Event = Expression Union(allTimers.wait) in
	 	let allActionsWait : Event = Expression Union(allActions.wait) in
	 	let allAandTWait : Event = Expression Union(allTimersWait, allActionsWait) in
		Relation Coincides(self.allWait, allAandTWait)	
	
	inv defAllWaitNoAction:
 	(allActions->size() = 0) implies
 		let allTimersWait2 : Event = Expression Union(allTimers.wait) in
 		let _allConnectionWait2 : Event = Expression Union(allTimedConnections.wait) in
		let _allTimerAndConnectionRelease : Event = Expression Union(allTimersWait2,_allConnectionWait2) in
		Relation Coincides(self.allWait, _allTimerAndConnectionRelease)	
		
context Timer
	def : theModel : Model = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Model))->asSequence()->first().oclAsType(Model)


	inv timerStartsWhenUpdates:
		Relation Coincides(self.updates, self.starts)
		 
	inv timerWaitOrRelease:
	    Relation Exclusion(self.wait, self.releases)

	inv timerSetActionLifeCycle:
		let nbInputs : Integer = 1 in --timers always have a single input (actually 0 but a single start by timejump
		Relation TimerConstraint(self.starts, self.canRelease, self.wait, self.releases, theModel.timeJump, nbInputs) 
		
context Action
	def : theModel : Model = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Model))->asSequence()->first().oclAsType(Model)


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
	Relation AlternatesFSM(source.synchronousExecution, target.synchronousExecution)
	 
	inv ActionSourceBeforeTarget:  
	(self.minDelay <> null and self.minDelay.time <> null) implies
	let source : Reaction = self.oclAsType(ecore::EObject).eContainer().oclAsType(Reactor).reactions
				->select(r | r.effects->first().variable.name = self.name)->first()   in
	let target : Reaction = self.oclAsType(ecore::EObject).eContainer().oclAsType(Reactor).reactions
				->select(r | r.triggers->first().oclIsKindOf(VarRef) and r.triggers.oclAsType(VarRef)->first().variable.name = self.name)->first()   in
	Relation AlternatesFSM(source.synchronousExecution, target.synchronousExecution)
	
	inv ActionTargetWhenReleased:  
	(self.minDelay <> null and self.minDelay.time <> null) implies
	let target : Reaction = self.oclAsType(ecore::EObject).eContainer().oclAsType(Reactor).reactions
				->select(r | r.triggers->first().oclIsKindOf(VarRef) and r.triggers.oclAsType(VarRef)->first().variable.name = self.name)->first()   in
	Relation AlternatesFSM(self.releases, target.synchronousExecution)
	 
	/**
	 * time related stuff
	 */
	inv setActionLifeCycle: 
	let theReactor : Reactor = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Reactor))->asSequence()->first().oclAsType(Reactor) in
	let nbInputs : Integer = theReactor.inputs->size() in
	(self.minDelay <> null and self.minDelay.time <> null) implies
		Relation TimerConstraint(self.starts, self.canRelease, self.wait, self.releases, theModel.timeJump, nbInputs) 

	inv setNullLifeCycle:
	(self.minDelay = null or self.minDelay.time = null) implies
		Relation AlternatesFSM(self.starts, self.releases) 
		
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
		
		
		
-- start warning ! it should not alternate here since we can have inputs at different steps
		
	inv startComputeInputOnly:
		(    (type.timers->size() = 0)
		 and (type.inputs->size() > 0)
		) implies
		let allInputUpdates : Event = Expression Sup(type.inputs.updates) in						--WARNING ! was Union. Just a test here ! KIt works but forces synchronization that should not be forced this way. With Union that diverges towards a deadlock
		Relation AlternatesFSM(allInputUpdates, self.startExecuting)

	inv startComputeInputAndTimer:
		(    (type.timers->size() > 0)
		 and (type.inputs->size() > 0)
		) implies
		let allTimers2 : Event = Expression Union(type.timers.updates) in
		let allInputUpdates2 : Event = Expression Union(type.inputs.updates) in						
		let allTriggers : Event = Expression Union(allTimers2, allInputUpdates2) in
		Relation AlternatesFSM(allTriggers, self.startExecuting)



--> sans le non reentrant on casse le pipeline -> no...


--	inv nonRentrant:
--		Relation AlternatesFSM(self.startExecuting, self.finishExecuting)
--stop warning stop warning stop warning stop warning
		
		
	inv startInternalReactionWhenStart:
		(type.reactions->size() > 0 and type.timers->size() = 0) implies
		let firstReactionExec : Event = Expression Inf(type.reactions.synchronousExecution) in
		Relation AlternatesFSM(self.startExecuting, firstReactionExec)
	
	inv FinishInternalReactionWhenFinish:
		(type.reactions->size() > 0) implies
		let lastReactionExec : Event = Expression Sup(type.reactions.synchronousExecution) in
		Relation Coincides(lastReactionExec, self.finishExecuting)
	 
context Connection 
	def : theModel : Model = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Model))->asSequence()->first().oclAsType(Model)

	inv ConnectorSourceBeforeTarget:
		(self.delay = null or self.delay.time = null) implies
		Relation Coincides(self.leftPorts.variable->first().updates, self.rightPorts.variable->first().updates)
		
	inv ConnectorSourceBeforeTargetTimed:
		(self.delay <> null and self.delay.time <> null) implies
		let nbInputs : Integer = 1 in --timers always have a single input (actually 0 but a single start by timejump
		Relation TimerConstraint(self.starts, self.canRelease, self.wait, self.releases, theModel.timeJump, nbInputs) 
		
	inv TimedConnectorStartsWithSource:
		(self.delay <> null and self.delay.time <> null) implies
		Relation Coincides(self.starts, self.leftPorts.variable->first().updates)
		
	inv TimedConnectorReleaseWithTarget:
		(self.delay <> null and self.delay.time <> null) implies
		Relation Precedes(self.releases, self.rightPorts.variable->first().updates)

context Reaction  --we consider that we have either input or timer but not a mixed of them
				  --we consider a reaction can be triggered only by one timer.
				  --we consider that triggers are always VarRefs
				 
	inv startWhenTheOnlyInputArrivedNoTimer:
		let allInputsNoTimer : Sequence(Variable) = self.triggers.oclAsType(VarRef).variable->select(t|t.oclIsKindOf(Variable) and (not t.oclIsKindOf(Timer)) and (not t.oclIsKindOf(Action))) in
		(allInputsNoTimer->size() = 1) implies --check this condition that seems not sufficient
		Relation AlternatesFSM(allInputsNoTimer->first().updates, self.synchronousExecution)

	inv startWhenTheLastInputArrivedNoTimer:
		let allInputsNoTimer : Sequence(Variable) = self.triggers.oclAsType(VarRef).variable->select(t|t.oclIsKindOf(Variable) and (not t.oclIsKindOf(Timer)) and (not t.oclIsKindOf(Action))) in
		(allInputsNoTimer->size() > 1) implies --check this condition that seems not sufficient
		let lastInput : Event = Expression Sup(allInputsNoTimer.updates) in
		Relation AlternatesFSM(lastInput, self.synchronousExecution)
	
	inv startWhenTheTimerRelease:
		(self.triggers.oclAsType(VarRef).variable->select(t| t.oclIsKindOf(Timer))->size() > 0) implies
		Relation AlternatesFSM(self.triggers.oclAsType(VarRef).variable->select(t| t.oclIsKindOf(Timer))->first().oclAsType(Timer).releases, self.synchronousExecution)	
	
	inv startWhenTheActionRelease:
		(self.triggers.oclAsType(VarRef).variable->select(t| t.oclIsKindOf(Action))->size() > 0) implies
		Relation AlternatesFSM(self.triggers.oclAsType(VarRef).variable->select(t| t.oclIsKindOf(Action))->first().oclAsType(Action).releases, self.synchronousExecution)	
	
	
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