
%%%%
% FUNCTIONS FOR DEPOT STRATEGY
%%%%

ptestDepotStrategyStDe() :-
	doInferencialGroundingSeIG(),
	savePlansForDepotStrategyStDe().

%%savePlansForDepotStrategyStDe
% savePlansForDepotStrategyStDe()
%  searches Depos in Maps and generates Plans for them
savePlansForDepotStrategyStDe() :-
%	asserta(depotPlanCounter(0)),
	pfindDepotsInMapStDe(DepotList),
	(
		(DepotList==[]) ->
			psysoStDe('No','Depots');
			psavePlansForDepotsStDe(DepotList)
	),
	psysoStDe('','PlansSafed').
%	retractall(depotPlanCounter(_X)).
	
% pfindDepotsInMapStDe(-DepotList)
% retruns a List with all structures that are depots
pfindDepotsInMapStDe(DepotList) :-
	findall(
		Struct,
		(
			structure(Struct),
			containsRelevant(Struct,RelevantObject),
			not(hasMaterial(RelevantObject,tnt)),
			not(pig(RelevantObject))
		),
		StructList
	),
	sort(StructList,DepotList).
	
% psavePlansForDepotsStDe(+DepotList)
% saves plans for all depots
psavePlansForDepotsStDe([]) :- !.
psavePlansForDepotsStDe(InputList) :-
	splitListIn(InputList,Depot,Tail),
	pfindCollapsableStructuresStDe(Depot,CollapseList),
	length(CollapseList,NumberOfCollapsables),
	(NumberOfCollapsables > 0 ->
		psavePlansToCollapseOtherStructuresStDe(Depot,CollapseList);
		pfindNextStructureStDe(Depot,NextStructs),
		(
			(NextStructs == []) ->
				psysoStDe('No','Structs to collapse');
				psavePlansForNextStructuresStDe(Depot,NextStructs)
		)
	),
	psavePlansForDepotsStDe(Tail),!.

% pfindNextStructureStDe(+Depot,-NextStruct)
% returns the Next structure from depot with direction
pfindNextStructureStDe(Depot,NextStructs) :-
	findall(
		StructDirect,
		(
			collapsesInDirection(Depot,StructDirectTmp,DirectionDirect),
			StructDirect = [StructDirectTmp,DirectionDirect]
		),
		StructDirectList
	),
	findall(
		StructIndirect,
		(
			collapsesInDirection(StructIndirectTmp,Depot,DirectionIndirectTmp),
			(
				(DirectionIndirectTmp == towards) ->
					DirectionIndirect = away;
					DirectionIndirect = towards
			),
			StructIndirect = [StructIndirectTmp,DirectionIndirect]
		),
		StructIndirectList
	),
	(
		(StructDirectList == [], StructIndirect == []) ->
			NextStructs = [];
			append(StructDirectList,StructIndirectList,NextStructs)
	).

% psavePlansForNextStructures(+Depot,+StructList)
% saves plans for all maybe reachable structures
psavePlansForNextStructuresStDe(_Depot,[]) :- !.
psavePlansForNextStructuresStDe(Depot,[Struct|Tail]) :-
	nth0(0,Struct,Structure),
	nth0(1,Struct,Direction),
	findall(
		Relevant,
		(
			containsRelevant(Structure,Relevant),
			not(protects(Depot,Relevant))
		),
		RelevantObjects
	),
	(
		(RelevantObjects == []) ->
			psysoStDe('No relevant objects in Structure',Structure);
			pfindHittableObjectInDepotStDe(Depot,Structure,Direction,HittableObjects),
			psavePlansToCollapseOtherStructuresWithDepotStDe(HittableObjects,RelevantObjects)
	),
	psavePlansForNextStructuresStDe(Depot,Tail).

