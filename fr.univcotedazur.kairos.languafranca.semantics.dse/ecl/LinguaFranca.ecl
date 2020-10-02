import 'http://www.icyphy.org/LinguaFranca'
import 'http://www.eclipse.org/emf/2002/Ecore'

ECLimport "platform:/plugin/fr.inria.aoste.timesquare.ccslkernel.model/ccsllibrary/kernel.ccslLib"
ECLimport "platform:/plugin/fr.inria.aoste.timesquare.ccslkernel.model/ccsllibrary/CCSL.ccslLib"
ECLimport "platform:/resource/fr.univcotedazur.kairos.linguafranca.semantics.moclib/mocc/LinguaFrancaUtils.moccml"

package linguaFranca

--context Model
--	def: time : Event = self

context Reactor
	def if(not self._main): startExecuting: Event = self 
	def if(not self._main): finishExecuting: Event = self

context Reaction
	def: synchronousExecution: Event = self 
	
context Variable
	def : update : Event = self
	
-- not needed it is a variable, can use update
--context Timer
--	def: release : Event = self
	

--context Timer
--	inv releasePeriodically:
--	let theModel : Model = self->closure(s | s.oclAsType(ecore::EObject).eContainer())->select(eo | eo.oclIsKindOf(Model))->asSequence()->first().oclAsType(Model) in
--	let thePeriod : Integer = self.period.parameter._init->first().time.interval in 
--	let theOffset : Integer = self.offset.parameter._init->first()._literal.toInteger() in 
--	let periodicRelease : Event = Expression Periodic(theModel.time, thePeriod, theOffset ) in
--	Relation Coincides(self.update, periodicRelease)
--	

context Model
	inv forFiniteStateSpace1:
		let firstInternalExec : Event = Expression Inf(self.reactors->select(r | not r._main).startExecuting) in
		let lastInternalExec : Event = Expression Sup(self.reactors->select(r | not r._main).finishExecuting) in
		Relation AlternatesFSM(firstInternalExec, lastInternalExec) 

	inv forAllTimersTogether: --this is crap for testing
		Relation Coincides(self.oclAsType(ecore::EObject).allSubobjects()->select(eo | eo.oclIsKindOf(Timer)).oclAsType(Timer).update)

context Reactor
	inv startComputeTimerOnly:
		(    (self.timers->size() > 0)
		 and (self.inputs->size() = 0)	
		) implies
		let allTimers : Event = Expression Union(self.timers.update) in
		Relation Coincides(self.startExecuting, allTimers)
	
	inv startComputeInputOnly:
		(    (self.timers->size() = 0)
		 and (self.inputs->size() > 0)
		) implies
		let allInputUpdates : Event = Expression Union(self.inputs.update) in
		Relation Coincides(self.startExecuting, allInputUpdates)

	inv startComputeInputAndTimer:
		(    (self.timers->size() > 0)
		 and (self.inputs->size() > 0)
		) implies
		let allTimers2 : Event = Expression Union(self.timers.update) in
		let allInputUpdates2 : Event = Expression Union(self.inputs.update) in
		let allTriggers : Event = Expression Union(allTimers2, allInputUpdates2) in
		Relation Coincides(self.startExecuting, allTriggers)
		
	inv startInternalReactionWhenStart:
		(self.reactions->size() > 0) implies
		let firstReactionExec : Event = Expression Inf(self.reactions.synchronousExecution) in
		Relation Coincides(firstReactionExec, self.startExecuting)
	
	inv FinishInternalReactionWhenFinish:
		(self.reactions->size() > 0) implies
		let lastReactionExec : Event = Expression Sup(self.reactions.synchronousExecution) in
		Relation Coincides(lastReactionExec, self.finishExecuting)
		
context Action
	inv ActionSourceCoincidesTarget:  
	(self.minDelay = null) implies
	let source : Reaction = self.oclAsType(ecore::EObject).eContainer().oclAsType(Reactor).reactions
				->select(r | r.effects->first().variable.name = self.name)->first()   in
	let target : Reaction = self.oclAsType(ecore::EObject).eContainer().oclAsType(Reactor).reactions
				->select(r | r.triggers->first().oclIsKindOf(VarRef) and r.triggers.oclAsType(VarRef)->first().variable.name = self.name)->first()   in
	Relation Coincides(source.synchronousExecution, target.synchronousExecution)
	 
	inv ActionSourceBeforeTarget:  
	(self.minDelay <> null) implies
	let source : Reaction = self.oclAsType(ecore::EObject).eContainer().oclAsType(Reactor).reactions
				->select(r | r.effects->first().variable.name = self.name)->first()   in
	let target : Reaction = self.oclAsType(ecore::EObject).eContainer().oclAsType(Reactor).reactions
				->select(r | r.triggers->first().oclIsKindOf(VarRef) and r.triggers.oclAsType(VarRef)->first().variable.name = self.name)->first()   in
	Relation Precedes(source.synchronousExecution, target.synchronousExecution)
	 
context Connection
	inv ConnectorSourceBeforeTarget:
	Relation Coincides
	(self.leftPorts.variable->first().update, self.rightPorts.variable->first().update)

context Reaction
	inv startWhenAnyInputArrived:
		(self.triggers->select(t|t.oclIsKindOf(Variable))->size() > 0) implies
		let anyInput : Event = Expression Union(self.triggers->select(t|t.oclIsKindOf(Variable)).oclAsType(Variable).update) in
		Relation Coincides(anyInput, self.synchronousExecution)
	inv UpdatesOutVarAllTogether: 
		(self.effects->size() > 1) implies
		Relation Coincides(self.effects.variable.update)
	inv UpdatesOutVarOnExecute:
		(self.effects->size()  > 0) implies
		Relation Coincides(self.effects.variable->first().update, self.synchronousExecution)
	

endpackage