% functions to use for get strategies etc from high architecture

%Calculate Effiency of the Domino tactics
pigDestructionRate(X) :-
	findStructuresThatCanCollapseTheMost(Structures),
	getNumberOfPigsIn(Structures, X)
	.
	
findStructuresThatCanCollapseTheMost(Structures) :-
	findall(
		Structure,
		(
			structure(Structure),
			isCollapsable(Structure, IsCollapsable),
			IsCollapsable=true,
			canCollapse(Structure, OtherStructure),
			not(OtherStructure=false)			
		),
		StructureLists
	),
	sort(TmpList, List),
	length(List,X).
	
getNumberOfPigsIn(Structures, PigCount) :-
		findall(
			Pig,
			(
				belongsTo(Pig, Structures),
				pig(Pig)
			),
			PigList
		),
		sort(PigList, PigListSorted),
		length(PigListSorted, PigCount)
		.


% find all relevant structures 
% (are colapsable and have a relevant strategy)
findRelevantStructures(X) :-
	findall(
		Structure,
		(
			structure(Structure),
			structureType(Structure, Type),
			structureStrategy(Type, Strategy),
			not(Strategy = ignore),
			isCollapsable(Structure, IsCollapsable),
			IsCollapsable = true,
			(
				belongsTo(Object, Structure),
				pig(Object);
				canCollapse(Structure,AnotherStructure),
				belongsTo(AnotherObject, AnotherStructure),
				pig(AnotherObject)
			)
		),
		TmpList
	),
	sort(TmpList, List),
	length(List,X).

% Is a Structure Relevant for passing
isRelevant(Structure, Bool) :-
	((
		structure(Structure),
		structureType(Structure, Type),
		structureStrategy(Type, Strategy),
		not(Strategy=ignore),
		isCollapsable(Structure, IsCollapsable),
		IsCollapsable=true,
		(
			(belongsTo(Object,Structure),
			pig(Object));
			(canCollapse(Structure,OtherStructure),
			belongsTo(Object,OtherStructure),
			pig(Object))
		)
	) ->
		Bool=true;
		Bool=false
	).

getRelevantObjectsInStructure(Structure,List) :-
	findall(
		Object,
		belongsTo(Object, Structure),
		List
	).

findColapsable(Structure_One, Structure_Two) :- 
	findColapsable(Structure_One, Structure_Two,[]).
	
findColapsable(Structure_One, Structure_Two,Loop) :-
	canCollapse(Structure_One, Structure_Two);
	(
		canCollapse(Structure_One, Another_Structure),
		not(member(Another_Structure,Loop)),
		findColapsable(Another_Structure, Structure_Two,[Another_Structure|Loop])
	).
% Cuts (!)
% Backtracking verhindern
% GreenCut RedCut
% Learn Prolog Now (VC)

getNumberOfColapsableStructures(Structure,Number) :-
	findall(
		Struct,
		findColapsable(Structure,Struct),
		List
	),
	sort(List,OrderedList),
	length(OrderedList, Result),
	(
		(member(Structure, OrderedList)) -> Number is Result-1;
		Number = Result
	)
	. 

% find domino or chain reaction strategy
findDominoStrategy(StructueForDomino) :-
	findall(
		Structure,
		(
			structure(Structure),
			isRelevant(Structure,IsRelevant),
			IsRelevant=true
			
		),
		StructList
	).

