/*****************************************************************************
 ** IHSEV AIBirds Agent 2014
 ** Copyright (c) 2015, Mihai Polceanu, CERV Brest France
 ** Contact: polceanu@enib.fr
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/

//Modified version of original ClientNaiveAgent.java file (see copyright notice below)

/**
 * ***************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK * Copyright (c) 2014, XiaoYu (Gary) Ge,
 * Stephen Gould, Jochen Renz * Sahan Abeyasinghe,Jim Keys, Andrew Wang, Peng
 * Zhang * All rights reserved. *This work is licensed under the Creative
 * Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. *To view a
 * copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/
 * or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain
 * View, California, 94041, USA.
 * ***************************************************************************
 */

package ab.demo;

import ab.demo.other.ClientActionRobot;
import ab.demo.other.ClientActionRobotJava;
import ab.planner.TrajectoryPlanner;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;
import ab.vision.real.shape.Poly;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import org.jbox2d.common.Vec2;
//Naive agent (server/client version)

public class ClientNaiveAgent implements Runnable
{
    //Wrapper of the communicating messages
    private ClientActionRobotJava ar;
    public byte currentLevel = 0;
    //public int failedCounter = 0;
    //public int[] solved;
    TrajectoryPlanner tp;
    private int id = 27939; //IHSEV
    private boolean firstShot;
    private Point prevTarget;
    private Random randomGenerator;
    
    //----------------------------------------//
    //focus point
    private int focus_x;
    private int focus_y;
    
    private int numberOfLevels = 21;
    
    private int[] triedLevels = new int[21];
    private int[] failedLevels = new int[21];
    private int[] failedMinPigs = new int[21];
    private int pigsLeft = 0;
	
    private int[] bestScores = new int[21];
    private int[] myScores = new int[21];
    
    private int shouldRestart = 0;
    
    private boolean debugStop = false;

    private int failedLevelsThresh = 2;
	
    private int desperateModeThresh = 3;
    //----------------------------------------//

    /**
     * Constructor using the default IP
     *
     */
    public ClientNaiveAgent()
    {
        // the default ip is the localhost
        ar = new ClientActionRobotJava("127.0.0.1");
        tp = new TrajectoryPlanner();
        randomGenerator = new Random();
        prevTarget = null;
        firstShot = true;

    }

    /**
     * Constructor with a specified IP
     *
     */
    public ClientNaiveAgent(String ip)
    {
        ar = new ClientActionRobotJava(ip);
        tp = new TrajectoryPlanner();
        randomGenerator = new Random();
        prevTarget = null;
        firstShot = true;

    }

    public ClientNaiveAgent(String ip, int id)
    {
        ar = new ClientActionRobotJava(ip);
        tp = new TrajectoryPlanner();
        randomGenerator = new Random();
        prevTarget = null;
        firstShot = true;
        this.id = id;
    }

    private byte getNextDesirableLevel()
    {
        /*
        for (int i=0; i<numberOfLevels; ++i)
        {
            if ((i+1) == currentLevel) continue; //skip the current level

            if ((myScores[i] == 0) && (failedLevels[i] == 0))
            {
                    return (byte)(i+1); //this level, but numerated from 1
            }
        }
        */
        for (int i=0; i<numberOfLevels; ++i)
        {
            if (triedLevels[i] == 0)
            {
                return (byte)(i+1); //get next untried level !
            }
        }

        Random rand = new Random(); //magique !

        //if all levels have been tried, choose a random failed one
        ArrayList<Integer> unsolvedLevels = new ArrayList<Integer>();
        for (int i=0; i<numberOfLevels; ++i)
        {
            //if ((myScores[i] == 0) && (failedLevels[i] < (desperateModeThresh+3)))
            if (myScores[i] == 0)
            {
                unsolvedLevels.add(i);
                if (failedMinPigs[i] == 1)
                {
                    //triple chance if one pig left
                    unsolvedLevels.add(i);
                    unsolvedLevels.add(i);
                }
                else if (failedMinPigs[i] == 2)
                {
                    //double chance if two pig left
                    unsolvedLevels.add(i);
                }
            }
        }

        if (unsolvedLevels.size() > 0)
        {
            return (byte)(unsolvedLevels.get(rand.nextInt(unsolvedLevels.size()))+1);
        }

        //if all levels have been solved, try to improve score based on best results
        ArrayList<Integer> lowScoreLevels = new ArrayList<Integer>();
        for (int i=0; i<numberOfLevels; ++i)
        {
            if (myScores[i] < bestScores[i]) lowScoreLevels.add(i);
        }

        if (lowScoreLevels.size() > 0)
        {
            return (byte)(lowScoreLevels.get(rand.nextInt(lowScoreLevels.size()))+1);
        }

        //if in this happy situation, pick a random level
        return (byte)(rand.nextInt(numberOfLevels)+1);
    }

