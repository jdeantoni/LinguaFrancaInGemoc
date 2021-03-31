import 'platform:/resource/org.icyphy.linguafranca/model/generated/LinguaFranca.ecore' --'http://www.icyphy.org/LinguaFranca'
import 'http://www.eclipse.org/emf/2002/Ecore'

ECLimport "platform:/plugin/fr.inria.aoste.timesquare.ccslkernel.model/ccsllibrary/kernel.ccslLib"
ECLimport "platform:/plugin/fr.inria.aoste.timesquare.ccslkernel.model/ccsllibrary/CCSL.ccslLib"
ECLimport "platform:/resource/fr.univcotedazur.kairos.linguafranca.semantics.moclib/mocc/LinguaFrancaUtils.moccml"


 


/**
 * TODO: 
 *    correct semantics of multiple inputs for reaction ?
 *    add support for systems with no timed elements
 *    add support for link between logical and physical time
 */



package ecore --trick to retrieve QVTo facilities
	context EObject
		def : allSubobjects() : Collection(EObject) = self.eAllContents().oclAsType(Collection(EObject))
endpackage






package linguaFranca
 
/**
 * Domain Specific Event definitions
 */
context Model
	def : timeJump : Event(StartEvent) = self.timeJump()
	def : untimedAction : Event = self
	
	def : allRelease : Event = self
	def : allCanRelease : Event = self
	def : allWait : Event = self

context Reaction
	def: synchronousExecution: Event(produceEvent) = self.exec()

context Variable
	def : updates : Event(produceEvent) = self
	
context TriggerRef
	def if (not self.oclIsKindOf(VarRef) and self.startup): starts : Event(produceEvent) = self 
	
	
context TimedConcept
	def 
	if(	(self.oclIsKindOf(Timer))
		or  
		(self.oclIsKindOf(Action))
		or
		(self.oclIsKindOf(Connection) and self.oclAsType(Connection).delay <> null and self.oclAsType(Connection).delay <> null)
	  ): starts : Event(produceEvent) = self.schedule() --coincides with update since Action is a Variable
	def
	if( (self.oclIsKindOf(Timer))
		or 
		(self.oclIsKindOf(Action))
		or
		(self.oclIsKindOf(Connection) and self.oclAsType(Connection).delay <> null and self.oclAsType(Connection).delay <> null)
	  ): releases : Event(StartEvent) = self.release()	  
	def 
	if((self.oclIsKindOf(Timer))
		or
		(self.oclIsKindOf(Action))
		or
		(self.oclIsKindOf(Connection) and self.oclAsType(Connection).delay <> null and self.oclAsType(Connection).delay <> null)
	): wait : Event(FinishEvent) = self
	def 
	if((self.oclIsKindOf(Timer)) 
		or
		(self.oclIsKindOf(Action))
		or
		(self.oclIsKindOf(Connection) and self.oclAsType(Connection).delay <> null and self.oclAsType(Connection).delay <> null)
	): canRelease : Event(FinishEvent) = self.canTick()[res] 
			       						switch 
			       						case (res = true) force releases;
					      				case (res = false) force wait;



/**
 * Domain Specific Constraint Definitions
 */
context Model
	
	 def : allTimedConcepts : Collection(TimedConcept) =
	  self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Timer)).oclAsType(TimedConcept)
				->union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Action)).oclAsType(TimedConcept))
			    ->union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Connection) and eo.oclAsType(Connection).delay <> null).oclAsType(TimedConcept))

