import 'platform:/resource/org.lflang/model/generated/LF.ecore' --''https://lf-lang.org'
import 'http://www.eclipse.org/emf/2002/Ecore'

ECLimport "platform:/plugin/fr.inria.aoste.timesquare.ccslkernel.model/ccsllibrary/kernel.ccslLib"
ECLimport "platform:/plugin/fr.inria.aoste.timesquare.ccslkernel.model/ccsllibrary/CCSL.ccslLib"
ECLimport "platform:/resource/fr.univcotedazur.kairos.linguafranca.semantics.moclib/mocc/LinguaFrancaUtils.moccml"


 


/**
 * TODO: 
 *    add support for link between logical and physical time
 */



package ecore --trick to retrieve QVTo facilities
	context EObject
		def : allSubobjects() : Collection(EObject) = self.eAllContents().oclAsType(Collection(EObject))
endpackage






package lf

/**
 * Domain Specific Event definitions
 */
context Model
	def : timeJump : Event = self.timeJump()
	def : untimedAction : Event = self
	
	def : allRelease : Event = self
	def : allCanRelease : Event = self
	def : allWait : Event = self

context Reaction
	def: startExecution: Event(produceEvent) = self.exec()
	def: finishExecution: Event = self
	def if(self.effects->size() > 0): allOutputAbsent: Event = self

context Variable
	--or self.oclIsKindOf(Action)
	def if (not self.oclIsKindOf(TimedConcept)) : updates : Event = self.updates() 
	def if (not self.oclIsKindOf(TimedConcept)) : present : Event(produceEvent) = self
	def if (not self.oclIsKindOf(TimedConcept)) : absent : Event(FinishEvent) = self

	
context TriggerRef  --mother class of VarRef
	def : theModel : Model = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Model))->asSequence()->first().oclAsType(Model)

	def : updates : Event = self
	def : present : Event(produceEvent) = self
	def : absent : Event(FinishEvent) = self 
	def if (self.oclIsKindOf(VarRef) and self.oclAsType(ecore::EObject).eContainer().oclAsType(Reaction).effects->exists(t | t = self)
		): isPresent : Event(produceEvent) = self.isPresent()[result] 
		       						switch 
		       						case (result = true) forbid absent until updates;
				      				case (result = false) forbid present on updates;

context TimedConcept
	def 
	if(not (self.oclIsKindOf(Connection) and self.oclAsType(Connection).delay = null)
	  ): starts : Event(produceEvent) = self.schedule() --coincides with update since Action is a Variable
	def
	if( not (self.oclIsKindOf(Connection) and self.oclAsType(Connection).delay = null)
	  ): releases : Event(StartEvent) = self.release()	  
	def 
	if(not (self.oclIsKindOf(Connection) and self.oclAsType(Connection).delay = null)
	): wait : Event(FinishEvent) = self
	def 
	if(not (self.oclIsKindOf(Connection) and self.oclAsType(Connection).delay = null)
	): canRelease : Event(FinishEvent) = self.canTick()
										[res] 
			       						switch 
			       						case (res = true)  forbid wait until releases; 
					      				case (res = false) forbid releases until wait;



/**
 * Domain Specific Constraint Definitions
 */
 
context TriggerRef
	def : theModel : Model = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Model))->asSequence()->first().oclAsType(Model)

 	
	inv TRupdatesIsPresentOrAbsent:
		let TRpresOrAbs : Event = Expression Union(self.present, self.absent) in
		Relation Coincides(self.updates, TRpresOrAbs)
	
	inv TReitherPresOrAbs:
		Relation Exclusion(self.present, self.absent)
	
	--constrains the startup
		
--	inv startupPresentOnlyOnce:
--		(not self.oclIsKindOf(VarRef) and self.startup) implies
--		let firstTick : Event = Expression OneTickAndNoMore(self.present) in 
--		Relation Coincides(firstTick, self.present)
		
	inv startupPresentOnlyOnce:
		(not self.oclIsKindOf(VarRef) and self.startup) implies
