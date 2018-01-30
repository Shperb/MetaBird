% EFFICIENT OBJECTS
% Get a List of objects with one material
getEfficientObjects(BestObjectList) :-
	birdOrder(Bird, 0),
	hasColor(Bird,Color),
	getOrderdEffRates(EffRates),
	nth0(2,EffRates,BestRate),
	findall(
		Object,
		(
			object(Object),
			isHittable(Object,isHittable),
			isHittable=true,
			hasMaterial(Object,Material),
			efficiencyBirdMaterial(Color,Material,Rate),
			Rate=BestRate
		),
		BestObjectList
	).
getEfficientObjects(BestObjectList,Level) :-
	birdOrder(Bird, 0),
	hasColor(Bird,Color),
	getOrderdEffRates(EffRates),
	(Level=2 ->
		nth0(1,EffRates,BestRate);
		nth0(0,EffRates,BestRate)
	),
	findall(
		Object,
		(
			object(Object),
			isHittable(Object,isHittable),
			isHittable=true,
			hasMaterial(Object,Material),
			efficiencyBirdMaterial(Color,Material,Rate),
			Rate=BestRate
		),
		BestObjectList
	).
	
% Get the List with best objects for the actual bird
getEfficientObjectList(BestObjectList) :-
	getEfficientObjects(FirstOrderList),
	length(FirstOrderList,FirstLength),
	(FirstLength>0 ->
		BestObjectList=FirstOrderList;
		getEfficientObjects(SecOrderList,2),
		length(SecOrderList,SecLength),
		(SecLength>0 ->
			BestObjectList=SecOrderList;
			getEfficientObjects(BestObjectList,3)
		)
	).