--	TODO: the following defs do not work if some collections are empty, i.e. ii there is no timedElements

 	inv defuntimedActionWithStartup:
 		let allStartups1 : Collection(TriggerRef) = self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(TriggerRef) and eo.oclAsType(TriggerRef).startup).oclAsType(TriggerRef) in
 		(allStartups1->size() > 0) implies
 		let allSynchronousExecution1: Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Reaction)).oclAsType(Reaction).synchronousExecution) in
		let allUpdates1: Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Port)).oclAsType(Port).updates) in
		let allTimedConceptsStarts1: Event = Expression Union(allTimedConcepts.starts) in
		let allStartupStarts1: Event = Expression Union(allStartups1.starts) in
		let union1_2a : Event = Expression Union(allUpdates1, allSynchronousExecution1) in
		let union1_2_3a : Event = Expression Union(union1_2a, allTimedConceptsStarts1) in
		let union1_2_3_4a : Event = Expression Union(union1_2_3a, allStartupStarts1) in
		Relation Coincides(self.untimedAction, union1_2_3_4a)
		
	inv defuntimedActionNoStartup:
		let allStartups3 : Sequence(TriggerRef) = self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(TriggerRef) and eo.oclAsType(TriggerRef).startup).oclAsType(TriggerRef)->asSequence() in
 		(allStartups3->size() = 0) implies
 		let allSynchronousExecution3: Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Reaction)).oclAsType(Reaction).synchronousExecution) in
		let allUpdates3: Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Port)).oclAsType(Port).updates) in
		let allTimedConceptsStarts3: Event = Expression Union(allTimedConcepts.starts) in
		let union1_2c : Event = Expression Union(allUpdates3, allSynchronousExecution3) in
		let union1_2_3c : Event = Expression Union(union1_2c, allTimedConceptsStarts3) in
		Relation Coincides(self.untimedAction, union1_2_3c)
	 
		
	inv defAllCanRelease:
		let allTimedConceptCanRelease1 : Event = Expression Union(allTimedConcepts.canRelease) in
		Relation Coincides(allTimedConceptCanRelease1,allCanRelease)
		
	inv defallRelease:
		let allTimedConceptRelease1 : Event = Expression Union(allTimedConcepts.releases) in
		Relation Coincides(allTimedConceptRelease1,allRelease)
	
 	inv defAllWait:
 		let allTimedConceptsWait1 : Event = Expression Union(allTimedConcepts.wait) in
		Relation Coincides(self.allWait, allTimedConceptsWait1)	
	
context Timer
	def : theModel : Model = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Model))->asSequence()->first().oclAsType(Model)

	inv timerStartsWhenUpdates:
		Relation Coincides(self.updates, self.starts)
		 
	inv timerWaitOrRelease:
	    Relation Exclusion(self.wait, self.releases)

	inv timerSetActionLifeCycle:
		let nbInputs : Integer = 1 in --timers always have a single input (actually 0 but a single start by timeJump
		Relation TimerConstraint(self.starts, self.canRelease, self.wait, self.releases, theModel.timeJump, nbInputs) 
		
context Action
	def : theModel : Model = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Model))->asSequence()->first().oclAsType(Model)
 

	inv actionStartsWhenUpdates:
		Relation Coincides(self.updates, self.starts)

	inv actionWaitOrRelease:
	(self.minDelay <> null and self.minDelay.time <> null) implies
		Relation Exclusion(self.wait, self.releases) 
	 
	/**
	 * time related stuff
	 */
	inv setActionLifeCycle: 
	let theReactor : Reactor = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Reactor))->asSequence()->first().oclAsType(Reactor) in
	let nbInputs : Integer = theReactor.inputs->size() in
	(theReactor.inputs->size() > 0) implies
		Relation TimerConstraint(self.starts, self.canRelease, self.wait, self.releases, theModel.timeJump, nbInputs) 

	inv setActionLifeCycleSingleStart: 
	let theReactor : Reactor = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Reactor))->asSequence()->first().oclAsType(Reactor) in
	(theReactor.inputs->size() = 0) implies
	let nbInputs : Integer = self.oclAsType(ecore::EObject).eContainer().oclAsType(Reactor).reactions->select(r | r.effects.oclAsType(VarRef).variable->exists(v | v = self))->size() in
		Relation TimerConstraint(self.starts, self.canRelease, self.wait, self.releases, theModel.timeJump, nbInputs) 
		

