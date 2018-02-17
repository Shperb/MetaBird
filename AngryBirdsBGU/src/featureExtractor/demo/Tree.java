package featureExtractor.demo;

import DB.Features;
import Jama.Matrix;
import featureExtractor.planner.TrajectoryPlanner;
import featureExtractor.vision.ABObject;
import featureExtractor.vision.ABType;
import org.json.simple.JSONObject;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import static featureExtractor.utils.ABUtil.bubblesort;
import static featureExtractor.utils.ABUtil.isSupport;

public class Tree
{
    private ArrayList<ArrayList<Node>> level_list;            // list for tree
    private Random ran = new Random();
    private TrajectoryPlanner tp;
    private List<ABObject> birds;                       // extra list for birds

    private int pigs = 0;
    private double height = 0;
    private double width = 0;
    private int objects = 0;
    private int StonedPigs = 0;

    // class's constructor
    public Tree(TrajectoryPlanner tp_)
    {
        tp = tp_;
        level_list = new ArrayList<ArrayList<Node>>();
        birds = new ArrayList<ABObject>();
    }

    //Getters
    //ToAdd - total number of birds, how many from each type of birds, how many types
    //ToAdd -
    //ToAdd - See if i can check the difference in pigs size
    //ToAdd - getter how many of the pigs with stone

    public long getNumOfRedBirds()
    {
        return BirdsListIterator(o -> o.type.name().equals("RedBird"));
    }

    public long getNumOfYellowBirds()
    {
        return BirdsListIterator(o -> o.type.name().equals("YellowBird"));
    }

    public long getNumOfBlueBirds()
    {
        return BirdsListIterator(o -> o.type.name().equals("BlueBird"));
    }

    public long getNumOfBlackBirds()
    {
        return BirdsListIterator(o -> o.type.name().equals("BlackBird"));
    }

    public long getNumOfWhiteBirds()
    {
        return BirdsListIterator(o -> o.type.name().equals("WhiteBird"));
    }


    public int getNumOfBirds()
    {
        return birds.size();
    }

    public int getStonedPigs()
    {
        return this.StonedPigs;
    }

    public int getNoStonePigs()
    {
        return this.pigs - this.StonedPigs;
    }

    public long getIce()
    {
        return LevelListIterator(o -> o.type.equals("Ice"));
    }

    public long getWood()
    {
        return LevelListIterator(o -> o.type.equals("Wood"));
    }

    public long getStone()
    {
        return LevelListIterator(o -> o.type.equals("Stone"));
    }

    public double getDensity()
    {
        return this.pigs / (this.height * this.width);
    }

    public int getNumberOfObjects()
    {
        return this.objects;
    }

    public double getHeight()
    {
        return this.height;
    }

    public int getNumOfPigs()
    {
        return this.pigs;
    }

    public double getWidth()
    {
        return this.width;
    }

    public double getFarmostDistanceFromSling()
    {
        return rightmost().obj.getX();
    }

    public double getShortestDistanceFromSling()
    {
        return leftmost().obj.getX();
    }

    public int getNumOfBlocks()
    {
        ArrayList<Node> potentialBlocks = new ArrayList<Node>();
        int count = 0;
        int rootx = LevelSize() - 1, rooty = 0;
        ArrayList<Node> rootChildren = GetElement(rootx, rooty).children;
        //Loop over children
        for (Node child : rootChildren)
        {
            int shared = 0;
            //if first or empty supporters skip this step
            if (potentialBlocks.size() != 0)
            {
                shared = compareSupporters(child, level_list, potentialBlocks);
            }

            //if there are no shared supporters, assume new block
            if (shared == 0)
            {
                count++;
                getSupporters(child, level_list, potentialBlocks);
            }
        }
        return count;
    }


    /*
     * help function to construct level_list
     * 	scan game scene in bottom-up fashion to construct tree
     */
    private boolean findIntersectionList(List<ABObject> objects, int level, Line2D line)
    {
        boolean flag = false;
        ABObject check = null;

        if (!objects.isEmpty())
        {
            for (int i = 0; i < objects.size(); i++)
            {
                try
                {
                    check = objects.get(i);
                } catch (IndexOutOfBoundsException e)
                {
                    System.err.println("Caught get exception in findIntersectionList(): " + e.getMessage());
                }
                Rectangle2D tmp = new Rectangle2D.Double(check.getX(), check.getY(), check.getWidth(), check.getHeight());
                if (tmp.intersectsLine(line))
                {
                    level_list.get(level).add(new Node(check, level));
                    objects.remove(i);
                    i = i - 1;
                    flag = true;
                }
            }
        }
        return flag;
    }

