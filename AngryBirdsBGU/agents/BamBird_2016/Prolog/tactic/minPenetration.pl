pFindPlansForMinPenetrationTMinP([]) :- !.
pFindPlansForMinPenetrationTMinP([Structure | RestOfStructures]) :-
	pSysoTaMinP('[TaMinP] Given StructureList', Structure),
	pFindPlansForStructureMinPenetrationTMinP(Structure),
	
	pSysoTaMinP('[TaMinP] Given StructureList', RestOfStructures),
	pFindPlansForMinPenetrationTMinP(RestOfStructures).
	
%%pFindPlansForStructureMinPenetrationTMinP/2
pFindPlansForStructureMinPenetrationTMinP(Structure) :-
	pSysoTaMinP('[TaMinP] Used Structure', Structure),
	pFindRelevantObjectsTMinP(Structure, RelevantObjectsList),
	pSysoTaMinP('[TaMinP] RelevantObject for structure', RelevantObjectsList),
	
	findPathsFromToHittablesForActiveBirdWithStandardLimitSeWf(RelevantObjectsList, PathsList),
	pSysoTaMinP('[TaMinP]Paths for minimal Penetration', PathsList),
	pSavePlansForStructureMinPenetrationTMinP(Structure, PathsList).

pSavePlansForStructureMinPenetrationTMinP(_Structure, []) :- !.
pSavePlansForStructureMinPenetrationTMinP(Structure, PathsList) :-
	length(PathsList, Check),
	((Check \== 0) ->
		findAllPigsInStructure(Structure, PigList),
		length(PigList, PigCount),
		splitListIn(PathsList, FirstPath, RestOfPathsList),
		pSavePlansForStructureMinPenetrationTMinP(Structure, RestOfPathsList),
		length(PathsList, MinorValue),
		splitListIn(FirstPath, PathValue, Path),
		length(Path, PathLength),
		((PathValue == 0) ->
			TmpPathValue is 10
			;
			TmpPathValue is (1/PathValue)),
		getPlanRankSeDH(PigCount, TmpPathValue, PathLength, MinorValue, minPen, RankValue),
		
		% Target = First Object in Path
		% Goal = Last Object in Path + Pigs
		nth0(0, Path, Target),
		GetValue is PathLength -1,
		nth0(GetValue, Path, GoalObject),
		append([GoalObject], PigList, Goal),
		savePlan(Target, Goal, minPen, RankValue)
		;
		true).
% lower half, integrity bigger than half of maxintegrity, destroyable by bird
pFindRelevantObjectsTMinP(Structure, ObjectList) :-
		findall(
			Object,
			(
				belongsTo(Object,Structure),
				hasIntegrity(Object,IntegrityValue),
				hasMaxIntegrity(Structure,MaxIntegrity),
				IntegrityValue=MaxIntegrity,
	%			not(IntegrityValue=0),
				hasSize(Object,Size),
				hasForm(Object,Form),
				hasMaterial(Object,Material),
				birdOrder(Bird,0),
				hasColor(Bird,Color),
				efficiencySizeForm(Size,Form,Material,EffObValue),
				efficiencyBirdMaterial(Color,Material,EffBiValue),
				EffValue=EffObValue*EffBiValue,

				EffValue<1
			),
			ObjectList).
			
				
pSysoTaMinP(_Text, _Value) :- %syso(_Text, _Value),
	true.