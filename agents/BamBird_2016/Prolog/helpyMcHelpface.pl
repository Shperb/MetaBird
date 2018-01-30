%%savePlan/4
% savePlan(+Target, +Goal, +Origin, +Rank)
% saves a plan at the end of database
savePlan(Targets, Goals, Origin, Rank) :-
	(plan(Rank, Targets, Goals, Origin) -> true; assertz(plan(Rank, Targets, Goals, Origin))),
	sysoPlan(Targets, Goals, Origin, Rank).

retractAllPlans() :-
	retractall(plan(_A, _B, _C, _D)).

%%substractFrom/3
%substractFrom(+SubtractorList, +SubtrahendList, -ResultList)
%deletes all elements in SubtractorList from SubtrahendList and returns the resulting List
substractFrom(SubtractorList, SubtrahendList, ResultList):-
	splitListIn(SubtractorList, Substractor, ReducedSubtractorList),
	delete(SubtrahendList, Substractor, ReducedSubtrahendList),
	substractFrom(ReducedSubtractorList, ReducedSubtrahendList, ResultList).
	
splitListIn([Head|Tail], Head, Tail).
%splitListIn([], [], []).

%%kwikeSort/2.
%kwikeSort(+ListToSort, -SortedList).
%SortedList is an ordered permutation of ListToSort. (in ascending Order/LOWEST value first)
%Every List entry of ListToSort has to be comprised of:
%	[Worth (Value that will be compared by this algorithm) |
%	Content (arbitrary Number of List-Entries, like objects etc.)]
kwikeSort([],[]) :- !.
kwikeSort(ListToSort, SortedList) :-
	findall(
		Sorted,
		pkwikeSort(ListToSort, Sorted),
		TmpList		
	), nth0(0, TmpList, SortedList).
pkwikeSort([], []) :- !.
pkwikeSort([FirstEntry | Rest], SortedList):-
	pPartition(Rest, FirstEntry, Littles, Bigs),
	kwikeSort(Littles, Ls),
	kwikeSort(Bigs, Bs),
	append(Ls, [FirstEntry | Bs], SortedList).

pPartition([X|Xs], Y, [X|Ls], Bs) :-
	splitListIn(X, HeadX, _TailX),
	splitListIn(Y, HeadY, _TailY),
	HeadX =< HeadY,
	pPartition(Xs, Y, Ls, Bs).
pPartition([X|Xs], Y, Ls, [X|Bs]) :-
	splitListIn(X, HeadX, _TailX),
	splitListIn(Y, HeadY, _TailY),
	HeadX > HeadY,
	pPartition(Xs, Y, Ls, Bs).
pPartition([ ], _Y ,[ ], [ ]).

%%kwikeSortInverse/2.
%kwikeSortInverse(+ListToSort, -SortedList).
%as "kwikeSort", but SortedList is in descending order/HIGHEST value first.
kwikeSortInverse([],[]) :- !.
kwikeSortInverse([FirstEntry | Rest], SortedList) :-
	kwikeSort([FirstEntry | Rest], SortedListAscending),
	invertList(SortedListAscending, SortedList).

%%invertList/2
%invertList(+ListToInvert, -InvertedList)
%inverts list... Jesus says: "The last ones, will be the first ones"
invertList(ListToInvert, InvertedList) :-
	invertList(ListToInvert, [], InvertedList).
invertList([Head | Tail], Accumulator, InvertedList) :-
	invertList(Tail, [Head|Accumulator], InvertedList).
invertList([],Accumulator, Accumulator).