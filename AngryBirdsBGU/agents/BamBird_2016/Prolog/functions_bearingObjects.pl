% BEARING OBJECTS

% stands or lies an object directly on another object
onObject(Object,OnObject) :-
%	standsOn(Object,OnObject);
%	liesOn(Object,OnObject);
	isOn(Object,OnObject).
% bears an Object another Object
allObjectsOnObjects(X,OnObject) :-
	onObject(X,OnObject);
	(
		onObject(X,Parent),
		allObjectsOnObjects(Parent,OnObject)
	).
% how many Objects are bearing by an Object
getNumberOfAllObjectsOnObject(X,OnObject) :-
	findall(
		Object,
		allObjectsOnObjects(Object,OnObject),
		List
	),
	sort(List,OrderedList),
	length(OrderedList,X).
%
getAllNumbersOfBearingObjects(List) :-
	findall(
		Number,
		(
			object(Object),
			%isDestroyable(Object,Destroyable),
			%Destroyable=true,
			(
				hasMaterial(Object, Material);
				pig(Object)
			),			
			isHittable(Object,Hittable),
			Hittable=true,
			getNumberOfAllObjectsOnObject(Number,Object)
		),
		TempList
	),
	sort(0,@>,TempList,List).
findTheBearingObjectsInMap(ObjectList) :-
	getAllNumbersOfBearingObjects(List),
	nth0(0,List,X),
	findall(
		Object,
		(
			object(Object),
			%isDestroyable(Object,Destroyable),
			%Destroyable=true,
			(
				hasMaterial(Object, Material);
				pig(Object)
			),
			isHittable(Object,Hittable),
			Hittable=true,
			getNumberOfAllObjectsOnObject(Number,Object),
			Number=X
		),
		ObjectList
	).
% Get all destructible, bearing objects
getBearingObjects(BearingObjectList) :-
	findall(
		Object,
		(
			object(Object),
			%isCollapsable(Object, true),
			(
				hasMaterial(Object, Material);
				pig(Object)
			),
			isHittable(Object, true),
			getNumberOfAllObjectsOnObject(X,Object)
		),
		ObjectList
	),
	sort(ObjectList, BearingObjectList).

getNumberOfObjectsOfStructure(Structure,OrderdList) :-
	findall(
		Number,
		(
			belongsTo(Objects,Structure),
			getNumberOfAllObjectsOnObject(Number,Objects)
		),
		List
	),
	sort(0,@>,List,OrderdList).
 
getBearingObjectOfStructure(Structure,BearingObjectList) :-
	getNumberOfObjectsOfStructure(Structure,Numbers),
	nth0(0,Numbers,BestNumber),
	findall(
		Objects,
		(
			belongsTo(Object,Structure),
			getNumberOfAllObjectsOnObject(ThisNumber,Object),
			ThisNumber=BestNumber,
			Objects=Object
		),
		BearingObjectList
	).