--		let firstTick : Event = Expression OneTickAndNoMore(self.present) in 
		let firstUntimed : Event = Expression OneTickAndNoMore(theModel.untimedAction) in 
		Relation Coincides(self.present, firstUntimed)
		
	inv startupPresentBeforeAbsent:
		(not self.oclIsKindOf(VarRef) and self.startup) implies
		let firstAbsentTick : Event = Expression OneTickAndNoMore(self.absent) in 
		Relation AlternatesFSM(self.present, firstAbsentTick)
		
	
		
		
		
		
		
		
		
		
	--for all triggers 
	inv AbsentOnlyOnceByCycle:
		(self.oclAsType(ecore::EObject).eContainer().oclAsType(Reaction).triggers->exists(t | t = self) and self.startup) implies
		--let execOrTJ : Event = Expression Union(theModel.timeJump, self.oclAsType(ecore::EObject).eContainer().oclAsType(Reaction).finishExecution) in
		Relation AlternatesFSM(self.updates, theModel.timeJump) -- self.oclAsType(ecore::EObject).eContainer().oclAsType(Reaction).finishExecution)
 
 
 
 
 
 
 
 
context VarRef
	def : theModel : Model = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Model))->asSequence()->first().oclAsType(Model)
	
	--PORTS
	inv presentVarRefToPort:
		(self.variable.oclIsKindOf(Port)) implies
		Relation Coincides(self.present, self.variable.oclAsType(Port).present)
	inv absentVarRefToPort:
		(self.variable.oclIsKindOf(Port)) implies
		Relation Coincides(self.absent, self.variable.oclAsType(Port).absent)
--	inv updatesVarRefToPort:
--		(self.variable.oclIsKindOf(Port)) implies
--		Relation Coincides(self.updates, self.variable.oclAsType(Port).updates)
 
 	--ACTIONS (depends if in triggers or effects)
 	inv trigPresentVarRefToAction:
		(self.variable.oclIsKindOf(Action) and self.oclAsType(ecore::EObject).eContainer().oclAsType(Reaction).triggers->exists(t | t = self)) implies
		let releaseSampledOnUntimed1 : Event = Expression SampledOn(self.variable.oclAsType(Action).releases, theModel.untimedAction) in
		Relation Coincides(releaseSampledOnUntimed1, self.present)
	
	inv trigAbsentVarRefToVar: 
		(self.variable.oclIsKindOf(Action) and self.oclAsType(ecore::EObject).eContainer().oclAsType(Reaction).triggers->exists(t | t = self)) implies
		Relation Coincides(self.absent, self.variable.oclAsType(Action).wait)
		
	inv effectPresentVarRefToAction:
		(self.variable.oclIsKindOf(Action) and self.oclAsType(ecore::EObject).eContainer().oclAsType(Reaction).effects->exists(t | t = self)) implies
		Relation Coincides(self.present, self.variable.oclAsType(Action).starts)
		
 	--TIMERS
 	inv presentVarRefToTimer:
		(self.variable.oclIsKindOf(Timer)) implies
		let releaseSampledOnUntimed2 : Event = Expression SampledOn(self.variable.oclAsType(Timer).releases, theModel.untimedAction) in
		Relation Coincides(releaseSampledOnUntimed2, self.present)
	inv absentVarRefToTimer:
		(self.variable.oclIsKindOf(Timer)) implies
		Relation Coincides(self.absent, self.variable.oclAsType(Timer).wait)
 
context Variable
	inv updatesIsPresentOrAbsent:
		(not self.oclIsKindOf(TimedConcept)) implies
		let presOrAbs : Event = Expression Union(self.present, self.absent) in
		Relation Coincides(self.updates, presOrAbs)
	
	inv eitherPresOrAbs:
		(not self.oclIsKindOf(TimedConcept)) implies
		Relation Exclusion(self.present, self.absent)
 
 
context Model
	
	 def : allTimedConcepts : Collection(TimedConcept) =
	  self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Timer)).oclAsType(TimedConcept)
				->union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Action)).oclAsType(TimedConcept))
			    ->union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Connection) and eo.oclAsType(Connection).delay <> null).oclAsType(TimedConcept))

