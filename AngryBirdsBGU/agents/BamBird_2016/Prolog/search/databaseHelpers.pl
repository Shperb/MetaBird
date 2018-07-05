
%%getEfficiencyValueSeDH/2
% getEfficiencyValueSeDH(+Object,-Value)
% gets a Object and returns the efficiency value
% depending on the actual bird
getEfficiencyValueSeDH(Object,EffValue) :-
	findall(
		Values,
		getEfficiencyValue(Object,Values),
		List
	),
	nth0(0,List,EffValue).
getEfficiencyValue(Object, EffValue) :-
	hasSize(Object,Size),
	hasForm(Object,Form),
	hasMaterial(Object,Material),
	birdOrder(Bird,0),
	hasColor(Bird,Color),
	efficiencySizeForm(Size,Form,Material,EffObValue),
	efficiencyBirdMaterial(Color,Material,EffBiValue),
	EffValue is EffObValue*EffBiValue.
	
%%getHeightSeDH/2
% getHeight(+Object,-Value)
% gets an Object and returns the height of the object
% depending on the orientation of the object
getHeightSeDH(Object,Value) :-
	findall(
		Values,
		getHeight(Object,Values),
		List
	),
	nth0(0,List,Value).
getHeight(Object,Value) :-
	hasSize(Object,Size),
	(pig(Object) ->
		pigSize(Size,Value);
		hasOrientation(Object,Orientation),
		hasForm(Object,Form),
		(Orientation == 'vertical' ->
			objectWidthHeight(Size,Form,_Ignore,TmpValue);
			objectWidthHeight(Size,Form,TmpValue,_Ignore)
		),
		Value = TmpValue
	).

%%getWidthSeDH/2
% getWidth(+Object,-Value)
% gets an Object and returns the height of the object
% depending on the oreintation of the object
getWidthSeDH(Object,Value) :-
	findall(
		Values,
		getWidth(Object,Values),
		List
	),
	nth0(0,List,Value).
getWidth(Object,Value) :-
	hasSize(Object,Size),
	(pig(Object) ->
		pigSize(Size,Value);
		hasOrientation(Object,Orientation),
		hasForm(Object,Form),
		(Orientation = 'vertical' -> 
			objectWidthHeight(Size,Form,Value,_Ignore);
			objectWidthHeight(Size,Form,_Ignore,Value)
		)
	).

%%findAllPigsInStructureList/2
% findAllPigsInStructureList(+StructureList, -PigList)
findAllPigsInStructureList(StructureList, PigList) :-
	splitListIn(StructureList, Structure, RestOfStructureList),
	findAllPigsInStructureList(RestOfStructureList, RestOfPigList),
	findAllPigsInStructure(Structure, StructurePigList),
	append(StructurePigList, RestOfPigList, PigList).
findAllPigsInStructureList([], []).

%%findAllPigsInStructure/2
% findAllPigsInStructure(+Structure, -PigList)
findAllPigsInStructure(Structure, PigList) :-
	findall(
			Pig,
			(
				pig(Pig),
				belongsTo(Pig, Structure)
			),
			PigList).
			
%%getPlanRank/5
% getPlanRank(+FirOrderValue,+SecOrderValue,+ThiOrderValue,+FourOrderValue,+Strategy,-Value)
% gets the four Values for calculation and the strategy
% returns the Ranking for the plan
getPlanRankSeDH(FirValue,SecValue,ThiValue,FourValue,Strategy,Rank) :-
	rankingFactor(RankingFactor),
	rankingFactorForStrategy(Strategy,StrategyFactor),
	First 	is FirValue	* (RankingFactor^3),
	Second 	is SecValue	* (RankingFactor^2),
	Third 	is ThiValue	* (RankingFactor^1),
	Fourth 	is FourValue	* (RankingFactor^0),
	TmpRank is (First + Second + Third + Fourth),
	Rank 	is (TmpRank * StrategyFactor).

%%findHittablesInStructure/2
%findHittablesInStructure(+Structure, -HittablesList) 
%finds all hittables in given Structure and returns a List;)
findHittablesInStructure(Structure, HittablesList) :-
	findall(
		Hittable,
		(	
			object(Hittable),
			belongsTo(Hittable, Structure),
			isHittable(Hittable,true)
		),
		HittablesList
	), syso('Hittables', HittablesList).
	