    /* 
     * Run the Client (Naive Agent)
     */
    public void run()
    {
        byte[] configResponse = ar.configure(ClientActionRobot.intToByteArray(id));

        numberOfLevels = (int)configResponse[2];

        System.out.println("Nr of levels: " +numberOfLevels);

        //load best scores
        bestScores = ar.checkScore();

        //load my scores
        myScores = ar.checkMyScore();

        //load the initial level (default 1)
        currentLevel = getNextDesirableLevel();
        System.out.println(" loading the level " + (currentLevel) );
        ar.loadLevel(currentLevel);
        triedLevels[currentLevel-1] = 1;
        
        GameState state;
        while (true)
        {
            state = solve();

            //If the level is solved , go to the next level
            if (state == GameState.WON)
            {
                /*
                System.out.println("Whohoo ! Waiting 4 seconds to record the score...");
                try
                {
                    Thread.sleep(4000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                */
                
                System.out.println("Whohoo !");
                
                int wonLevel = currentLevel;

                currentLevel = getNextDesirableLevel();
                System.out.println(" loading the level " + (currentLevel) );
                ar.loadLevel(currentLevel);
                triedLevels[currentLevel-1] = 1;
                //ar.loadLevel(++currentLevel);

                //load best scores
                bestScores = ar.checkScore();

                //load my scores
                myScores = ar.checkMyScore();

                //display the global best scores
                System.out.println("The global best score: ");
                for (int i = 0; i < bestScores.length; ++i)
                {
                    System.out.print( " level " + (i+1) + ": " + bestScores[i]);
                }
                System.out.println();

                System.out.println(" My score: ");
                for (int i = 0; i < myScores.length; ++i)
                {
                    System.out.print( " level " + (i+1) + ": " + myScores[i]);
                }
                System.out.println();

                if (myScores[wonLevel-1] == 0)
                {
                    myScores[wonLevel-1] = 1; //to know it's solved...
                }
				
                // make a new trajectory planner whenever a new level is entered
                tp = new TrajectoryPlanner();

                // first shot on this level, try high shot first
                firstShot = true;
            }
            else if (state == GameState.LOST) //If lost, then restart the level
            {
                System.out.println("restart");
                failedLevels[currentLevel-1]++;
                if ((pigsLeft>0) && (pigsLeft < failedMinPigs[currentLevel-1]))
                {
                    failedMinPigs[currentLevel-1] = pigsLeft;
                }
				
                if (failedLevels[currentLevel-1] >= failedLevelsThresh)
                {
                    byte lastLevel = currentLevel;
                    currentLevel = getNextDesirableLevel();
                    System.out.println(" loading the level " + (currentLevel) );
                    if (currentLevel == lastLevel)
                    {
                        ar.restartLevel();
                    }
                    else
                    {
                        ar.loadLevel(currentLevel);
                        triedLevels[currentLevel-1] = 1;
                    }
                }
                else
                {
                    ar.restartLevel();
                }
            }
            else if (state == GameState.LEVEL_SELECTION)
            {
                System.out.println("unexpected level selection page, go to the last current level : " + currentLevel);
                ar.loadLevel(currentLevel);
            }
            else if (state == GameState.MAIN_MENU)
            {
                System.out.println("unexpected main menu page, reload the level : " + currentLevel);
                ar.loadLevel(currentLevel);
            }
            else if (state == GameState.EPISODE_MENU)
            {
                System.out.println("unexpected episode menu page, reload the level: " + currentLevel);
                ar.loadLevel(currentLevel);
            }
        }
    }