--	TODO: the following defs do not work if some collections are empty, i.e. ii there is no timedElements

 	inv defuntimedActionWithStartup:
 		let allStartups1 : Collection(TriggerRef) = self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(TriggerRef) and eo.oclAsType(TriggerRef).startup).oclAsType(TriggerRef) in
 		(allStartups1->size() > 0) implies
 		let allStartExecution1: Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Reaction)).oclAsType(Reaction).startExecution) in
 		let allFinishExecution1: Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Reaction)).oclAsType(Reaction).finishExecution) in
		let allUpdates1: Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Variable) and not eo.oclIsKindOf(TimedConcept)).oclAsType(Variable).updates) in
		let allUpdates1a: Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(TriggerRef)).oclAsType(TriggerRef).updates) in
		let allTimedConceptsStarts1: Event = Expression Union(allTimedConcepts.starts) in
		let allTimedConceptsWait1: Event = Expression Union(allTimedConcepts.wait) in
		let allStartupStarts1: Event = Expression Union(allStartups1.updates) in
		let union1_2a : Event = Expression Union(allUpdates1, allStartExecution1) in
		let union1_2a2 : Event = Expression Union(union1_2a, allFinishExecution1) in
		let union1_2a3 : Event = Expression Union(union1_2a2, allTimedConceptsWait1) in
		let union1_2a4 : Event = Expression Union(union1_2a3, allUpdates1a) in
		let union1_2_3a : Event = Expression Union(union1_2a4, allTimedConceptsStarts1) in
		let union1_2_3_4a : Event = Expression Union(union1_2_3a, allStartupStarts1) in
		Relation Coincides(self.untimedAction, union1_2_3_4a)
		
	inv defuntimedActionNoStartup:
		let allStartups3 : Sequence(TriggerRef) = self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(TriggerRef) and eo.oclAsType(TriggerRef).startup).oclAsType(TriggerRef)->asSequence() in
 		(allStartups3->size() = 0) implies
 		let allStartExecution3: Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Reaction)).oclAsType(Reaction).startExecution) in
 		let allFinishExecution3: Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Reaction)).oclAsType(Reaction).finishExecution) in
		let allUpdates3: Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Variable) and not eo.oclIsKindOf(TimedConcept)).oclAsType(Variable).updates) in
		let allUpdates3a: Event = Expression Union(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(TriggerRef)).oclAsType(TriggerRef).updates) in
		let allTimedConceptsStarts3: Event = Expression Union(allTimedConcepts.starts) in
		let allTimedConceptsWait3: Event = Expression Union(allTimedConcepts.wait) in
		let union1_2c : Event = Expression Union(allUpdates3, allStartExecution3) in
		let union1_2c2 : Event = Expression Union(union1_2c, allFinishExecution3) in
		let union1_2c3 : Event = Expression Union(union1_2c2, allTimedConceptsWait3) in
		let union1_2c4 : Event = Expression Union(union1_2c3, allUpdates3a) in
		let union1_2_3c : Event = Expression Union(union1_2c4, allTimedConceptsStarts3) in
		Relation Coincides(self.untimedAction, union1_2_3c)
	 
		
	inv defAllCanRelease:
		let allTimedConceptCanRelease1 : Event = Expression Union(allTimedConcepts.canRelease) in
		Relation Coincides(allTimedConceptCanRelease1,allCanRelease)
		
	inv defallRelease:
		let allTimedConceptRelease1 : Event = Expression Union(allTimedConcepts.releases) in
		Relation Coincides(allTimedConceptRelease1,allRelease)
	
 	inv defAllWait:
 		let allTimedConceptsWait2 : Event = Expression Union(allTimedConcepts.wait) in
		Relation Coincides(self.allWait, allTimedConceptsWait2)	
	
context Timer
	def : theModel : Model = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Model))->asSequence()->first().oclAsType(Model)

	inv timerSetActionLifeCycle:
		Relation TimerConstraint(self.starts, self.canRelease, self.wait, self.releases, theModel.timeJump) 
		