    /*
     * function to create level-list in its initial form
     */
    private void create_level_list(Rectangle ourRoom, List<ABObject> Objects_for_Tree, int xmin, int ymin, int root_width)
    {
        int level = 0;
        boolean breakFlag = false;

        // level-list construction
        while (true)
        {
            // find the object which is located near to the ground
            int maxobj = findMaxObject(Objects_for_Tree);
            double ymax = Objects_for_Tree.get(maxobj).getCenterY();

            // Line2D to see what intersect in each level of the list we want to create
            Line2D.Double line = new Line2D.Double((double) 0, ymax, (double) 840, ymax);
            //// System.out.println("x1: " + line.getX1() + " x2: " + line.getX2() + " y1: " + line.getY1() + " y2: " + line.getY2());

            level_list.add(level, new ArrayList<Node>());
            boolean interflag = findIntersectionList(Objects_for_Tree, level, line);

            if (!interflag)
            {
                // System.out.println("No INTERSECTION: Object: " + Objects_for_Tree.get(maxobj).type.toString() + " x: " + Objects_for_Tree.get(maxobj).getCenterX()
                //        + " y: " + Objects_for_Tree.get(maxobj).getCenterY() + " width: " + Objects_for_Tree.get(maxobj).width + " height: " + Objects_for_Tree.get(maxobj).height);

                // remove object to construct our Tree properly
                Objects_for_Tree.remove(maxobj);
                level_list.remove(level);
                level--;
            }

            // in case that the "no-intersection" is the last element of Objects_for_Tree
            if (Objects_for_Tree.isEmpty())
            {        // termination means that all objects inserted in our list
                level++;
                breakFlag = true;
                break;
            } else
            {
                level++;
            }
        }

        // After level_list construction, "Root" Node must be inserted on top of level_list
        level_list.add(level, new ArrayList<Node>());
        Rectangle root = new Rectangle(0, ymin - 2, 8000, 1);

        level_list.get(level).add(new Node(new ABObject(root, ABType.Root), level));

        // Error in level_list construction
        if (!breakFlag)
        {
            // System.out.println("While didn't break normal.....");
            // System.out.println("Something's wrong... It musn't be printed....");
            mySleep(2000);
        } else
        {
            // System.out.println("List construction completed: While broke normal!");
        }
    }

    // help function to construct level_list
    private int findMaxObject(List<ABObject> list)
    {
        int tmp = 0;
        double ymax = list.get(0).getCenterY();
        for (int i = 1; i < list.size(); i++)
        {
            if (list.get(i).getCenterY() > ymax)
            {
                ymax = list.get(i).getCenterY();
                tmp = i;
            }
        }
        return tmp;
    }

    // sort each level according to obj.x coordinates, small to large
    private void OrderLevelList()
    {
        for (int i = 0; i < level_list.size(); i++)
        {
            if (level_list.get(i).size() > 1)
            {
                bubblesort(level_list.get(i));
            }
        }
    }

    public String toString(int typeOfFeatures)
    {
        String ans ="";
        ans = concatStringsAndNextLine(ans,"PRINT CONSTRUCTED TREE");
        ans = concatStringsAndNextLine(ans,"TOTAL LEVELS: " + level_list.size());
        ans = concatStringsAndNextLine(ans,"-----------------------------------------------------------------------------------------------------------------------------------------");
        for (int i = level_list.size() - 1; i >= 0; i--)
        {
            ans = concatStringsAndNextLine(ans,"level: " + i + " size: " + level_list.get(i).size());
            for (int j = 0; j < level_list.get(i).size(); j++)
            {
                Node tmp = level_list.get(i).get(j);

                if (typeOfFeatures == 0)
                { // Relevant Height feature
                    ans = concatStringsAndNextLine(ans,"Node " + j + " type: " + tmp.obj.type + " CenterX: " + tmp.obj.getCenterX() + " CenterY: " + tmp.obj.getCenterY()
                            + " PerWeight: " + tmp.myWeight + " TopDown: " + tmp.topdown + " BottomUp: " + tmp.bottomup + " RelHeight: " + tmp.relevantHeight + " npDistance: " + tmp.NPdistance);
                } else
                {        // Parents' Cumulative Weight
                    ans = concatStringsAndNextLine(ans,"Node " + j + " type: " + tmp.obj.type + " height: " + tmp.obj.height + " CenterX: " + tmp.obj.getCenterX() + " CenterY: " + tmp.obj.getCenterY()
                            + " PerWeight: " + tmp.myWeight + " totalWeight: " + tmp.totalWeight + " npDistance: " + tmp.NPdistance
                            + " Feasible: " + tmp.feasible + " reachable: " + tmp.reachable + " Points: " + tmp.targetNode.size());
                }
                ans = concatStringsAndNextLine(ans,"-----------------------------------------------------------------------------------------------------------------------------------------");
            }
        }

        ans = concatStringsAndNextLine(ans,"END OF LIST PRINTING");
        return ans;
    }

    public DB.Features getFeatures(){
        Features features = new Features();

        features.NumBlocks = getNumOfBlocks();
        features.targetWidth = getWidth();
        features.targetHeight = getHeight();
        features.closestObjDist = getShortestDistanceFromSling();
        features.farthestObjDist = getFarmostDistanceFromSling();
        features.density = getDensity();
        features.numObjects = getNumberOfObjects();
        features.iceObjects = getIce();
        features.woodObjects = getWood();
        features.stoneObjects = getStone();
        features.numPigs = getNumOfPigs();
        features.helmetPigs = getStonedPigs();
        features.noHelmetPigs = getNoStonePigs();
        features.numBirds = getNumOfBirds();
        features.numRedBirds = getNumOfRedBirds();
        features.numYellowBirds = getNumOfYellowBirds();
        features.numBlueBirds = getNumOfBlueBirds();
        features.numBlackBirds = getNumOfBlackBirds();
        features.numWhiteBirds = getNumOfWhiteBirds();

        return features;
    }
    
    private String concatStringsAndNextLine (String s,String b){
        return s + b +"\n"; 
    }