    /**
     * Solve a particular level by shooting birds directly to pigs
     *
     * @return GameState: the game state after shots.
     */
    public GameState solve()
    {
        // capture Image
        BufferedImage screenshot = ar.doScreenShot();

        // process image
        Vision vision = new Vision(screenshot);

        Rectangle sling = vision.getRealVision().findSling();

        //If the level is loaded (in PLAYING　state)but no slingshot detected, then the agent will request to fully zoom out.
        while (sling == null && ar.checkState() == GameState.PLAYING)
        {
            System.out.println("CNA: no slingshot detected. Please remove pop up or zoom out");

            ar.fullyZoomOut();
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            
            screenshot = ar.doScreenShot();
            vision = new Vision(screenshot);
            sling = vision.getRealVision().findSling();
        }
        
        BufferedImage testScreenshot = ar.doScreenShot();
        Vision testVision = new Vision(testScreenshot);
        if ((sling != null) && (testVision != null) && (!closeEnough(sling, testVision.getRealVision().findSling())))
        {
            System.out.println("Scene moving...");
            return ar.checkState();
        }
        
        // --------------------------------------------------------------------------------------------------------//
        // PERCEPTION
        
        // get all the pigs
        List<ABObject> real_pigs = vision.findPigsRealShape();
        List<Rectangle> pigs = new ArrayList<>();
        for (int i=0; i<real_pigs.size(); ++i) pigs.add(real_pigs.get(i));
        
        String shootingBirdSpecies = vision.detectShootingBirdSpecies();
        
        List<Rectangle> red_birds = new ArrayList<>();
        List<Rectangle> blue_birds = new ArrayList<>();
        List<Rectangle> yellow_birds = new ArrayList<>();
        List<Rectangle> white_birds = new ArrayList<>();
        List<Rectangle> black_birds = new ArrayList<>();

        if ("RED_BIRD".equals(shootingBirdSpecies)) red_birds.add(new Rectangle());
        else if ("BLUE_BIRD".equals(shootingBirdSpecies)) blue_birds.add(new Rectangle());
        else if ("YELLOW_BIRD".equals(shootingBirdSpecies)) yellow_birds.add(new Rectangle());
        else if ("WHITE_BIRD".equals(shootingBirdSpecies)) white_birds.add(new Rectangle());
        else if ("BLACK_BIRD".equals(shootingBirdSpecies)) black_birds.add(new Rectangle());

        System.out.println(">>>>> "+shootingBirdSpecies);
        
        List<ABObject> sceneObjects = vision.findBlocksRealShape();
        
        List<Rectangle> stones = new ArrayList<>();
        List<Rectangle> ice = new ArrayList<>();
        List<Rectangle> wood = new ArrayList<>();
        List<Rectangle> tnts = new ArrayList<>();
        
        for (int i=0; i<sceneObjects.size(); ++i)
        {
            if (ABType.Stone.equals(sceneObjects.get(i).getType()))
            {
                stones.add(sceneObjects.get(i));
            }
            else if (ABType.Ice.equals(sceneObjects.get(i).getType()))
            {
                ice.add(sceneObjects.get(i));
            }
            else if (ABType.Wood.equals(sceneObjects.get(i).getType()))
            {
                wood.add(sceneObjects.get(i));
            }
            else if (ABType.TNT.equals(sceneObjects.get(i).getType()))
            {
                tnts.add(sceneObjects.get(i));
            }
            else
            {
                System.out.println("Unknown object type: " + sceneObjects.get(i).getType());
            }
        }
        
        List<ABObject> real_tnts = vision.findTNTs();
        for (int i=0; i<real_tnts.size(); ++i) tnts.add(real_tnts.get(i));
        
        
        /*
        List<Rectangle> rawStones = vision.findStones();
        List<Rectangle> stones = new ArrayList<Rectangle>(); for (int i=0; i<rawStones.size(); ++i) { stones.add(new abObjectBlueprint(rawStones.get(i), screenshot, abObjectBlueprint.Material.STONE)); }
        List<Rectangle> rawIce = vision.findIce();
        List<Rectangle> ice = new ArrayList<Rectangle>(); for (int i=0; i<rawIce.size(); ++i) { ice.add(new abObjectBlueprint(rawIce.get(i), screenshot, abObjectBlueprint.Material.ICE)); }
        List<Rectangle> rawWood = vision.findWood();
        List<Rectangle> wood = new ArrayList<Rectangle>(); for (int i=0; i<rawWood.size(); ++i) { wood.add(new abObjectBlueprint(rawWood.get(i), screenshot, abObjectBlueprint.Material.WOOD)); }
        List<Rectangle> tnts = vision.findTNTs();
        */
        
        float ground = vision.findGround(screenshot);

        List<List<Vec2> > groundPolygons = vision.findSupportGround2(screenshot, sling);
        /*
        List<List<Vec2> > groundPolygons = new ArrayList<>();
        List<ABObject> real_groundPolys = vision.findHills();
        for (int i=0; i<real_groundPolys.size(); ++i)
        {
            List<Vec2> nupoly = new ArrayList<>();
            java.awt.Polygon poly = ((Poly)real_groundPolys.get(i)).polygon;
            for (int j=0; j<poly.npoints; ++j)
            {
                nupoly.add(new Vec2(poly.xpoints[j], poly.ypoints[j]));
            }
            groundPolygons.add(nupoly);
        }
        */

        List<Rectangle> supports = vision.findSupport(screenshot);
        
        
        Hashtable<String, List<Rectangle> > objects = new Hashtable<>();
        objects.put("pigs", pigs);
        objects.put("red_birds", red_birds);
        objects.put("blue_birds", blue_birds);
        objects.put("yellow_birds", yellow_birds);
        objects.put("white_birds", white_birds);
        objects.put("black_birds", black_birds);
        objects.put("stones", stones);
        objects.put("ice", ice);
        objects.put("wood", wood);
        objects.put("tnts", tnts);
        
        // --------------------------------------------------------------------------------------------------------//
        
        // --------------------------------------------------------------------------------------------------------//
        // LOGIC
        
        int bird_count = 0;
        bird_count = ("UNKNOWN_BIRD".equals(shootingBirdSpecies))?0:1;

        System.out.println("...found " + pigs.size() + " pigs and (at least) " + bird_count + " bird");
        GameState state = ar.checkState();

        if ((sling != null) && (pigs.size() > 0) && ("UNKNOWN_BIRD".equals(shootingBirdSpecies)))
        {
            shouldRestart++;
        }
        else
        {
            shouldRestart = 0;
        }

        if (shouldRestart > 7)
        {
            System.out.println("Should restart: "+shouldRestart);
            failedLevels[currentLevel-1]++;

            if (failedLevels[currentLevel-1] >= failedLevelsThresh)
            {
                byte lastLevel = currentLevel;
                currentLevel = getNextDesirableLevel();
                System.out.println(" loading the level " + (currentLevel) );
                if (currentLevel == lastLevel)
                {
                        ar.restartLevel();
                }
                else
                {
                        ar.loadLevel(currentLevel);
                        triedLevels[currentLevel-1] = 1;
                }
            }
            else
            {
                ar.restartLevel();
            }

            shouldRestart = 0;
            firstShot=true;
        }

        if (debugStop) return state;

        // if there is a sling, then play, otherwise skip.
        if (sling != null && (bird_count > 0))
        {
            //ar.fullyZoomOut();

            //If there are pigs, we pick up a pig randomly and shoot it. 
            if (!pigs.isEmpty())
            {
                pigsLeft = pigs.size();
                
                Point releasePoint = new Point();
                Point refPoint = tp.getReferencePoint(sling);

                int tap_time = 100;
                   
                 // ########################################################################################################## //
                if (failedLevels[currentLevel-1] >= desperateModeThresh)
                {
                    // random pick up a pig
                    Random r = new Random();

                    int index = r.nextInt(pigs.size());
                    ABObject pig = (ABObject)pigs.get(index);
                    Point _tpt = pig.getCenter();

                    //System.out.println("the target point is " + _tpt);

                    // if the target is very close to before, randomly choose a point near it
                    if (prevTarget != null && distance(prevTarget, _tpt) < 10)
                    {
                        double _angle = r.nextDouble() * Math.PI * 2;
                        _tpt.x = _tpt.x + (int) (Math.cos(_angle) * 10);
                        _tpt.y = _tpt.y + (int) (Math.sin(_angle) * 10);
                        //System.out.println("Randomly changing to " + _tpt);
                    }

                    prevTarget = new Point(_tpt.x, _tpt.y);

                    // estimate the trajectory
                    ArrayList<Point> pts = tp.estimateLaunchPoint(sling, _tpt);

                    // do a high shot when entering a level to find an accurate velocity
                    /*if (firstShot && pts.size() > 1)
                    {
                        releasePoint = pts.get(1);
                    }
                    else
                    */
                    if (pts.size() == 1)
                    {
                        releasePoint = pts.get(0);
                    }
                    else if (pts.size() > 1)
                    {
                        // System.out.println("first shot " + firstShot);
                        // randomly choose between the trajectories, with a 1 in
                        // 6 chance of choosing the high one
                        if (r.nextInt(6) == 0)
                            releasePoint = pts.get(1);
                        else
                            releasePoint = pts.get(0);
                    }
                    
                    if (releasePoint != null)
                    {
                        double releaseAngle = tp.getReleaseAngle(sling, releasePoint);
                        //System.out.println(" The release angle is : "+ Math.toDegrees(releaseAngle));
                        int base = 0;
                        //tap later when the angle is more than PI/4
                        if (releaseAngle > Math.PI / 4)
                        {
                                base = 1400;
                        }
                        else
                        {
                                base = 550;
                        }
                        tap_time = (int) (base + Math.random() * 1500);
                        if ("WHITE_BIRD".equals(shootingBirdSpecies) || "BLACK_BIRD".equals(shootingBirdSpecies))
                        {
                            tap_time = 4000;
                        }
                        //System.out.println("tap_time " + tap_time);
                    }
                    else
                    {
                        System.err.println("Out of Knowledge");
                    }
                }
                else // ########################################################################################################## //
                {
                    //ArrayList<Shot> shots = new ArrayList<Shot>();
                    ArrayList<abSimulation> sims = new ArrayList<abSimulation>();
                    
                    boolean pigsDiedWhileSimulating = false;
                    boolean scaleChanged = false;
                    
                    abSimulation abs = null;
                    //for (double iAngle=0.13f; iAngle<=1.197; iAngle+=0.01)
                    for (double iAngle=0.0f; iAngle<=1.197; iAngle+=0.01)
                    {
                        //simulate

                        Point tmpReleasePoint = tp.findReleasePoint(sling, iAngle);

                        tp.setTrajectory(sling, tmpReleasePoint);

                        float sceneScale = (float)tp.getSceneScale(sling);
                        //System.out.println("SceneScale: " + sceneScale);

                        double actualAngle = tp.launchToActual(iAngle);
                        double velocity = tp.getVelocity(actualAngle);
                        double timeUnit = tp.getTimeUnit();
                        Vec2 shootDir = new Vec2((float)(velocity * Math.cos(actualAngle) * 1000.0 / timeUnit), (float)(velocity * Math.sin(actualAngle) * 1000.0 / timeUnit)); //y inverted

                        //System.out.println("Shooting dir vector: "+shootDir.x+" "+shootDir.y);
                        float gravity = (float)tp.getGravity(sling);

                        double releaseAngle = tp.getReleaseAngle(sling, tmpReleasePoint);
                        int base = 0;
                        if(releaseAngle > Math.PI/4)
                        {
                                base = 1400;
                        }
                        else
                        {
                                base = 550;
                        }
                        long tapTime = (long)(base + Math.random() * 2000);

                        if (abs == null)
                        {
                                abs = new abSimulation(sceneScale, new Vec2(refPoint.x, refPoint.y));
                                abs.setObjects(objects);
                                abs.setGroundLevel(ground);
                                abs.setSupportGround(groundPolygons);
                                abs.setSupportPlatforms(supports);
                                abs.setShootingVector(shootDir);
                                abs.setGravity(gravity);
                                abs.setTrajectory(tp.getTrajectory());
                                abs.setReleasePoint(tmpReleasePoint);
                                abs.setTapTime(tapTime);
                                abs.setSlingSize(sling.height, sling.width);
                                abSimUtils.getInstance().addSimulation(new SimulationWrapper(abs));
                                //abSimUtils.getInstance().addSimulation(new SimulationWrapper(abs, true)); //debug

                                sims.add(abs);
                        }
                        else
                        {
                                abSimulation absCopy = abs.copy();
                                absCopy.setShootingVector(shootDir);
                                absCopy.setGravity(gravity);
                                //abs.setTrajectory(tp.getTrajectory());
                                absCopy.setReleasePoint(tmpReleasePoint);
                                abs.setTapTime(tapTime);
                                abSimUtils.getInstance().addSimulation(new SimulationWrapper(absCopy));

                                sims.add(absCopy);
                        }
                    }
                    
                    try
                    {
                        System.out.print("Simulating");
                        while (!abSimUtils.getInstance().allFinished())
                        {
                                //Thread.sleep(100);
                                System.out.print(".");
                                
                                BufferedImage pigScreenshot = ar.doScreenShot();
                                Vision pigVision = new Vision(pigScreenshot);
                                List<ABObject> piggies = pigVision.findPigsRealShape();
                                if (piggies.size() < pigs.size())
                                {
                                    pigsDiedWhileSimulating = true;
                                    System.out.println();
                                    System.out.println("Pig count decreased. Re-thinking !");
                                    abSimUtils.getInstance().abort();
                                    break;
                                }
                                
                                Rectangle _sling = pigVision.getRealVision().findSling();
                                
                                //if (bestResult == null) System.out.println("No result from simulations ! so I don't know what to do !!");
                                
                                //System.out.println("sling: " + sling + " -- _sling: " + _sling);
                                
                                if (!closeEnough(sling,_sling))
                                {
                                    scaleChanged = true;
                                    abSimUtils.getInstance().abort();
                                    System.out.println("scale is changed, can not execute the shot, will re-segement the image");
                                    break;
                                }
                        }
                        System.out.println();
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                    
                    if (pigsDiedWhileSimulating || scaleChanged)
                    {
                        abSimUtils.getInstance().clear();
                        return state;
                    }

                    abSimulation bestResult = abSimUtils.getInstance().getBestResult();
                    if (bestResult != null)
                    {
                        releasePoint = bestResult.getReleasePoint();
                        tap_time = bestResult.getTapTime();
                    }
                    abSimUtils.getInstance().clear();

                    //debug
                    /*
                    abSimulation debugSim = bestResult.copy();
                    //debugSim.setDebugTrajectory(true);
                    SimulationWrapper sw = new SimulationWrapper(debugSim, true);
                    for (int i=0; i<sims.size(); ++i)
                    {
                            sw.addSimulation(sims.get(i));
                    }
                    debugStop = true;
                    */
                }
                 // ########################################################################################################## //

                //check image if no pigs
                boolean thinkAgain = false;
                BufferedImage newScreenshot = ar.doScreenShot();
                Vision newVision = new Vision(newScreenshot);
                List<ABObject> newPigs = newVision.findPigsMBR();

                if (newPigs.size() == 0)
                {
                    thinkAgain = true;
                }


                focus_x = (int) (refPoint.x);
                focus_y = (int) (refPoint.y);

                
                {
                    if (!thinkAgain)
                    {
                        // make the shot
                        //ar.shoot(focus_x, focus_y, (int) releasePoint.getX() - focus_x, (int) releasePoint.getY() - focus_y, 0, tap_time, false);
                        
                        ar.shoot(focus_x, focus_y, (int) releasePoint.getX() - focus_x, (int)releasePoint.getY() - focus_y,0, tap_time, false);
                        
                        // check the state after the shot
                        state = ar.checkState();

                        // update parameters after a shot is made
                        if (state == GameState.PLAYING)
                        {
                            screenshot = ar.doScreenShot();
                            vision = new Vision(screenshot);
                            List<Point> traj = vision.findTrajPoints();
                            tp.adjustTrajectory(traj, sling, releasePoint);
                            firstShot = false;
                        }
                    }
                }
            }
        }
        
        return state;
    }
    
    private boolean closeEnough(Rectangle a, Rectangle b)
    {
        if ((a == null) || (b == null))
        {
            return false;
        }
        return ((Math.abs(a.height-b.height) <= 1) && (Math.abs(a.width-b.width) <= 1) && (Math.abs(a.x-b.x) <= 1) && (Math.abs(a.y-b.y) <= 1));
    }

    private double distance(Point p1, Point p2)
    {
        return Math.sqrt((double) ((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y)));
    }

    public static void main(String args[])
    {

        ClientNaiveAgent na;
        if (args.length > 0)
        {
            na = new ClientNaiveAgent(args[0]);
        } else
        {
            na = new ClientNaiveAgent();
        }
        na.run();

    }
}