context Action
	def : theModel : Model = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Model))->asSequence()->first().oclAsType(Model)


	inv setActionLifeCycleSelfLoop: 
		(self.oclAsType(ecore::EObject).eContainer().oclIsKindOf(Reactor) and self.oclAsType(ecore::EObject).eContainer().oclAsType(Reactor).reactions->exists(r |r.triggers->exists(t | t.oclIsKindOf(VarRef) and t.oclAsType(VarRef).variable = self) and r.effects->exists(e | e.oclIsKindOf(VarRef) and e.oclAsType(VarRef).variable = self))) implies
		Relation ConnectionActionSelfLoop(self.starts, self.canRelease, self.wait, self.releases, theModel.timeJump) 
		
	inv setActionLifeCycleNOSelfLoop: --not powerfull enough... should check with a closure
		(not (self.oclAsType(ecore::EObject).eContainer().oclIsKindOf(Reactor) and self.oclAsType(ecore::EObject).eContainer().oclAsType(Reactor).reactions->exists(r |r.triggers->exists(t | t.oclIsKindOf(VarRef) and t.oclAsType(VarRef).variable = self) and r.effects->exists(e | e.oclIsKindOf(VarRef) and e.oclAsType(VarRef).variable = self)))) implies
		Relation ConnectionAction(self.starts, self.canRelease, self.wait, self.releases, theModel.timeJump) 
 
context Connection
	def : theModel : Model = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Model))->asSequence()->first().oclAsType(Model)
	
	inv ConnectorSourceBeforeTargetForPresent:
		(self.delay = null) implies
		Relation Coincides(self.leftPorts.variable->first().present, self.rightPorts.variable->first().present)
		
	inv ConnectorSourceBeforeTargetForAbsent:
		(self.delay = null) implies
		Relation Coincides(self.leftPorts.variable->first().absent, self.rightPorts.variable->first().absent)
		
--	inv ConnectorSourceAbsentPrecedesWait:
--		(self.delay <> null) implies
--		Relation Precedes(self.leftPorts.variable->first().absent, self.wait)
		
	inv ConnectorSourceWaitCoincidesTargetAbsent:
		(self.delay <> null) implies
		let waitOrAbsent : Event = Expression Union(self.wait, self.leftPorts.variable->first().absent) in
		Relation Coincides(waitOrAbsent, self.rightPorts.variable->first().absent)
		
	inv test:
	(self.delay <> null) implies
	Relation Exclusion(self.wait, self.leftPorts.variable->first().absent)
		
		
	inv ConnectorConstraintsNoSelfLoop:
		(self.delay <> null and self.rightPorts->first().variable.oclAsType(ecore::EObject).eContainer() <> self.leftPorts->first().variable.oclAsType(ecore::EObject).eContainer()) implies
		Relation ConnectionAction(self.starts, self.canRelease, self.wait, self.releases, theModel.timeJump) 
		
	inv ConnectorConstraintsSelfLoop:
		(self.delay <> null and self.rightPorts->first().variable.oclAsType(ecore::EObject).eContainer() = self.leftPorts->first().variable.oclAsType(ecore::EObject).eContainer()) implies
		Relation ConnectionActionSelfLoop(self.starts, self.canRelease, self.wait, self.releases, theModel.timeJump) 
		
	inv TimedConnectorStartsWithSource:
		(self.delay <> null) implies	
		Relation Coincides(self.leftPorts.variable->first().present, self.starts)
		
	inv TimedConnectorReleaseWithTarget:
		(self.delay <> null) implies
		let releaseSampledOnUntimed : Event = Expression SampledOn(self.releases, theModel.untimedAction) in
		Relation Coincides(releaseSampledOnUntimed, self.rightPorts->first().present)   

