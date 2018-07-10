:- [database].
:- [helpyMcHelpface].
%:- [level/level2].
:- [search/bearingObject].
:- [search/databaseHelpers].
/*delete when dijk works*/%:- [search/dijkstraTestLevel].
:- [search/wayfinder].
:- [search/graphGenerator].
:- [search/dijkstra].
:- [search/inferencialGrounding].
:- [search/quadrantSearch].
%:- [search/shortestPath].
:- [search/shortestPathChooser].
:- [search/totalHeight].
:- [strategy/destroyStructure].
:- [strategy/destroyStructureLow].
:- [strategy/domino].
:- [strategy/tnt].
:- [strategy/depot].
:- [strategy/pigContingency].
:- [tactic/minPenetration]. 


%:- [helpers].
%:- [functions_highArchitecture].
%:- [functions_database].
%:- [functions_efficientObjects].
%:- [functions_bearingObjects].


%%CodeConventions
% List 		-> findList
% Object 	-> getObject
% Bool 		-> isBool

% TODO get good rankingvalue, unload unused files
% TODO tnt need to use ranking
% TODO quadrantsearch lacks of good left/right assigning
% TODO give plans for more strategies
% TODO read TODOs in domino strategy
% TODO ----!!! minPenetration currently only uses objects with maxIntegrity, this is not good. Comments are missing. No behaviour for blue bird.

% Get the object to shoot at
%getBestShot(PlanList) :-
%	findAllPlans(PlanList).

%%sysoprint/2
% sysoprint(+String, +Object)
% can print a string and an object
syso(_String, _Object) :-
%	write(String),write(': '),writeln(Object),
	true.

%syso for Plan saving
sysoPlan(_Targets, _Goals, _Origin, _Rank) :-
%	write(Origin),write('-Plan saved: '),write(Targets), write(Goals), write('. Rank: '),writeln(Rank),
	true.

main :-
	initiateProlog().

initiateProlog() :-
	read(Filename),
	catch(consult(Filename),writeln('incorrect File Name'),initiateProlog()),
	findAllPlans(AllPlans),
	writeln(AllPlans),
	flush_output(),
	halt.
	
% Test functions
%getBestShotLists(_Object).
	
%getBestShotTerms(_Object).

findAllPlans(FinalPlanList) :-
%	pclearAllPlans(),
	psaveAllPlans(),
%	retract(plan(0,dummy,dummy,dummy)),
	findall(
		Plan,
		(
			plan(Rank, Target,Goal,_Strategy),
%			syso(strategy,Strategy),
%			syso(rank,Rank),
%			syso(targets,Target),
%			syso(goal,Goal),
			Plan = [Rank, Target, Goal]
%			syso(Plan, plan)
		),
		PlanList),
	sort(PlanList,SortPlanList),
	kwikeSortInverse(SortPlanList, SortedPlanList),
%	syso('Sorted', SortedPlanList),
	pDeleteRankValues(SortedPlanList, FinalPlanList),
	syso('FinalPlanList',FinalPlanList).

pDeleteRankValues([],[]) :- !.
pDeleteRankValues(PlanListWithRankValues, PlanList) :-
	splitListIn(PlanListWithRankValues, Plan, RestOfPlanListWithRankValues),
	splitListIn(Plan, _RankValue, Rest),
	syso(rest, Rest),
	pDeleteRankValues(RestOfPlanListWithRankValues, RestOfPlanList),
	append([Rest], RestOfPlanList, PlanList).

psaveAllPlans() :-
	doInferencialGroundingSeIG(),
	syso('TNTStrategy','started'),
	(
		(savePlansForTntStrategyStTNT()) ->
			syso('TNTStrategy','finished');
			syso('TNTStrategy','faild')
	),
	% works just for maps with no or with direct hittable tnt
	syso('DepotStrategy','started'),
	(
		(savePlansForDepotStrategyStDe()) ->
			syso('DepotStrategy','finished');
			syso('DepotStrategy','faild')
	),
	% collapsesInDirection didnt work
%	syso('DestroyLowStrategy','started'),
%	(
%		(saveDestroyStructurePlansLowStDS()) ->
%			syso('DestroyLowStrategy','finished');
%			syso('DestroyLowStrategy','faild')
%	),
	syso('DominoStrategy','started'),
	(
		(findDominoStrategyStDo()) ->
			syso('DominoStrategy','finished');
			syso('DominoStrategy','faild')
	),
	syso('DestroyHighStrategy','started'),
	(
		(saveDestroyStructurePlansStDS()) ->
			syso('DestroyHighStrategy','finished');
			syso('DestroyHighStrategy','faild')
	),
	syso('PigStrategy','started'),
	(
		(findPigContingencyPlansStPc()) ->
			syso('PigStrategy','finished');
			syso('PigStrategy','faild')
	).

	
	

%pclearAllPlans() :-
%	retractall(plan(_A,_B,_C,_D)).