context Connection 
	def : theModel : Model = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Model))->asSequence()->first().oclAsType(Model)

	inv ConnectorSourceBeforeTarget:
		(self.delay = null) implies
		Relation Coincides(self.leftPorts.variable->first().updates, self.rightPorts.variable->first().updates)
		
	inv ConnectorSourceBeforeTargetTimed:
		(self.delay <> null) implies
		let nbInputs : Integer = 1 in --connections always have a single input (actually 0 but a single start by timeJump
		Relation TimerConstraint(self.starts, self.canRelease, self.wait, self.releases, theModel.timeJump, nbInputs) 
		
	inv TimedConnectorStartsWithSource:		--does not coincides since it can be activated several times while waiting (see "Anomaly.lf" example)
		(self.delay <> null) implies	
		Relation Precedes(self.leftPorts.variable->first().updates, self.starts)
		
	inv TimedConnectorReleaseWithTarget:
		(self.delay <> null) implies
		Relation AlternatesFSM(self.releases, self.rightPorts.variable->first().updates)   --precedes ?

context Reaction  --we consider that we have either input or timer but not a mixed of them
				  --we consider a reaction can be triggered only by one timer.
				  --we consider that triggers are always VarRefs
		
	def : inputStartups : Sequence(TriggerRef) = self.triggers->select(t|t.oclIsKindOf(TriggerRef) and t.oclAsType(TriggerRef).startup).oclAsType(TriggerRef) 
		 
	inv startWhenTheOnlyInputArrivedNoTimerNoStartup:
		let allInputsNoTimer : Sequence(Variable) = self.triggers->select(t|t.oclIsKindOf(VarRef)).oclAsType(VarRef).variable->select(t|t.oclIsKindOf(Variable) and (not t.oclIsKindOf(Timer)) and (not t.oclIsKindOf(Action))) in
		(allInputsNoTimer->size() = 1 and inputStartups->size() = 0) implies --check this condition that seems not sufficient
		Relation AlternatesFSM(allInputsNoTimer->first().updates, self.synchronousExecution)

	inv startWhenOneInputArrivedNoTimerNoStartup:
		let allInputsNoTimer : Sequence(Variable) = self.triggers->select(t|t.oclIsKindOf(VarRef)).oclAsType(VarRef).variable->select(t|t.oclIsKindOf(Variable) and (not t.oclIsKindOf(Timer)) and (not t.oclIsKindOf(Action))) in
		(allInputsNoTimer->size() > 1 and inputStartups->size() = 0) implies --check this condition that seems not sufficient
		let theInputs : Event = Expression Union(allInputsNoTimer.updates) in
		let nbInputs : Integer = allInputsNoTimer->size() in
		Relation MultipleInputsReaction(theInputs, self.synchronousExecution, nbInputs)
		
	inv startWhenTheOnlyInputArrivedNoTimerWithStartup:
		let allInputsNoTimer : Sequence(Variable) = self.triggers->select(t|t.oclIsKindOf(VarRef)).oclAsType(VarRef).variable->select(t|t.oclIsKindOf(Variable) and (not t.oclIsKindOf(Timer)) and (not t.oclIsKindOf(Action))) in
		(allInputsNoTimer->size() = 1 and inputStartups->size() > 0) implies --check this condition that seems not sufficient
		let theInputAndStartup : Event = Expression Union(allInputsNoTimer->first().updates, inputStartups->first().starts) in
		let deuxInputs : Integer = 2 in
		Relation MultipleInputsReaction(theInputAndStartup, self.synchronousExecution, deuxInputs)

	inv startWhenOneInputArrivedNoTimerWithStartup:
		let allInputsNoTimer : Sequence(Variable) = self.triggers->select(t|t.oclIsKindOf(VarRef)).oclAsType(VarRef).variable->select(t|t.oclIsKindOf(Variable) and (not t.oclIsKindOf(Timer)) and (not t.oclIsKindOf(Action))) in
		(allInputsNoTimer->size() > 1 and inputStartups->size() > 0) implies --check this condition that seems not sufficient
		let theInputs2 : Event = Expression Union(allInputsNoTimer.updates) in
		let theInputsAndTheStartup : Event = Expression Union(theInputs2, inputStartups->first().starts) in
		let nbInputsPlusOne : Integer = (allInputsNoTimer->size() + inputStartups->size()).round() in
		Relation MultipleInputsReaction(theInputsAndTheStartup, self.synchronousExecution, nbInputsPlusOne)
		
	inv startWhenOnlyStartup:
		let allInputs : Collection(TriggerRef) = self.triggers->select(t|t.oclIsKindOf(VarRef)) in
		(allInputs->size() = 0 and inputStartups->size() = 1) implies 
		Relation Coincides(inputStartups->first().starts, self.synchronousExecution) 

	inv startWhenTheTimerReleaseNoStartup:
		((self.triggers->select(t| t.oclIsKindOf(VarRef)).oclAsType(VarRef).variable->select(a | a.oclIsKindOf(Timer)))->size() > 0 and inputStartups->size() = 0) implies
		Relation AlternatesFSM((self.triggers->select(t| t.oclIsKindOf(VarRef)).oclAsType(VarRef).variable->select(a | a.oclIsKindOf(Timer)))->first().oclAsType(Timer).releases, self.synchronousExecution)	

	inv startWhenTheActionReleaseNoStartup:
		((self.triggers->select(t| t.oclIsKindOf(VarRef)).oclAsType(VarRef).variable->select(a | a.oclIsKindOf(Action)))->size() > 0 and inputStartups->size() = 0) implies
		Relation AlternatesFSM((self.triggers->select(t| t.oclIsKindOf(VarRef)).oclAsType(VarRef).variable->select(a | a.oclIsKindOf(Action)))->first().oclAsType(Action).releases, self.synchronousExecution)	
	
	inv startWhenTheTimerReleaseWithStartup:
		((self.triggers->select(t| t.oclIsKindOf(VarRef)).oclAsType(VarRef).variable->select(a | a.oclIsKindOf(Timer)))->size() > 0 and inputStartups->size() > 0) implies
		let theTimerAndStartup : Event = Expression Union((self.triggers->select(t| t.oclIsKindOf(VarRef)).oclAsType(VarRef).variable->select(a | a.oclIsKindOf(Timer)))->first().updates, inputStartups->first().starts) in
		let deuxInputs2 : Integer = 2 in
		Relation MultipleInputsReaction(theTimerAndStartup, self.synchronousExecution, deuxInputs2)

	inv startWhenTheActionReleaseWithStartup:
		((self.triggers->select(t| t.oclIsKindOf(VarRef)).oclAsType(VarRef).variable->select(a | a.oclIsKindOf(Action)))->size() > 0 and inputStartups->size() > 0) implies
		let theActionAndStartup : Event = Expression Union((self.triggers->select(t| t.oclIsKindOf(VarRef)).oclAsType(VarRef).variable->select(a | a.oclIsKindOf(Action)))->first().oclAsType(Action).releases, inputStartups->first().starts) in
		let deuxInputs3 : Integer = 2 in
		Relation MultipleInputsReaction(theActionAndStartup, self.synchronousExecution, deuxInputs3)
	
	
	
	inv UpdatesOutVarAllTogether:
		(self.effects->size() > 1) implies
		Relation Coincides(self.effects.variable.updates)
	
	inv UpdatesOutVarOnExecute:
		(self.effects->size()  > 0) implies
		Relation Coincides(self.synchronousExecution, self.effects.variable->first().updates)


context TriggerRef
	inv startsOnlyOnce:
		(not self.oclIsKindOf(VarRef) and self.startup) implies
		let firstTick : Event = Expression OneTickAndNoMore(self.starts) in 
		Relation Coincides(firstTick, self.starts)











/**
 * PRIORITY
 */ 
 
context Model

	inv untimedPriorTimed: 
		Prior : self.untimedAction prevails on self.timeJump
	inv eitherTimedOrUntimed:
		Relation Exclusion(self.timeJump, self.untimedAction)
		
	inv releasePriorUntimed:
		Prior : self.allRelease prevails on self.untimedAction
	inv untimedPriorTimed2Exclusion:
		Relation Exclusion(self.untimedAction, allRelease)
		
	inv canReleasePriorUntimed:
		Prior : self.allCanRelease prevails on self.untimedAction
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
	(self.delay <> null and self.delay <> null) implies
		Relation SubClock(self.starts, theModel.untimedAction)
 	inv propagatesPrio12:
 	(self.delay <> null and self.delay <> null) implies
		Relation SubClock(self.canRelease, theModel.allCanRelease)
	inv propagatesPrio13:
	(self.delay <> null and self.delay <> null) implies
		Relation SubClock(self.wait, theModel.allWait)
		
context TriggerRef
	def : theModel : Model = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Model))->asSequence()->first().oclAsType(Model)
		
	inv propagatesPrio14:
		(not self.oclIsKindOf(VarRef) and self.startup) implies
		Relation SubClock(self.starts, theModel.untimedAction)
 