context Reaction	
	def : inputStartups : Sequence(TriggerRef) = self.triggers->select(t|t.oclIsKindOf(TriggerRef) and t.oclAsType(TriggerRef).startup).oclAsType(TriggerRef) 
	def : theModel : Model = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Model))->asSequence()->first().oclAsType(Model)
	
		inv allAbsentDef:
			(self.effects->size() > 0) implies
			let allAbsentTogether : Event = Expression Intersection(self.effects.absent) in
			Relation Coincides(self.allOutputAbsent, allAbsentTogether)
	
	inv reactionLifeCycle:
		(self.effects->size() > 0) implies
		let allInputs: OrderedSet(TriggerRef) = self.triggers in 
		let theInputsPresence : Event = Expression Union(allInputs.present) in
		let theLastUpdate : Event = Expression Sup(allInputs.updates) in
		Relation Reaction(theInputsPresence, theLastUpdate, self.startExecution, self.finishExecution, self.effects->first().updates, self.allOutputAbsent, theModel.timeJump)
		
	inv reactionLifeCycleNoOutput:
		(self.effects->size() = 0) implies
		let allInputsNO: Sequence(VarRef) = self.triggers.oclAsType(VarRef) in
		let theInputsPresenceNO : Event = Expression Union(allInputsNO.present) in
		let theLastUpdateNO : Event = Expression Sup(allInputsNO.updates) in
		Relation ReactionNoOutput(theInputsPresenceNO, theLastUpdateNO, self.startExecution, self.finishExecution, theModel.timeJump)
	
	inv UpdatesOutVarAllTogether:
		(self.effects->size() > 1) implies
		Relation Coincides(self.effects.updates)
		
	inv IsPresentOutVarAllTogether:
		(self.effects->size() > 1) implies
		Relation Coincides(self.effects.isPresent)
	
	inv StartUpdateProcessOutVarOnFinishExecute:
		(self.effects->size()  > 0) implies
		Relation Coincides(self.finishExecution, self.effects->first().isPresent)
	

context Reactor
	inv followPriorityFromModel:
		Relation Precedes(self.reactions.startExecution)

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

context Model
	inv propagatesPrio:
		Relation SubClock(self.allWait, self.untimedAction)

context Reaction
	def : theModel : Model = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Model))->asSequence()->first().oclAsType(Model)
	inv propagatesPrio0:
		Relation SubClock(self.finishExecution, theModel.untimedAction)
	inv propagatesPrio1:
		Relation SubClock(self.startExecution, theModel.untimedAction)
	inv propagatesPrio1a:
	(self.effects->size() > 0) implies
		Relation SubClock(self.allOutputAbsent, theModel.untimedAction)

context Variable
	def : theModel : Model = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Model))->asSequence()->first().oclAsType(Model)
	inv propagatesPrio2:
		(not self.oclIsKindOf(TimedConcept)) implies
		Relation SubClock(self.updates, theModel.untimedAction)
	inv propagatesPrio2a:
		(not self.oclIsKindOf(TimedConcept)) implies
		Relation SubClock(self.present, theModel.untimedAction)
	inv propagatesPrio2b:
		(not self.oclIsKindOf(TimedConcept)) implies
		Relation SubClock(self.absent, theModel.untimedAction)

context Timer
	def : theModel : Model = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Model))->asSequence()->first().oclAsType(Model)
	inv propagatesPrio5:
		Relation SubClock(self.starts, theModel.untimedAction)
	inv propagatesPrio7:
		Relation SubClock(self.canRelease, theModel.allCanRelease)
	inv propagatesPrio9:
		Relation SubClock(self.wait, theModel.allWait)
	inv propagatesPrio9a:
		Relation SubClock(self.wait, theModel.untimedAction)

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
	(self.delay <> null ) implies
		Relation SubClock(self.starts, theModel.untimedAction)
 	inv propagatesPrio12:
 	(self.delay <> null ) implies
		Relation SubClock(self.canRelease, theModel.allCanRelease)
	inv propagatesPrio13:
	(self.delay <> null) implies
		Relation SubClock(self.wait, theModel.allWait)
		
context TriggerRef
	def : theModel : Model = self.oclAsType(ecore::EObject)->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Model))->asSequence()->first().oclAsType(Model)
		
	inv propagatesPrio14:
		Relation SubClock(self.present, theModel.untimedAction)
	inv propagatesPrio15:
		Relation SubClock(self.absent, theModel.untimedAction)
	inv propagatesPrio16:
		Relation SubClock(self.updates, theModel.untimedAction)
	
 
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