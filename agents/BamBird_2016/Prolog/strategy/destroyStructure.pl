%%destroyStructureStDS/0
% destroyStructureStDS()
% 
saveDestroyStructurePlansStDS() :-
%saveDestroyStructurePlansStDS().
%saveDestroyStructurePlansStDSNOTWORKINGASDIJKSTRADOESNOTWORK() :-
	findall(
		Structure,
		(
			structure(Structure),
			pig(Pig),
			belongsTo(Pig, Structure)
		),
		List),
	length(List, ListLength),
	((ListLength > 0) ->
		pFindPlansForMinPenetrationTMinP(List)
		;
		true).
	%retractStuff.