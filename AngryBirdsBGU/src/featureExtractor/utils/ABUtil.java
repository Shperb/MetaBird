package featureExtractor.utils;

import featureExtractor.demo.Node;
import ab.demo.other.Shot;
import featureExtractor.planner.TrajectoryPlanner;
import featureExtractor.vision.ABObject;
import featureExtractor.vision.Vision;
import featureExtractor.vision.real.shape.Poly;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static MetaAgent.MyLogger.log;

public class ABUtil {

	public static int gap = 5; //vision tolerance
	private static TrajectoryPlanner tp = new TrajectoryPlanner();
	private static Random randomGenerator = new Random();
	private static long rolling = 0;

	public static long getRollingItemsNum()
    {
        return rolling;
    }

	/*
	 * function to detect Scene objects
	 * needed for tree construction
	 */
	public static ABObject SceneDetection(Vision vision, List<ABObject> objects, List<ABObject> pigs, List<ABObject> PigsObjects, List<ABObject> tnts, List<Poly> hills) {
		// get all objects: wood, stone, ice
		List<ABObject> objectsReal = vision.findBlocksRealShape();
		List<ABObject> objectsMBR  = vision.findBlocksMBR();

		// Keep the representation which detects the objects correctly
		if(objectsReal.size() - objectsMBR.size() <= -10){
			objects.addAll(objectsMBR);
			log("Tree construction under MBR representation....");
		}
		else{
			log("Tree construction under REAL representation...");
			objects.addAll(objectsReal);
		}

		// detect tnts and add them to list used for tree construction
		tnts.addAll(vision.getMBRVision().findTNTs());
		for(int i=0; i<tnts.size(); i++)
			objects.add(tnts.get(i));

		// findRealShape() mis-detects Pigs, so we use findPigsMBR() for Pig detection
		pigs.addAll(vision.findPigsMBR());

		// pop "empty" and "unknown" objects from our list
		if(!objects.isEmpty()){
			for (int i=0; i<objects.size(); i++){
				String test = new String(objects.get(i).type.toString());

				if(objects.get(i).isEmpty()){
					//// System.out.println("Empty object removed....");
					objects.remove(i);
					i = i-1;
				}
				else if (test.equals("Unknown")){
					//// System.out.println("Unknown object removed....");
					objects.remove(i);
					i = i-1;
				}
				test = null;
			}
		}

		// add pigs and rolling stones in a list
		List<ABObject> PigsAndRolling = new ArrayList<ABObject>();
		if(!pigs.isEmpty())
			PigsAndRolling.addAll(pigs);

		if(!objects.isEmpty()) {
			for (int i=0; i<objects.size(); i++){
				String test = new String(objects.get(i).type.toString());

				if (objects.get(i).shape.toString().equals("Circle"))
				{
					if (!test.equals("Pig"))
					{
						rolling++;
					}
					if (test.equals("Stone"))
					{
						PigsAndRolling.add(objects.get(i));
					}
				}
				test = null;
			}
		}

		// find most distant pig or rolling stone in the scene
		// to make the most "right" Nodes infeasible
		ABObject mostDistantObj = null;
		if(!PigsAndRolling.isEmpty()){
			mostDistantObj = PigsAndRolling.get(0);

			for(int i=1; i<PigsAndRolling.size(); i++) {
				if(PigsAndRolling.get(i).getCenterX() > mostDistantObj.getCenterX()) {
					mostDistantObj = PigsAndRolling.get(i);
				}
			}
		}

		// list of objects used at tree creation
		PigsObjects.addAll(pigs);
		PigsObjects.addAll(objects);

		// Discover hills inside the scene
		List<ABObject> hillstmp = vision.findHills();

		for(int i=0; i<hillstmp.size(); i++)
			hills.add((Poly)hillstmp.get(i));

		hillstmp.clear();
		hillstmp = null;

		// return the most right object to make nodes infeasible
		return mostDistantObj;
	}

	/*
	 * my functions to allocate game Scene
	 */
	public static int findMinX(int xmin, List<ABObject> object){
		for (int i=0; i<object.size(); i++){
			ABObject tmp = object.get(i);
			if (tmp.x < xmin) {
				xmin = tmp.x;
			}
		}
		return xmin;
	}

	public static int findMinY(int ymin, List<ABObject> object){
		for (int i=0; i<object.size(); i++){
			ABObject tmp = object.get(i);
			if (tmp.y < ymin) {
				ymin= tmp.y;
			}
		}
		return ymin;
	}

	public static int findMaxX(int xmax, List<ABObject> object){
		for (int i=0; i<object.size(); i++){
			ABObject tmp = object.get(i);
			if ((tmp.x+tmp.width) > xmax) {
				xmax = tmp.x+tmp.width;
			}
		}
		return xmax;
	}

