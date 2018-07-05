
%Get efficiency of current Bird
getAllEffRatesForCurrentBird(List) :-
	bird(Bird),
	isInSlingshot(Bird),
	findall(
		Value,
		(
			object(Object),
			isCollapsable(Object, IsDestroyable),
			IsDestroyable=true,
			isHittable(Object, IsHittable),
			IsHittable=true,
			getEffRate(Bird,Object,Value),
			[Object,Value]=Value1,
			Value1=Value
		),
		TmpList
	),
	write(TmpList),
	sort(0,@>,TmpList,List).

getTest(X) :-
	getAllEffRatesForCurrentBird(List),
	find(X,List).
	
% get EfficiencyRate of one Object
getEffRate(Bird,Object,Value) :-
	hasColor(Bird,Color),
	hasMaterial(Object,Material),
	hasSize(Object,Size),
	hasForm(Object,Form),
	efficiencyBirdMaterial(Color,Material,MaterialValue),
	efficiencyForm(Form,FormValue),
	(pig(Object) ->
		efficiencyPig(Size,SizeValue);
		efficiencySize(Size,SizeValue)
	),
	Value is MaterialValue*FormValue*SizeValue.
	
	
