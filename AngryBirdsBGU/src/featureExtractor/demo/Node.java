package featureExtractor.demo;

import Jama.Matrix;
import featureExtractor.planner.TrajectoryPlanner;
import featureExtractor.vision.ABObject;

import java.awt.*;
import java.util.ArrayList;

public class Node
{
	
	public ABObject obj;				// object type
	public String type;					// merge objects
	public int level;			
	public ArrayList<Node> children;	// list for child/children of each Node
	public ArrayList<Node> parent;		// list for parent/s of each Node
	
	// for features
	public double myWeight = 0;				// Node's Area
	public double totalWeight = 0;				// Parents Cumulative Weight
	public double slingDistance = -1.00;		// Node's distance of slingshot
	public double NPdistance = 0;				// field to show us the distance from the nearest Pig or TNT
	public double relevantHeight = -1.00;
	public double topdown = 0;
	public double bottomup = 0;

	// Node's Feasibility
	public boolean reachable = true;
	public boolean sceneFeasible = true;		// field to show us if an object is in the initial scene
	public boolean feasible = true; 
	public boolean WhiteFeasible = false;
	
	public Matrix PhiX;
	public double hitvalue = Double.NEGATIVE_INFINITY;			// expected reward		

	public ArrayList<TargetNode> targetNode;						// Points where a Node can be hit
	public double TapInterval = 0;									// field, that used for tbm is Pig or TNT, to compute right tap
	
	
	public Node(ABObject object, int lev){
		this.level = lev;
		this.obj = object;
		this.obj.angle = Math.toDegrees(this.obj.angle);
		this.children = new ArrayList<Node>();
		this.parent = new ArrayList<Node>();
		String type_ = object.type.toString();
		this.type = type_;
		this.hitvalue = 0;
		this.setmyWeight();
		this.targetNode = new ArrayList<TargetNode>();
	}

	public Node(String newType, ABObject object, int lev){
		this.level = lev;
		this.obj = object;
		this.children = new ArrayList<Node>();
		this.parent = new ArrayList<Node>();
		this.type = newType;
		this.hitvalue = 0;
		this.setmyWeight();
		this.targetNode = new ArrayList<TargetNode>();
	}
	
	// function to compute Phi*w for each Node
	public void WFproduct(Matrix w){
		// Check Matrices dimensions 
		//// System.out.println("Phi size(): Row: " + this.PhiX.getRowDimension() + " " + this.PhiX.getColumnDimension());
		//// System.out.println("w size(): Row: " + w.getRowDimension() + " " + w.getColumnDimension());
		Matrix hitvalueM = this.PhiX.times(w);
		this.hitvalue = hitvalueM.get(0,0);
	}
	
	/*
	 * function to choose targetPoint
	 */
	public TargetNode targetPointSelection(){	

		return(targetNode.get(0));
	}

	private void setmyWeight(){
		int res, weight = 0;
		
		switch(this.obj.type.ordinal()){
			case 9 : 
				res = 1;	// Wood: 9 
				break;
			case 8 : 
				res = 1;	// Ice: 8 
				break;
			case 10 : 
				res = 1;	// Stone: 10
				break;
			case 11 : 
				res = 1;	// Tnt: 11 	TARGET
				break;	 
			case 7 : 
				res = 1;	// Pig: 7	TARGET
				this.obj.area = this.obj.width*this.obj.height;		// findPigsMBR() used
				break; 
			case 12: 
				res = 0;	// Root: 12
				break;
			case 14: 
				res = -1;
				// // System.out.println(this.type);
				// // System.out.println("unknown type of node...");
				break;
			default : 
				res = 10;
				// // System.out.println("Default case in setmyWeight()...");
				break;
		}
		
		// find area in MBR representation 
		if(this.obj.area == 0){
			this.obj.area = this.obj.width*this.obj.height;
		}
		weight = this.obj.area*res;	
		this.myWeight = (double) weight;
	}

	public double SlingDistance(Rectangle slingshot){
		Point NodeC = this.obj.getCenter();
		Point slingC;
		double dist = -1;
		
		if (slingshot != null){
			slingC = new Point((int)slingshot.getCenterX(), (int)slingshot.getCenterY());
		}
		else{
			// System.out.println("there is no slingshot... distance = -1");
			return(dist);
		}
		
		dist = distance(NodeC, slingC);
		return(dist);
	}
	
	private double distance(Point p1, Point p2) {
		return Math.sqrt((double) ((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y)));
	}
	
	public boolean areBrothers(Node node, Node father){
		boolean brother = false;
		int bro = 0;
		
		if (father.children.contains(this))
			bro++;
		
		if (father.children.contains(node))
			bro++;
		
		if (bro == 2)
			brother = true;
		
		return(brother);
	}
	
	static public void freeNode(Node node){
		
		for(int i=0; i<node.targetNode.size(); i++)
			TargetNode.freeTargetNode(node.targetNode.get(i));
		
		node.targetNode.clear();
		node.children.clear();
		node.parent.clear();
		node = null;
	}

}