% psavePlansToCollapseOtherStructuresStDe(+CollapseList)
% save plans for structures that can be collapsed by depot
psavePlansToCollapseOtherStructuresStDe(_Depot,[]) :- !.
psavePlansToCollapseOtherStructuresStDe(Depot,[Collapsable|Tail]) :-
	findall(
		Relevant,
		(
			containsRelevant(Collapsable,Relevant),
			not(protects(Depot,Relevant))
		),
		RelevantObjects
	),
	length(RelevantObjects,NumberOfRelevantObjects),
	(
		(NumberOfRelevantObjects == 0) ->
			psysoStDe('No relevant objects in collapsable Structure',Collapsable);
			psysoStDe(d,findHittablesFor,Depot),
			pfindHittableObjectInDepotStDe(Depot,Collapsable,HittableObjects),
			psysoStDe(d,hittablesFound,HittableObjects),
			psavePlansToCollapseOtherStructuresWithDepotStDe(HittableObjects,RelevantObjects)
	),
	psavePlansToCollapseOtherStructuresStDe(Depot,Tail).

% pfindHittableObjectInDepotStDe(+Depot,+CollapsableStructure,-HittableObjectList)
% returns the object to collapse the depot in direction of the Collapsable Structure
pfindHittableObjectInDepotStDe(Depot,Collapsable,HittableObjects) :-
	collapsesInDirection(Depot,Collapsable,Direction),
	findHittablesForCollapseSortedByMinHeightSeQS(Depot,Direction,HittableObjects).

% pfindHittableObjectInDepotStDe(+Depot,+Structure,+Direction,-HittableObjectList)
pfindHittableObjectInDepotStDe(Depot,_Structure,Direction,HittableObjectList) :-
	psysoStDe(depot,Depot),
	psysoStDe(direction,Direction),
	findHittablesForCollapseSortedByMinHeightSeQS(Depot,Direction,HittableObjectList),
	psysoStDe(hittableObjects,HittableObjectList).

% pfindCollapsableStructuresStDe(+Depot,-ListOfCollapsableStructures)
% find all Structures that can be collapsed by the depot
pfindCollapsableStructuresStDe(Depot,CollapseList) :-
	findall(
		Collapsable,
		canCollapse(Depot,Collapsable),
		TmpList
	),
	sort(TmpList,CollapseList).

% psavePlansToCollapseOtherStructuresWithDepotStDe(+HittableObjectsOfDepot,+RelevantObjectsOfTarget)
% save Plans for each hittable object of depot
psavePlansToCollapseOtherStructuresWithDepotStDe([],_RelevantObjectsOfTarget) :- !.
psavePlansToCollapseOtherStructuresWithDepotStDe([HittableObject|Tail],RelevantObjectsOfTarget) :-
	HittableObjectList = [HittableObject],
	append(HittableObjectList,RelevantObjectsOfTarget,Goals),
	Targets = [HittableObject],
	psavePlanForDepotStDe(Targets,Goals),
	psavePlansToCollapseOtherStructuresWithDepotStDe(Tail,RelevantObjectsOfTarget).

psavePlanForDepotStDe(Targets,Goals) :-
%	nth0(0,Targets,Target),
%	nth0(0,Goals,Goal),
	psysoStDe(targets,Targets),
	psysoStDe(goals,Goals),
	findall(
		Plan,
		(
			plan(Target,Goal,depot,_Rank),
			Target == Targets,
			Goal == Goals,
			Plan = [Target,Goal]
		),
		ExistingPlanList
	),
	psysoStDe(d,existingPlanList,ExistingPlanList),
	(
		(ExistingPlanList == []) ->
			pgetValuesForDepotStDe(Targets,Goals,TmpTPigs,TmpTStruct,TmpTObjects,TmpTDepotSize),
			getPlanRankSeDH(TmpTPigs,TmpTStruct,TmpTObjects,TmpTDepotSize,depot,Rank),
			savePlan(Targets,Goals,depot,Rank);
			psysoStDe('Plan not saved','Already exists')
	).


% calculate Rank