/**END For priority propagation (see paper@RIVF2019 */





 /**
  * begin state space shrinking:
	  * remove irrelevant internal interleaving
	  */
--context Model
--	
--	 def : allTimedConcepts : Collection(TimedConcept) =
--	  self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Timer)).oclAsType(TimedConcept)
--				->union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Action)).oclAsType(Action)) 
--			    ->union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Connection) and eo.oclAsType(Connection).delay <> null).oclAsType(Connection))
--
--	
--	inv onlyOneCanTickAtATime1:
--		Relation Exclusion(allTimedConcepts.canRelease)	
--	
--	inv onlyOneReleaseAtATime1:
--		Relation Exclusion(allTimedConcepts.releases)	
--		
--	inv onlyOneWaitAtATime1:
--		Relation Exclusion(allTimedConcepts.wait)	
--
--	inv canReleasePriorRelease:
--		Prior : self.allCanRelease prevails on self.allRelease					
--	inv canReleasePriorReleaseExclusion:
--		Relation Exclusion(self.allCanRelease, self.allRelease)
--	
--	inv canReleasePriorWait:
--		Prior : self.allCanRelease prevails on self.allWait						
--	inv canReleasePriorWaitExclusion:
--		Relation Exclusion(self.allCanRelease, self.allWait)
	
	 /** 
	  * end state space shrinking
	  */


endpackage