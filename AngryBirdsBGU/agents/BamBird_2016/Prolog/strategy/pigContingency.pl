findPigContingencyPlansStPc() :-
	findall(
		_PigPlan,
		(
			pig(Pig),
			isHittable(Pig, true),
			%Maybe add some advanced categorisation
			%Atm its only the 1 pig;)
			getPlanRankSeDH(1,0,0,0, pigCont, Rank),
			birdOrder(Bird, 0),
			(	(hasColor(Bird, blue)) ->
				savePlan([Pig, Pig, Pig], [Pig], pigCont, Rank);
				savePlan([Pig], [Pig], pigCont, Rank)
			)
		),
		_PigPlans
	).