    /*
     * function to merge Tree's Nodes
     * according to their material and geometrical properties
     */
    private void MergeNodesSameLevel()
    {
        Node toInsert = null;
        for (int i = 0; i < level_list.size() - 1; i++)
        {
            int m = level_list.get(i).size();
            for (int j = 0; j < m - 1; j++)
            {                    // m-1: check next Node
                Node tmp = level_list.get(i).get(j);
                Node next = level_list.get(i).get(j + 1);
                boolean containsPig = false;

                boolean typeflag = false, Circleflag = false;

                if (tmp.type.equals(next.type))
                {
                    typeflag = true;
                }

                if (tmp.obj.shape.equals("Circle") || (next.obj.shape.equals("Circle")))
                {
                    Circleflag = true;
                }

                if ((tmp.type.contains("Pig")) || (next.type.contains("Pig")))
                {
                    containsPig = true;
                }

                // to check geometrical properties
                if ((!containsPig) && (typeflag) && (!Circleflag))
                {
                    boolean createNode = false;
                    int compY = Math.abs(tmp.obj.y - next.obj.y);
                    int compH = Math.abs(tmp.obj.height - next.obj.height);
                    if (compY <= 10)
                    {
                        if (compH <= 5)
                        {
                            int compare = tmp.obj.x + tmp.obj.width;
                            int compW = Math.abs(next.obj.x - compare);
                            if (compW <= 10)
                            {
                                createNode = true;
                                String newType = new String(tmp.type.concat(next.type));
                                int toInWidth = tmp.obj.width + next.obj.width;
                                int toInHeight = tmp.obj.height;
                                Rectangle RtoInsertRect = new Rectangle(tmp.obj.x, tmp.obj.y, toInWidth, toInHeight);
                                ABObject ABOtoInsert = new ABObject(RtoInsertRect, tmp.obj.type);
                                ABOtoInsert.angle = tmp.obj.angle;
                                ABOtoInsert.area = tmp.obj.area + next.obj.area;
                                ABOtoInsert.shape = tmp.obj.shape;
                                toInsert = new Node(newType, ABOtoInsert, i);
                            }
                        }
                    }

                    // Node creation and insertion in right position
                    if (createNode)
                    {
                        // pop and insertion
                        level_list.get(i).remove(j + 1);
                        level_list.get(i).remove(j);
                        level_list.get(i).add(j, toInsert);
                        m = level_list.get(i).size();
                        j--;
                        toInsert = null;
                    }
                }
            }
        }
    }

