package ab.vision;

import ab.demo.Rec2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.jbox2d.common.Vec2;

public class HullUtils
{
    private ArrayList<Point> myHull;
    private Rec2D mySmallestSquare;
    
    public HullUtils(ArrayList<Point> points)
    {
        if (points.size() >= 3)
        {
            myHull = convexHull(points);

            mySmallestSquare = findSmallestSquare(myHull);
        }
    }
    
    public Rec2D getSmallestSquare()
    {
        return mySmallestSquare;
    }
    
    public int whatIsIt(float sceneScale) //returns  0:garbage 1:square 2:triangle (2+i: corner i)
    {
        if (myHull == null)
        {
            return 0; //garbage
        }
        //System.out.println();
        //System.out.println("Smallest square:" + mySmallestSquare.height + " " + mySmallestSquare.width + " " + mySmallestSquare.angle);
        
        //correct size of squares/triangles in the game is 22x22 pixels !
        //tests show that they're about 19x19 (possibly a bit smaller)
        //allow for some error ! check only height, since width is the same
        //if ((mySmallestSquare.height < 17.8) || (mySmallestSquare.height > 22))
        
        float sampleSceneScale = 66.0f; //scene scale where sample was taken
        
        if ((mySmallestSquare.height*sampleSceneScale/sceneScale < 17) || (mySmallestSquare.height*sampleSceneScale/sceneScale > 21))
        {
            return 0; //garbage
        }
        
        //test for triangle
        /*
        System.out.println(" 1  1: " + allInCorner(new Vec2( 1, 1), mySmallestSquare, myHull));
        System.out.println(" 1 -1: " + allInCorner(new Vec2( 1,-1), mySmallestSquare, myHull));
        System.out.println("-1  1: " + allInCorner(new Vec2(-1, 1), mySmallestSquare, myHull));
        System.out.println("-1 -1: " + allInCorner(new Vec2(-1,-1), mySmallestSquare, myHull));
        */
        
        boolean corner0 = allInCorner(new Vec2( 1, 1), mySmallestSquare, myHull); //top-right
        boolean corner1 = allInCorner(new Vec2(-1, 1), mySmallestSquare, myHull); //top-left
        boolean corner2 = allInCorner(new Vec2(-1,-1), mySmallestSquare, myHull); //bottom-left
        boolean corner3 = allInCorner(new Vec2( 1,-1), mySmallestSquare, myHull); //bottom-right
        
        int corner = 0;
        int count = 0;
        if (corner0 && !corner1 && !corner2 && !corner3)
        {
            corner = 2+0; //corner 0
            count++;
        }
        if (!corner0 && corner1 && !corner2 && !corner3)
        {
            corner = 2+1; //corner 1
            count++;
        }
        if (!corner0 && !corner1 && corner2 && !corner3)
        {
            corner = 2+2; //corner 2
            count++;
        }
        if (!corner0 && !corner1 && !corner2 && corner3)
        {
            corner = 2+3; //corner 3
            count++;
        }
        
        //System.out.println("Corner count: " + count);
        
        if (count == 0)
        {
            return 1; //square
        }
        
        if (count == 1)
        {
            return corner; //triangle
        }
        
        return 0; //you ain't nothin' !
    }
    
    private boolean allInCorner(Vec2 corner, Rec2D refFrame, ArrayList<Point> hull)
    {
        //System.out.println("Corner: " + corner);
        
        for (int i=0; i<hull.size(); ++i)
        {
            //transform coordinates to a 2x2 square
            float transX = hull.get(i).x - refFrame.x;
            float transY = hull.get(i).y - refFrame.y;
            float rtx = (float)(transX*Math.cos(-refFrame.angle) - transY*Math.sin(-refFrame.angle));
            float rty = (float)(transX*Math.sin(-refFrame.angle) + transY*Math.cos(-refFrame.angle));
            float stx = rtx*2.0f/refFrame.height;
            float sty = rty*2.0f/refFrame.height;
            
            //System.out.println("Hull " + i + "/"+hull.size()+": " + hull.get(i) + " => " + stx + "," + sty);
            
            float norm1 = Math.abs(corner.x-stx) + Math.abs(corner.y-sty);
            //System.out.println("norm1: " + norm1 + " -- refFrame.height: " + refFrame.height);
            
            if (norm1 > 2.0f*1.25f) // size 2 edge + 25% error
            {
                return false;
            }
        }
        
        /*
        float rtx = (float)(corner.x*Math.cos(refFrame.angle) - corner.y*Math.sin(refFrame.angle));
        float rty = (float)(corner.x*Math.sin(refFrame.angle) + corner.y*Math.cos(refFrame.angle));
        
        Vec2 realCorner = new Vec2((refFrame.x + rtx*refFrame.height/2.0f), (refFrame.y + rty*refFrame.height/2.0f));
        
        System.out.println("Real corner: " + realCorner);
        
        for (int i=0; i<hull.size(); ++i)
        {
            System.out.println("Hull " + i + ": " + hull.get(i));
            
            float norm1 = Math.abs(realCorner.x-hull.get(i).x) + Math.abs(realCorner.y-hull.get(i).y);
            System.out.println("norm1: " + norm1 + " -- refFrame.height: " + refFrame.height);
            
            if (norm1 > ((refFrame.height)*1.45f))
            {
                return false;
            }
            
        }
        */
        return true;
    }
    
    private float getSide(Vec2 ref, Vec2 sample) // >0 left , 0 online , <0 right
    {
        return (ref.x*sample.y - ref.y*sample.x);
    }
    
