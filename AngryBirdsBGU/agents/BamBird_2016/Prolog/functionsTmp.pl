:- [database].
:- [helpyMcHelpface].
:- [level/level24].
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

pspecialOutpot(String,Object) :-
	write(String),write(': '),writeln(Object),
	true.

main :-
	initiateProlog().

initiateProlog() :-
	read(Filename),
	catch(consult(Filename),writeln('incorrect File Name'),initiateProlog()),
	findAllPlans(AllPlans),
	writeln(AllPlans),
	flush_output(),
	initiateProlog().
	
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
	pspecialOutpot('TNTStrategy','started'),
	(
		(savePlansForTntStrategyStTNT()) ->
			pspecialOutpot('TNTStrategy','finished');
			pspecialOutpot('TNTStrategy','faild')
	),
	% works just for maps with no or with direct hittable tnt
	pspecialOutpot('DepotStrategy','started'),
	(
		(savePlansForDepotStrategyStDe()) ->
			pspecialOutpot('DepotStrategy','finished');
			pspecialOutpot('DepotStrategy','faild')
	),
	% collapsesInDirection didnt work
	pspecialOutpot('DestroyLowStrategy','started'),
	(
		(saveDestroyStructurePlansLowStDS()) ->
			pspecialOutpot('DestroyLowStrategy','finished');
			pspecialOutpot('DestroyLowStrategy','faild')
	),
	pspecialOutpot('DominoStrategy','started'),
	(
		(findDominoStrategyStDo()) ->
			pspecialOutpot('DominoStrategy','finished');
			pspecialOutpot('DominoStrategy','faild')
	),
%	pspecialOutpot('DestroyHighStrategy','started'),
%	(
%		(saveDestroyStructurePlansStDS()) ->
%			pspecialOutpot('DestroyHighStrategy','finished');
%			pspecialOutpot('DestroyHighStrategy','faild')
%	),
	pspecialOutpot('PigStrategy','started'),
	(
		(findPigContingencyPlansStPc()) ->
			pspecialOutpot('PigStrategy','finished');
			pspecialOutpot('PigStrategy','faild')
	).

	
	

%pclearAllPlans() :-
%	retractall(plan(_A,_B,_C,_D)).