    private void MergeNodesDifferentLevel()
    {
        Node toInsert = null;
        int n = level_list.size();

        for (int i = 0; i < level_list.size() - 1; i++)
        {
            for (int j = 0; j < level_list.get(i).size(); j++)
            {
                Node thisLevelNode = level_list.get(i).get(j);
                int mNextLevel = level_list.get(i + 1).size();
                boolean createNode = false;

                for (int k = 0; k < level_list.get(i + 1).size(); k++)
                {
                    Node nextLevelNode = level_list.get(i + 1).get(k);

                    boolean Circleflag = false;
                    if (thisLevelNode.obj.shape.equals("Circle") || nextLevelNode.obj.shape.equals("Circle"))
                        Circleflag = true;

                    boolean containsPig = false;
                    if ((thisLevelNode.type.contains("Pig")) || (nextLevelNode.type.contains("Pig")))
                    {
                        containsPig = true;
                    }

                    // no need to check the other Nodes if next condition returns true
                    if (nextLevelNode.obj.x > thisLevelNode.obj.x)
                        break;

                    // to avoid merging something like StoneStone--WoodStone
                    String thisLevelNodeType = thisLevelNode.obj.type.toString();
                    String nextLevelNodeType = nextLevelNode.obj.type.toString();

                    if (!thisLevelNodeType.equals(nextLevelNodeType))
                    {
                        break;
                    }

                    // to check geometrical properties
                    if ((!containsPig) && (!Circleflag))
                    {
                        int compX = Math.abs(nextLevelNode.obj.x - thisLevelNode.obj.x);
                        int compW = Math.abs(nextLevelNode.obj.width - thisLevelNode.obj.width);
                        if (compX <= 5)
                        {
                            if (compW <= 5)
                            {
                                int compare = nextLevelNode.obj.y + nextLevelNode.obj.height;
                                int compF = Math.abs(thisLevelNode.obj.y - compare);
                                if (compF <= 5)
                                {
                                    createNode = true;
                                    String newType = thisLevelNode.type.concat("--" + nextLevelNode.type);
                                    int newWidth = thisLevelNode.obj.width;
                                    int newHeight = thisLevelNode.obj.height + nextLevelNode.obj.height;
                                    Rectangle RtoInsert = new Rectangle(nextLevelNode.obj.x, nextLevelNode.obj.y, newWidth, newHeight);
                                    ABObject ABOtoInsert = new ABObject(RtoInsert, thisLevelNode.obj.type);
                                    ABOtoInsert.angle = thisLevelNode.obj.angle;
                                    ABOtoInsert.area = thisLevelNode.obj.area + nextLevelNode.obj.area;
                                    ABOtoInsert.shape = thisLevelNode.obj.shape;
                                    toInsert = new Node(newType, ABOtoInsert, i);
                                }
                            }

                            // Node creation and insertion in right position
                            if (createNode)
                            {
                                level_list.get(i).remove(j);
                                level_list.get(i + 1).remove(k);
                                level_list.get(i).add(j, toInsert);
                                mNextLevel = level_list.get(i + 1).size();
                                toInsert = null;
                                if (mNextLevel == 0)
                                {
                                    level_list.remove(i + 1);
                                    n = level_list.size();
                                    for (int a = 0; a < n; a++)
                                    {
                                        for (int b = 0; b < level_list.get(a).size(); b++)
                                        {
                                            level_list.get(a).get(b).level = a;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // sort each level according to obj.x coordinates, small to large
    private void OrderChildren()
    {
        for (int i = 1; i < level_list.size(); i++)
        {
            int m = level_list.get(i).size();

            for (int j = 0; j < m; j++)
            {
                int k = level_list.get(i).get(j).children.size();
                for (int l = 0; l < k; l++)
                {
                    bubblesort(level_list.get(i).get(j).children);
                }
            }
        }
    }

    // for each Node print Children
    private void PrintChildren()
    {
        // System.out.println("CHILDREN PRINT ");
        for (int i = 1; i < level_list.size(); i++)
        {
            // System.out.println("LEVEL: " + i);
            for (int j = 0; j < level_list.get(i).size(); j++)
            {
                Node tmp = level_list.get(i).get(j);
                int child = tmp.children.size();

                if (child > 0)
                {
                    // System.out.println("Node " + j + " from level: " + tmp.level + " ABType: " + tmp.obj.type + " type: " + tmp.type + " has ---" + child + "--- child/children");
                    for (int k = 0; k < child; k++)
                    {
                        Node tmpCHILD = level_list.get(i).get(j).children.get(k);
                        // System.out.println(k + "--> Node from level " + tmpCHILD.level + " ABType: " + tmpCHILD.obj.type + " type: " + tmpCHILD.type + " x: " + tmpCHILD.obj.x);
                    }
                    // System.out.println(" ");
                } else
                {
                    // System.out.println("No Children");
                }
            }
        }
    }

    // sort each level according to obj.x coordinates, small to large
    private void OrderParents()
    {
        for (int i = 0; i < level_list.size(); i++)
        {
            int m = level_list.get(i).size();

            for (int j = 0; j < m; j++)
            {
                int k = level_list.get(i).get(j).parent.size();
                for (int l = 0; l < k; l++)
                {
                    bubblesort(level_list.get(i).get(j).parent);
                }
            }
        }
    }

    // for each Node print Parents
    private void PrintParents()
    {
        // System.out.println("PARENTS PRINT ");

        for (int i = 0; i < level_list.size(); i++)
        {
            // System.out.println("LEVEL --> " + i);
            for (int j = 0; j < level_list.get(i).size(); j++)
            {
                Node tmp = level_list.get(i).get(j);
                int par = tmp.parent.size();

                if (par > 0)
                {
                    // System.out.println("Node " + j + " from level " + tmp.level + " ABType: " + tmp.obj.type + " x: " + tmp.obj.x + " type: " + tmp.type + " has --" + par + "-- parents");
                    for (int k = 0; k < par; k++)
                    {
                        Node tmpPAR = level_list.get(i).get(j).parent.get(k);
                        // System.out.println("Node " + k + " from level " + tmpPAR.level + " ABType: " + tmpPAR.obj.type + " type: " + tmpPAR.type + " x: " + tmpPAR.obj.x);
                    }
                } else
                {
                    // System.out.println("ROOT: This is the root of our Tree, no father for him");
                }
            }
        }
    }

    /*
     *  function to construct our Tree
     */
    public void constructEdges(int ymin)
    {
        for (int i = 0; i < level_list.size(); i++)
        {
            for (int j = 0; j < level_list.get(i).size(); j++)
            {
                Node tmp = level_list.get(i).get(j);
                // helps to see the intersection between our objects
                Rectangle area = new Rectangle(tmp.obj.x, ymin - 4, tmp.obj.width + 1, tmp.obj.y - (ymin - 4));

                int next_level = i + 1;
                for (int a = next_level; a < level_list.size(); a++)
                {
                    boolean found_child = false;
                    for (int k = 0; k < level_list.get(a).size(); k++)
                    {
                        Node tmp_ = level_list.get(a).get(k);

                        boolean cond = area.intersects(tmp_.obj);
                        if (cond)
                        {
                            level_list.get(a).get(k).children.add(tmp);
                            level_list.get(i).get(j).parent.add(tmp_);
                            found_child = true;
                        }
                    }

                    if (found_child)
                    {
                        break;
                    }
                }
                area = null;
            }
        }
    }

    /*
     * function to detect Pig--Stone
     * Pig with a Hut-Stone
     */
    private void PigStone()
    {
        Node toInsert = null;

        for (int i = 0; i < level_list.size() - 1; i++)
        {
            for (int j = 0; j < level_list.get(i).size(); j++)
            {
                Node thisLevelNode = level_list.get(i).get(j);

                for (int k = 0; k < level_list.get(i + 1).size(); k++)
                {
                    Node nextLevelNode = level_list.get(i + 1).get(k);
                    boolean containsPigStone = false;

                    if ((thisLevelNode.type.contains("Pig")) && (nextLevelNode.type.contains("Stone")) && (nextLevelNode.obj.shape.toString().equals("Poly")))
                    {
                        if (thisLevelNode.obj.intersects(nextLevelNode.obj))
                        {
                            containsPigStone = true;
                        }
                    }

                    if (nextLevelNode.obj.x > thisLevelNode.obj.x)
                        break;

                    if (containsPigStone)
                    {
                        int compX = Math.abs(nextLevelNode.obj.x - thisLevelNode.obj.x);
                        if (compX <= 5)
                        {
                            String newType = thisLevelNode.type;            //.concat("---Stone");
                            int newWidth = thisLevelNode.obj.width;
                            int newHeight = thisLevelNode.obj.height + nextLevelNode.obj.height;
                            Rectangle RtoInsert = new Rectangle(nextLevelNode.obj.x, nextLevelNode.obj.y, newWidth, newHeight);
                            ABObject ABOtoInsert = new ABObject(RtoInsert, thisLevelNode.obj.type);
                            ABOtoInsert.angle = thisLevelNode.obj.angle;
                            ABOtoInsert.area = thisLevelNode.obj.area + nextLevelNode.obj.area;
                            ABOtoInsert.shape = thisLevelNode.obj.shape;
                            toInsert = new Node(newType, ABOtoInsert, i);
                            StonedPigs++; //count pigs with stone helmets!
                            //// System.out.println("Pig and Stone-Hut detected .....");

                            // Node creation and insertion in right position
                            level_list.get(i).remove(j);
                            level_list.get(i + 1).remove(k);
                            level_list.get(i).add(j, toInsert);
                            toInsert = null;
                            if (level_list.get(i + 1).size() == 0)
                            {
                                level_list.remove(i + 1);
                                for (int a = 0; a < level_list.size(); a++)
                                {
                                    for (int b = 0; b < level_list.get(a).size(); b++)
                                    {
                                        level_list.get(a).get(b).level = a;
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /*
     * function to return the right type of bird
     */
    private int RightBird(int bird)
    {
        switch (bird)
        {
            case 2:
                return (0);
            case 3:
                return (1);
            case 4:
                return (2);
            case 5:
                return (3);
            case 6:
                return (4);
            case 14:
                return (2);        // unknown sometimes means blue
            default:
                // System.out.println("Error in RightBird, called by getNodeType...");
                return (-1);
        }
    }

    /*
     * function to return the right type
     * to get the right regressor for the prediction
     */
    private int getNodeType(Node tmp, int bird)
    {
        int pointer;
        int point = 0;

        switch (tmp.obj.type.ordinal())
        {
            case 7: // pig
                pointer = RightBird(bird);
                break;
            case 9: // wood
                if (tmp.obj.shape.ordinal() == 2)
                {    // cycle wood
                    point = 25;
                    pointer = point + RightBird(bird);
                } else
                {                                // simple wood
                    point = 5;
                    pointer = point + RightBird(bird);
                }
                break;
            case 10: // stone
                if (tmp.obj.shape.ordinal() == 2)
                {    // cycle stone
                    point = 30;
                    pointer = point + RightBird(bird);
                } else
                {                                // simple stone
                    point = 10;
                    pointer = point + RightBird(bird);
                }
                break;
            case 8: // ice
                point = 15;
                pointer = point + RightBird(bird);
                break;
            case 11: // tnt
                point = 20;
                pointer = point + RightBird(bird);
                break;
            default: // something else that cannot be recognized
                pointer = -1;
                // System.out.println(tmp.type);
                // System.out.println("Error in recognition in getNodeType, default case....");
                break;
        }
        return (pointer);
    }

    // function to construct the Tree for each Shot
    public void TreeConstruction(Rectangle room, List<ABObject> PigsObjects, int x, int y,
                                 int rootWidth, List<ABObject> pigs, List<ABObject> TNT, Rectangle sling,
                                 ABType bird, double limit, List<ABObject> birds)
    {

        this.birds = birds;
        boolean PrintFlag = false;

        this.height = room.getHeight();
        this.width = room.getWidth();
        this.pigs = pigs.size();
        this.objects = PigsObjects.size() - this.pigs;

        // Total tree's constructions
        create_level_list(room, PigsObjects, x, y, rootWidth);

        // Sort level_list according to their x coordinate
        OrderLevelList();

        // merge Nodes if needed
        MergeNodesDifferentLevel();
        MergeNodesSameLevel();
        MergeNodesDifferentLevel();

        // detects stones on a pigs' head
        PigStone();

        // Sort again level_list according to their x coordinate, due to merge
        OrderLevelList();

        // function which constructs our Tree
        constructEdges(y);

        // order children for each Node of our Tree
        OrderChildren();

        // order parents for each Node of our Tree
        OrderParents();

        // Print Children and Parents only if needed
        if (PrintFlag)
        {
            PrintChildren();
            PrintParents();
        }

        // for each Node, find points that it can be hit
        // if bird that is on sling is not the White Bird
        if (!bird.equals(ABType.WhiteBird))
            findTargetPoints(sling);

    }

    public void SetFeatures(Rectangle ourRoomNew, List<ABObject> pigs, List<ABObject> TNT, Rectangle sling, ABType bird, double limit)
    {

        //Distance from nearest pig or TNTs
        setNPdistance(pigs, TNT, sling, bird, limit, ourRoomNew);

        // RelevantHeight in Tree
        SetRelevantHeight();

        // set Cumulative Parents Weight
        SetAboveWeight(sling);
    }

    /*
     * function to find targetPoints for each Node
     * store all elements needed in an ArrayList<TargetNode>
     * designed specifically for this purpose
     * used for non-WhiteBirds
     */
    private void findTargetPoints(Rectangle sling)
    {
        Point th, tw;
        ArrayList<Point> pts;

        for (int i = 0; i < LevelSize() - 1; i++)
        {
            for (int j = 0; j < LevelSize(i); j++)
            {
                Node tmp = GetElement(i, j);

                // Target Points are computed only if the Node is in the Game Scene
                if (tmp.sceneFeasible)
                {
                    if (tmp.type.equals("Pig") || tmp.type.equals("TNT"))
                    {
                        Point tpt = tmp.obj.getCenter();
                        pts = tp.estimateLaunchPoint(sling, tpt);
                        tmp.targetNode.add(new TargetNode(tpt, pts, "PT-center"));
                    } else
                    {
                        if ((Math.abs(tmp.obj.angle - 90) > 20) && (Math.abs(tmp.obj.angle) > 20))
                        {        // Experimental threshold 20
                            Point tpt = tmp.obj.getCenter();
                            pts = tp.estimateLaunchPoint(sling, tpt);

                            tmp.targetNode.add(new TargetNode(tpt, pts, "center"));
                        } else
                        {
                            // TargetPoints for left side
                            if (tmp.obj.height > 10)
                            {
                                th = new Point(tmp.obj.x, tmp.obj.y + tmp.obj.height / 4);
                                pts = tp.estimateLaunchPoint(sling, th);

                                tmp.targetNode.add(new TargetNode(th, pts, "left-3/4"));

                                th = new Point(tmp.obj.x, tmp.obj.y + tmp.obj.height / 2);
                                pts = tp.estimateLaunchPoint(sling, th);

                                tmp.targetNode.add(new TargetNode(th, pts, "left-1/2"));
                            } else
                            {
                                th = new Point(tmp.obj.x, tmp.obj.y + tmp.obj.height / 2);
                                pts = tp.estimateLaunchPoint(sling, th);

                                tmp.targetNode.add(new TargetNode(th, pts, "left-1/2"));
                            }

                            // targetPoint for up-side
                            tw = new Point(tmp.obj.x + tmp.obj.width / 2, tmp.obj.y);
                            pts = tp.estimateLaunchPoint(sling, tw);

                            tmp.targetNode.add(new TargetNode(tw, pts, "up"));
                        }
                    }
                }
            }
        }
    }

    public Node GetElement(int i, int j)
    {
        return level_list.get(i).get(j);
    }

    public int LevelSize()
    {
        return level_list.size();
    }

    public int LevelSize(int i)
    {
        return level_list.get(i).size();
    }

    public void SetPhi(Matrix Phi, int i, int j)
    {
        GetElement(i, j).PhiX = Phi;
    }

    public double GetMyWeight(int i, int j)
    {
        return GetElement(i, j).myWeight;
    }

    /*
     * for each Node set RelevantHeight feature
     */
    private void SetRelevantHeight()
    {
        int n = LevelSize();

        // level_list consists of only two levels
        // top level contains Node "Root"
        // there are only two levels
        if (n - 3 < 0)
        {
            // System.out.println("Tree Structure consists of 2 levels .... 1st: 'Root' 2nd: other Nodes ... ");
        } else
        {    // there are more than two levels
            /*
             * top-down propagation of heights
             */
            for (int i = n - 2; i >= 0; i--)
            {
                int m = LevelSize(i);
                for (int j = 0; j < m; j++)
                {
                    Node thisNode = GetElement(i, j);
                    double min = 800;

                    for (int par = 0; par < thisNode.parent.size(); par++)
                    {
                        Node parentNode = thisNode.parent.get(par);

                        if (thisNode.parent.get(0).type.equals("Root"))
                        {
                            min = thisNode.obj.getCenterY() + thisNode.obj.height / 2.00;
                        } else if (parentNode.topdown < min)
                        {
                            min = parentNode.topdown;
                        }
                    }
                    thisNode.topdown = min;
                }
            }

            // to distinguish Rarent-"Root" nodes
            for (int i = n - 2; i >= 0; i--)
            {
                for (int j = 0; j < LevelSize(i); j++)
                {
                    Node tmp = GetElement(i, j);

                    if (tmp.parent.get(0).type.equals("Root"))
                    {
                        tmp.topdown = 0;
                    }
                }
            }
        }

        /*
         * bottom-up propagation of heights
         */
        for (int i = 0; i < n - 1; i++)
        {        // Root out
            int m = LevelSize(i);

            for (int j = 0; j < m; j++)
            {
                Node thisNode = GetElement(i, j);
                double max = -800;

                if (thisNode.children.isEmpty())
                    max = thisNode.obj.getCenterY() + thisNode.obj.height / 2.00;
                else
                {
                    for (int child = 0; child < thisNode.children.size(); child++)
                    {
                        if (thisNode.children.get(child).bottomup > max)
                        {
                            max = thisNode.children.get(child).bottomup;
                        }
                    }
                }
                thisNode.bottomup = max;
            }
        }

        // to distinguish "no-Children" nodes
        for (int i = 0; i < LevelSize() - 1; i++)
        {
            for (int j = 0; j < LevelSize(i); j++)
            {
                Node tmp = GetElement(i, j);

                if (tmp.children.isEmpty())
                {
                    tmp.bottomup = tmp.obj.getCenterY() + tmp.obj.height / 2.00;
                }
            }
        }

        // final computation of Relevant Height
        for (int i = 0; i < LevelSize() - 1; i++)
        {        // "Root out of computations
            for (int j = 0; j < LevelSize(i); j++)
            {
                Node tmp = GetElement(i, j);

                if (tmp.topdown == 0)
                {
                    tmp.relevantHeight = 0;
                } else
                {
                    tmp.relevantHeight = (double) Math.abs(((tmp.obj.getCenterY() + tmp.obj.height / 2.00) - tmp.topdown)) / 200;
                    if (tmp.relevantHeight > 1.0)
                        tmp.relevantHeight = 1.0;
                }
            }
        }
    }

    public double GetRelevantHeight(int i, int j)
    {
        return GetElement(i, j).relevantHeight;
    }

    /*
     * function which computes the total weight of each node
     * totalWeight refers to the Nodes above current Node
     * Cumulative Parents' Weight
     */
    public void SetAboveWeight(Rectangle slingshot)
    {
        if (slingshot != null)
        {
            int n = LevelSize();
            for (int i = n - 2; i >= 0; i--)
            {
                int m = LevelSize(i);

                for (int j = 0; j < m; j++)
                {
                    Node thisLevelNode = GetElement(i, j);

                    thisLevelNode.totalWeight = 0;

                    for (int a = 0; a < thisLevelNode.parent.size(); a++)
                    {
                        thisLevelNode.totalWeight += (int) (thisLevelNode.parent.get(a).totalWeight + thisLevelNode.parent.get(a).myWeight);
                    }
                }
            }
        }
    }

    public double GetAboveWeight(int i, int j)
    {
        return GetElement(i, j).totalWeight;
    }

    /*
     * function to compute the distance from nearest Pig or TNT
     */
    private void setNPdistance(List<ABObject> pigs, List<ABObject> TNT, Rectangle sling, ABType bird, double limit, Rectangle room)
    {
        List<ABObject> newList = new ArrayList<ABObject>();
        newList.addAll(pigs);
        newList.addAll(TNT);

        for (int i = LevelSize() - 2; i >= 0; i--)
        {
            for (int j = 0; j < LevelSize(i); j++)
            {
                Node tmp = GetElement(i, j);
                if (tmp.feasible && tmp.sceneFeasible && tmp.reachable)
                {

                    boolean tmptype = ((!tmp.type.equals("Pig")) && (!tmp.type.equals("TNT")));            // Not Pig AND not Tnt

                    if (tmptype)
                    {
                        double min = Double.POSITIVE_INFINITY;
                        double dist = -1;
                        Point tmpCenter = tmp.obj.getCenter();

                        // find the nearest pig or TNT
                        for (int a = 0; a < newList.size(); a++)
                        {
                            dist = distance(newList.get(a).getCenter(), tmpCenter);
                            if (!tmp.obj.equals(newList.get(a)))
                            {
                                if (dist < min)
                                {
                                    min = dist;
                                }
                            }
                        }
                        tmp.NPdistance = min;
                    } else
                    {
                        // Euclidean distance between nearest pig
                        // // System.out.println(tmp.type + " Center_X: " + tmp.obj.getCenterX() + " Center_Y: " + tmp.obj.getCenterY());
                        double min_ = 1000;
                        double[][] NP = {{0, 0, 0, 0}, {0, 0, 0, 0}};
                        int loc = 0;

                        for (int a = 0; a < tmp.targetNode.get(0).getReleasePointList().size(); a++)
                        {
                            NP[a] = findTapTimeforPigTNT(tmp, sling, tmp.targetNode.get(0).getReleasePointList().get(a), room, tmp.targetNode.get(0).getTargetPoint(), bird, limit);
                            if (NP[a][1] < min_)
                            {
                                min_ = NP[a][1];
                                loc = a;
                            }
                        }
                        tmp.TapInterval = NP[loc][0];
                        tmp.NPdistance = min_;

                        // remove needless trajectory
                        if (tmp.targetNode.get(0).getReleasePointList().size() == 2)
                        {
                            //// System.out.println(tmp.type);
                            if (loc == 0)
                            {
                                tmp.targetNode.get(0).getReleasePointList().remove(1);
                            } else
                            {
                                tmp.targetNode.get(0).getReleasePointList().remove(0);
                            }
                        }
                    }

                    if (tmp.NPdistance > 100)
                    {
                        tmp.NPdistance = 100;
                    }

                    // normalization: 100 is considered the maximum "affecting" distance
                    tmp.NPdistance = tmp.NPdistance / 100.0;
                }
            }
        }
    }

    public double GetNPDistance(int i, int j)
    {
        return GetElement(i, j).NPdistance;
    }

    // function to return Euclidean distance between two Points
    private double distance(Point p1, Point p2)
    {
        return Math.sqrt((double) ((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y)));
    }

    /*
     * function to estimate tap time
     * if target is a "sheltered" Pig
     * if the bird is Blue or Yellow or Black
     *
     * - returns: double [] matrix
     * [0]: tap time percentage
     * [1]: most external Point distance
     * [2]: external Point's coordinate x
     * [3]: external Point's coordinate y
     */
    private double[] findTapTimeforPigTNT(Node obj, Rectangle sling, Point LaunchPoint, Rectangle room, Point targetPoint, ABType bird, double limit)
    {
        double[] result = {0, 0, 0, 0};

        List<Point> trajPoint = tp.predictTrajectory(sling, LaunchPoint);
        Point compPoint = findRealTargetforPigTNT(sling, obj, LaunchPoint, room, targetPoint, limit, bird);

        int total_points = 0;
        int partial_points = 0;
        if (compPoint != null)
        {
            boolean compPointFind = false;
            //// System.out.println("CompNode: " + comp.type + " level: " + comp.level);
            result[1] = distance(compPoint, targetPoint);

            for (int i = 0; i < trajPoint.size(); ++i)
            {
                if ((obj.obj.x <= trajPoint.get(i).x))
                {
                    total_points = i;
                }
                if ((compPoint.x <= trajPoint.get(i).x) && !compPointFind)
                {
                    partial_points = i;
                    compPointFind = true;
                }
            }
        }

        double percentage = 0;
        switch (bird)
        {
            case RedBird:
                percentage = 90;
                break;
            case YellowBird:
                percentage = 85;
                break;
            case BlueBird:
                percentage = 75;
                break;
            case BlackBird:
                percentage = 160;
                break;
            default:
                percentage = 80;
                break;
        }

        if (!bird.equals(ABType.BlackBird))
        {
            if (total_points != 0)
            {
                percentage -= (double) ((total_points - partial_points) / total_points) * 100;
            }
        }

        if (partial_points != 0)
        {
            result[0] = percentage;
            result[2] = compPoint.x;
            result[3] = compPoint.y;
        } else
        {
            result[0] = percentage;
            result[1] = 0;
        }

        return (result);
    }

    /*
     * find the most external Point for a "shletered" Pig or TNT
     */
    private Point findRealTargetforPigTNT(Rectangle sling, Node target, Point LaunchPoint, Rectangle room, Point targetPoint, double limit, ABType bird)
    {
        int pos = 0;

        List<Point> trajPoint = tp.predictTrajectory(sling, LaunchPoint);
        Point resP = null;

        // find the first Point that is in ourRoom
        for (int i = 0; i < trajPoint.size(); i++)
        {
            if (room.contains(trajPoint.get(i)))
            {
                pos = i;
                break;
            }
        }

        for (int a = pos; a < trajPoint.size(); a++)
        {

            // most right from interest Point
            if ((targetPoint.y <= trajPoint.get(a).y) && (targetPoint.x <= trajPoint.get(a).x))
                break;

            //// System.out.println(trajPoint.get(a));
            for (int i = 0; i < level_list.size() - 1; i++)
            {
                for (int j = 0; j < level_list.get(i).size(); j++)
                {
                    Node tmp = level_list.get(i).get(j);

                    Rectangle tmpbird;
                    tmpbird = new Rectangle(trajPoint.get(a).x, trajPoint.get(a).y, 5, 5);            // average bird's width and height
                    if (tmp.obj.intersects(tmpbird))
                    {
                        if (!tmp.equals(target))
                        {
                            if (!(tmp.type.equals("Pig") || tmp.type.equals("TNT")))
                            {
                                //// System.out.println("Point: " + trajPoint.get(a));
                                return (trajPoint.get(a));
                            }
                        } else
                        {
                            return (targetPoint);
                        }
                    }
                    tmpbird = null;
                }
            }
        }

        return (resP);
    }

    /*
     * function to free the constructed Tree
     */
    public void myfree()
    {
        for (int i = 0; i < LevelSize(); i++)
        {
            for (int j = 0; j < LevelSize(i); j++)
            {
                Node.freeNode(GetElement(i, j));
            }
        }

        for (int i = 0; i < LevelSize(); i++)
        {
            level_list.get(i).clear();
        }
        level_list.clear();
        System.gc();

    }

    /*
     * to pause whenever needed
     */
    private void mySleep(int time)
    {
        try
        {
            Thread.sleep(time);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }


    //Return a link list of ABObjects that support o1 (test by isSupport function ).
    //objs refers to a list of potential supporters.
    //Empty list will be returned if no such supporters.
    //Used for getNumOfBlocks
    private void getSupporters(Node o2, ArrayList<ArrayList<Node>> level_list, ArrayList<Node> supporters)
    {
        //Loop through the potential supporters
        for (int i = o2.level - 1; i >= 0; i--)
        {
            for (int j = 0; j < LevelSize(i); j++)
            {
                if (isSupport(o2.obj, GetElement(i, j).obj))
                {
                    if (!supporters.contains(GetElement(i, j)))
                    {
                        supporters.add(GetElement(i, j));
                        getSupporters(GetElement(i, j), level_list, supporters);
                    }
                }
            }
        }
    }

    /*
    Checks if 2 nodes have some same supporters (if any o2 supporter exists in o1supporters)
    Used for getNumOfBlocks
    returns 1 if there's an intersect, 0 otherwise.
    */
    private int compareSupporters(Node o2, ArrayList<ArrayList<Node>> level_list, ArrayList<Node> o1supporters)
    {
        //get o2 supporters
        ArrayList<Node> o2Supporters = new ArrayList<Node>();
        getSupporters(o2, level_list, o2Supporters);

        //check if any of them is already in o1supportes
        for (Node o2sup : o2Supporters)
        {
            if (o1supporters.contains(o2sup))
                return 1;
        }
        return 0;
    }

    /*
    Returns the leftmost node(object) on the map (by x coordinate).
    Used for getWidth()
     */
    private Node leftmost()
    {
        Node leftmost = GetElement(0, 0);
        for (int i = 1; i < LevelSize() - 1; i++)
        {
            if (GetElement(i, 0).obj.getX() < leftmost.obj.getX())
                leftmost = GetElement(i, 0);
        }
        return leftmost;
    }

    /*
    Returns the rightmost node(object) on the map (by x coordinate).
    Used for getWidth()
     */
    private Node rightmost()
    {
        int rightestIndex = LevelSize(0) - 1;
        Node rightmost = GetElement(0, rightestIndex);
        for (int i = 1; i < LevelSize() - 1; i++)
        {
            rightestIndex = LevelSize(i) - 1;
            if (GetElement(i, rightestIndex).obj.getX() > rightmost.obj.getX())
                rightmost = GetElement(i, rightestIndex);
        }
        return rightmost;
    }


    //Iterator for level_list, used for getters
    private long LevelListIterator(Predicate<Node> predicate)
    {
        return level_list.stream().flatMap(List::stream).filter(predicate).count();
    }

    //Iterator for birds list, used for getters
    private long BirdsListIterator(Predicate<ABObject> predicate)
    {
        return birds.stream().filter(predicate).count();
    }
}