% pgetValuesForDepotStDe(+TargetList,-Pigs,-Structures,-Objects,-Count)
pgetValuesForDepotStDe(Target,Goal,Pigs,Struct,Objects,DepotSize) :-
	pgetAllInvolvedStructuresStDe(Target,TargetList),
	pgetAllInvolvedStructuresStDe(Goal,GoalList),
	append(TargetList,GoalList,UnsortedStructList),
	sort(UnsortedStructList,StructList),
	pfindAllPigsInTargetsAndGoalsStDe(StructList,UnsortedPigList),
	sort(UnsortedPigList,PigList),
	length(PigList,Pigs),
	length(StructList,Struct),
	pfindAllObjectsInTargetsAndGoalsStDe(StructList,UnsortedObjectList),
	sort(UnsortedObjectList,ObjectList),
	length(ObjectList,Objects),
	pgetDepotSizeStDe(Target,DepotSize).
	
% pgetAllInvolvedStructuresStDe(+TargetList,-StructList)
pgetAllInvolvedStructuresStDe([],[]) :- !.
pgetAllInvolvedStructuresStDe([Target|Tail],StructList) :-
	belongsTo(Target,StructTmp),
	Struct = [StructTmp],
	pgetAllInvolvedStructuresStDe(Tail,OtherStructs),
	append(Struct,OtherStructs,StructList).
	
% pgetDepotSizeStDe(+Target,-DepotSize)
pgetDepotSizeStDe(Target,DepotSize) :-
	findall(
		Object,
		(
			belongsTo(Target,Depot),
			object(Object),
			belongsTo(Object,Depot)
		),
		ObjectList
	),
	sort(ObjectList,OrderedList),
	length(OrderedList,DepotSize).
	
% pfindAllObjectsInTargetsAndGoals(+StructList,-Objects)
pfindAllObjectsInTargetsAndGoalsStDe([],[]) :- !.
pfindAllObjectsInTargetsAndGoalsStDe([Struct|Tail],Objects) :-
	findall(
		Object,
		(
			belongsTo(Object,Struct),
			object(Object)
		),
		ObjectList
	),
	pfindAllObjectsInTargetsAndGoalsStDe(Tail,FurtherObjects),
	append(ObjectList,FurtherObjects,Objects).
	
% pfindAllPigsInTargetsAndGoalsStDe(+StructList,-Pigs)
pfindAllPigsInTargetsAndGoalsStDe([],[]) :- !.
pfindAllPigsInTargetsAndGoalsStDe([Struct|Tail],Pigs) :-
	findall(
		Pig,
		(
			containsRelevant(Struct,Pig),
			pig(Pig)
		),
		PigList
	),
	pfindAllPigsInTargetsAndGoalsStDe(Tail,FurtherPigs),
	append(FurtherPigs,PigList,Pigs).
		
% pgetAllReachableStructuresStDe(+Target,+Goal,-StructList)
pgetAllReachableStructuresStDe(Target,Goal,StructList) :-
	findall(
		TStructs,
		belongsTo(Target,TStructs),
		TStructList
	),
	nth0(0,TStructList,TStruct),
	findall(
		Struct,
		(
			belongsTo(Goal,Struct);
			belongsTo(Goal,GStruct),
			collapsesInDirection(TStruct,GStruct,Direction),
			collapsesInDirection(TStruct,Struct,Direction)
		),
		GStructList
	),
	append(TStructList,GStructList,TmpStructList),
	sort(TmpStructList,StructList).
	

% pgetPigsInTtargetStDe(+Target,-Pigs)
pgetPigsInTargetStDe(Target,Pigs) :-
	belongsTo(Target,Struct),
	findall(
		Pig,
		(
			containsRelevant(Struct,Pig),
			pig(Pig)
		),
		PigList
	),
	sort(PigList,OrderedList),
	length(OrderedList,Pigs).

psysoStDe(Tag,TextOne,_TextTwo) :-
	(
		(Tag == d) ->
			string_concat('[Depot][Debug] ',TextOne,_Text);
			string_concat('[Depot][Sonstiges] ',TextOne,_Text) 
	),
%	syso(Text,TextTwo),
	true.
psysoStDe(TextOne,_TextTwo) :-
	string_concat('[Depot] ',TextOne,_Text),
%	syso(Text,TextTwo),
	true.
