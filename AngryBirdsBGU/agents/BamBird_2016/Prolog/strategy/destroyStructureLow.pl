%% destroyStructureStDS/0
% destroyStructureStDS()
% 

ptestDestroyStructureLow() :-
	doInferencialGroundingSeIG(),
	saveDestroyStructurePlansLowStDS().
%saveDestroyStructurePlansStDS() :-
saveDestroyStructurePlansLowStDS() :-
	pfindAllRelevantStructuresStDS(StructList),
	(
		(StructList == []) ->
			psysoStDS('No relevant structures in map','Something went totaly wrong');
			psavePlansForEachStructureStDS(StructList)
	).
	
% pfindAllRelevantStructuresStDS(-StructList)
pfindAllRelevantStructuresStDS(StructList) :-
	findall(
		Struct,
		(
			structure(Struct),
			containsRelevant(Struct,_RelevantObject)
		),
		TmpStructList
	),
	sort(TmpStructList,StructList).
	
% psavePlansForEachStructureStDS(+StructList)
psavePlansForEachStructureStDS([]) :- !.
psavePlansForEachStructureStDS([Struct|Tail]) :-
	pfindAllBearingObjectsInStructStDS(Struct,BearingObjects),
	(
		(BearingObjects == []) ->
			syso('No Hittable Objects in',Struct),
			psavePlansForEachStructureStDS(Tail);
			psavePlansForStructureStDS(BearingObjects),
			psavePlansForEachStructureStDS(Tail)
	).
	

% pfindAllBearingObjectsInStructStDS(+Struct,-BearingObjectList)
pfindAllBearingObjectsInStructStDS(Struct,BearingObjects) :-
	findall(
		BearingObject,
		(
			object(Object),
			belongsTo(Object,Struct),
			isHittable(Object,true),
			hasIntegrity(Object,Integrity),
			BearingObject = [Integrity,Object]
		),
		UnsortedBearingObjects
	),
	kwikeSortInverse(UnsortedBearingObjects,BearingObjects).

% psavePlansForStructureStDS(+ListOfBearingObjects)
psavePlansForStructureStDS([]) :- !.
psavePlansForStructureStDS([BearingObject|Tail]) :-
	nth0(0,BearingObject,IntegrityValue),
	nth0(1,BearingObject,Object),
	(
		(IntegrityValue > 1;pig(Object)) ->
			psavePlansForObjectStDS(Object);
			psysoStDS('Useless Object', Object)
	),
	psavePlansForStructureStDS(Tail).

% psavePlanForObjectStDS(+Object)
psavePlansForObjectStDS(Object) :-
	findall(
		Goal,
		(
			(isOn(Goal,Object));
			(isBelow(Goal,Object),
			not(Object == ground));
			(belongsTo(Object,Struct),
			containsRelevant(Struct,Goal))
		),
		GoalList
	),
	Target = [Object],
	psavePlanStDS(Target,GoalList).
	
% psavePlanStDS(+Target,+Goal)
psavePlanStDS(Target,Goal) :-
	pgetRankForTargetStDS(Target,Goal,Rank),
	savePlan(Target,Goal,destroyLow,Rank).
	
% pgetRankForTargetStDS(+TargetList,+GoalList,-Rank)
pgetRankForTargetStDS(Target,Goal,Rank) :-
	append(Target,Goal,Objects),
	pfindAllIncludedStructuresStDS(Objects,UnsortedStructList),
	sort(UnsortedStructList,Structures),
	length(Structures,StructCount),
	pfindAllContainingPigsStDS(Structures,UnsortedPigList),
	sort(UnsortedPigList,PigList),
	length(PigList,PigCount),
	pfindAllContainingObjectsStDS(Structures,UnsortedObjectList),
	sort(UnsortedObjectList,ObjectList),
	length(ObjectList,ObjectCount),
	nth0(0,Target,ThisTarget),
	hasIntegrity(ThisTarget,IntegrityValue),
	getPlanRankSeDH(PigCount,IntegrityValue,ObjectCount,StructCount,destroy,Rank).

% pfindAllContainingObjectsStDS(+StructList,-Objects)
pfindAllContainingObjectsStDS([],[]) :- !.
pfindAllContainingObjectsStDS([Struct|Tail],ObjectList) :-
	findall(
		Object,
		(
			object(Object),
			belongsTo(Object,Struct)
		),
		TmpObjectList
	),
	pfindAllContainingObjectsStDS(Tail,NextObjectList),
	append(TmpObjectList,NextObjectList,ObjectList).

% pfindAllIncludedStructuresStDS(+Objects,-Structures)
pfindAllIncludedStructuresStDS([],[]) :- !.
pfindAllIncludedStructuresStDS([Object|Tail],Structures) :-
	findall(
		Struct,
		belongsTo(Object,Struct),
		TmpStructList
	),
	pfindAllIncludedStructuresStDS(Tail,NextStructList),
	append(TmpStructList,NextStructList,Structures).
% pfindAllContainingPigsStDS(+StructList,-Pigs)
pfindAllContainingPigsStDS([],[]) :- !.
pfindAllContainingPigsStDS([Struct|Tail],PigList) :-
	findall(
		Pig,
		(
			belongsTo(Pig,Struct),
			pig(Pig)
		),
		TmpPigList
	),
	pfindAllContainingPigsStDS(Tail,NextPigList),
	append(TmpPigList,NextPigList,PigList).

psysoStDS(Tag,TextOne,_TextTwo) :-
	(
		(Tag == d) ->
			string_concat('[Depot][Debug] ',TextOne,_Text);
			string_concat('[Depot][Sonstiges] ',TextOne,_Text) 
	),
%	syso(Text,TextTwo),
	true.
psysoStDS(TextOne,_TextTwo) :-
	string_concat('[DestroyLow] ',TextOne,_Text),
%	syso(Text,TextTwo),
	true.
	
		