	public static int findMaxY(int ymax, List<ABObject> object){
		for (int i=0; i<object.size(); i++){
			ABObject tmp = object.get(i);

			if ((tmp.y + tmp.height) > ymax) {
				ymax = tmp.y+tmp.height;
			}
		}
		return ymax;
	}

	/*
	 * function to allocate game scene
	 * game scene includes all game objects
	 */
	public static Rectangle findOurRoom(List<ABObject> pigs, List<ABObject> objects){
		int xminP = 0, yminP = 0, xmaxP = 0, ymaxP = 0;
		int xminO = 0, yminO = 0, xmaxO = 0, ymaxO = 0;
		int xmin = 100000, ymin = 100000, xmax = -1, ymax = -1;

		if (pigs !=null){
			xminP = findMinX(xmin,pigs);
			yminP = findMinY(ymin,pigs);
			xmaxP = findMaxX(xmax,pigs);
			ymaxP = findMaxY(ymax,pigs);
		}

		if (objects !=null){
			xminO = findMinX(xmin,objects);
			yminO = findMinY(ymin,objects);
			xmaxO = findMaxX(xmax,objects);
			ymaxO = findMaxY(ymax,objects);
		}

		if(yminP < yminO)
			ymin = yminP;
		else
			ymin = yminO;

		if(xminP < xminO)
			xmin = xminP;
		else
			xmin = xminO;

		if(xmaxP > xmaxO)
			xmax = xmaxP;
		else
			xmax = xmaxO;

		if(ymaxP > ymaxO)
			ymax = ymaxP;
		else
			ymax = ymaxO;

		// compute width and height of our room
		int h = ymax-ymin;
		int w = xmax-xmin;

		return (new Rectangle(xmin,ymin,w,h));
	}

	// If o1 supports o2, return true. ACTUALLY this is modified - so it checks if it is underneath and not only support!
	public static boolean isSupport(ABObject o2, ABObject o1)
	{
		if(o2.x == o1.x && o2.y == o1.y && o2.width == o1.width && o2.height == o1.height)
			return true; //o1 supports itself

		int ex_o1 = o1.x + o1.width;
		int ex_o2 = o2.x + o2.width;

		int ey_o2 = o2.y + o2.height;
		if(!( o2.x - ex_o1  > gap || o1.x - ex_o2 > gap ))
			return true;

		return false;
	}

	// If o1 touches o2 from its side, return true.
	public static boolean isTouching(ABObject o2, ABObject o1)
	{
		if(o2.x == o1.x && o2.y == o1.y && o2.width == o1.width && o2.height == o1.height)
			return true; //o1 touches itself

		int ex_o1 = o1.x + o1.width;
		int ex_o2 = o2.x + o2.width;

		int ey_o2 = o2.y + o2.height;
		if(!(o2.x - ex_o1  > gap || o1.x - ex_o2 > gap ))
			return true;

		return false;
	}

    // return the area/territory of an object
    public static long ObjectTerritory(ABObject o1)
    {
        return (o1.x+o1.width)*(o1.y+o1.height);
    }

	//Return true if the target can be hit by releasing the bird at the specified release point
	public static boolean isReachable(Vision vision, Point target, Shot shot)
	{
		//test whether the trajectory can pass the target without considering obstructions
		Point releasePoint = new Point(shot.getX() + shot.getDx(), shot.getY() + shot.getDy());
		int traY = tp.getYCoordinate(vision.findSlingshotMBR(), releasePoint, target.x);
		if (Math.abs(traY - target.y) > 100)
		{
			//// System.out.println(Math.abs(traY - target.y));
			return false;
		}
		boolean result = true;
		List<Point> points = tp.predictTrajectory(vision.findSlingshotMBR(), releasePoint);
		for(Point point: points)
		{
			if(point.x < 840 && point.y < 480 && point.y > 100 && point.x > 400)
				for(ABObject ab: vision.findBlocksMBR())
				{
					if(
							((ab.contains(point) && !ab.contains(target))||Math.abs(vision.getMBRVision()._scene[point.y][point.x] - 72 ) < 10)
									&& point.x < target.x
							)
						return false;
				}

		}
		return result;
	}

	// our modified "Bubblesort" helps to sort each level's nodes, according to their x coordinate
	// and not only, it is also used to order the children and parents, because we're interested in order
	public static void bubblesort(ArrayList<Node> list){
		int n = list.size();

		for (int i=0; i<n; i++){
			for (int j=1; j<(n-i); j++){
				int a = list.get(j-1).obj.x, b = list.get(j).obj.x;
				if(a > b){
					Node tmp1 = list.get(j);
					list.remove(j);
					Node temp = list.get(j-1);
					list.remove(j-1);
					list.add(j-1, tmp1);
					list.add(j,temp);
				}
			}
		}
	}
}