    private Rec2D findSmallestSquare(ArrayList<Point> hull)
    {
        Vec2 v = new Vec2((hull.get(0).x-hull.get(hull.size()-1).x), (hull.get(0).y-hull.get(hull.size()-1).y));
        v.normalize();
        float minAngle = (float)Math.atan2(v.y, v.x);
        //System.out.println("Fitting rectangles... ");
        Rec2D minRectangle = fitRectangle(hull.get(hull.size()-1), minAngle, hull);
        float minArea = minRectangle.height * minRectangle.width;

        for (int i=0; i<hull.size()-1; ++i)
        {
                //System.out.println("Testing rectangle "+i+"/"+(hull.size()-2));
                v = new Vec2(hull.get(i+1).x-hull.get(i).x, hull.get(i+1).y-hull.get(i).y);
                v.normalize();
                float angle = (float)Math.atan2(v.y, v.x);

                Rec2D rec = fitRectangle(hull.get(i), angle, hull);
                if (rec.height*rec.width < minArea)
                {
                        minAngle = angle;
                        minRectangle = rec;
                        minArea = rec.height*rec.width;
                }
        }
        
        Rec2D result = new Rec2D(minRectangle);
        result.angle = minAngle;
        
        return result;
    }
    
    private Rec2D fitRectangle(Point a, float theta, ArrayList<Point> rest)
    {
        float minX = java.lang.Float.POSITIVE_INFINITY;
        float maxX = java.lang.Float.NEGATIVE_INFINITY;
        float minY = java.lang.Float.POSITIVE_INFINITY;
        float maxY = java.lang.Float.NEGATIVE_INFINITY;
        for (int i=0; i<rest.size(); ++i)
        {
            //translate
            float tx = rest.get(i).x-a.x;
            float ty = rest.get(i).y-a.y;
            //rotate
            float rtx = (float)(tx*Math.cos(-theta) - ty*Math.sin(-theta));
            float rty = (float)(tx*Math.sin(-theta) + ty*Math.cos(-theta));
            //find min and max for each axis
            if (rtx<minX) minX=rtx;
            if (rtx>maxX) maxX=rtx;
            if (rty<minY) minY=rty;
            if (rty>maxY) maxY=rty;
        }
        //it's a square, so equalize x and y
        float edgeSize = Math.max((maxX-minX), (maxY-minY));
        float test = Math.min((maxX-minX), (maxY-minY));
        
        if ((edgeSize-test) > 4) //pixel error from being a square
        {
            edgeSize = java.lang.Float.POSITIVE_INFINITY; //make impossible
        }
        //System.out.println("Edge difference: " + (edgeSize-test));
        
        Rec2D result = new Rec2D(edgeSize, edgeSize);
        
        //rotate square center back
        float sqcX = (maxX+minX)/2.0f;
        float sqcY = (maxY+minY)/2.0f;
        float rsqcX = (float)(sqcX*Math.cos(theta) - sqcY*Math.sin(theta));
        float rsqcY = (float)(sqcX*Math.sin(theta) + sqcY*Math.cos(theta));
        result.x = a.x+rsqcX;
        result.y = a.y+rsqcY;
        
        return result;
    }

    private ArrayList<Point> convexHull(ArrayList<Point> points) 
    {
        ArrayList<Point> xSorted = (ArrayList<Point>) points.clone();
        Collections.sort(xSorted, new XCompare());

        int n = xSorted.size();

        Point[] lUpper = new Point[n];

        lUpper[0] = xSorted.get(0);
        lUpper[1] = xSorted.get(1);

        int lUpperSize = 2;

        for (int i = 2; i < n; i++)
        {
            lUpper[lUpperSize] = xSorted.get(i);
            lUpperSize++;

            while (lUpperSize > 2 && !rightTurn(lUpper[lUpperSize - 3], lUpper[lUpperSize - 2], lUpper[lUpperSize - 1]))
            {
                // Remove the middle point of the three last
                lUpper[lUpperSize - 2] = lUpper[lUpperSize - 1];
                lUpperSize--;
            }
        }

        Point[] lLower = new Point[n];

        lLower[0] = xSorted.get(n - 1);
        lLower[1] = xSorted.get(n - 2);

        int lLowerSize = 2;

        for (int i = n - 3; i >= 0; i--)
        {
            lLower[lLowerSize] = xSorted.get(i);
            lLowerSize++;

            while (lLowerSize > 2 && !rightTurn(lLower[lLowerSize - 3], lLower[lLowerSize - 2], lLower[lLowerSize - 1]))
            {
                // Remove the middle point of the three last
                lLower[lLowerSize - 2] = lLower[lLowerSize - 1];
                lLowerSize--;
            }
        }

        ArrayList<Point> result = new ArrayList<Point>();

        for (int i = 0; i < lUpperSize; i++)
        {
            result.add(lUpper[i]);
        }

        for (int i = 1; i < lLowerSize - 1; i++)
        {
            result.add(lLower[i]);
        }

        return result;
    }

    private boolean rightTurn(Point a, Point b, Point c)
    {
        return (b.x - a.x)*(c.y - a.y) - (b.y - a.y)*(c.x - a.x) > 0;
    }

    private class XCompare implements Comparator<Point>
    {
        @Override
        public int compare(Point o1, Point o2) 
        {
                return (new Integer(o1.x)).compareTo(new Integer(o2.x));
        }
    }
}

