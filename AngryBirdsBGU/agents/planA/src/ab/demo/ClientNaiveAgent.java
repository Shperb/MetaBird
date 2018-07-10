/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
 **This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
 **To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
 *or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 *****************************************************************************/
package ab.demo;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
//import java.util.Random;

import ab.demo.other.ClientActionRobot;
import ab.demo.other.ClientActionRobotJava;
import ab.planner.TrajectoryPlanner;
import ab.vision.ABObject;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;
//Naive agent (server/client version)

/***************************************/
// SHS : add import
import ab.vision.ABType;
import ab.vision.VisionMBR;

//import ab.utils.*;
import java.util.Collections;
import java.util.Comparator;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/***************************************/

public class ClientNaiveAgent implements Runnable {

	// Wrapper of the communicating messages
	private ClientActionRobotJava ar;
	public byte currentLevel = -1;
	public int failedCounter = 0;
	public int[] solved;
	TrajectoryPlanner tp;
	private int id = 28888; // Specified Client ID
	// private boolean firstShot;
	// private Point prevTarget;
	// private Random randomGenerator;

	/* ====== custom variables ====== */
	private static int STAGE_COUNT = 1;
	private static boolean[] levelStatus; // true = cleared, false = uncleared
	private static boolean stageLoop = false;

	private static int undone_cnt = 0;
	// private static int undone_index = 0;

	// private static int bird_count;
	// private static int pig_count;
	// private static int obstacle_count;

	private Point _tpt = new Point();
	// private int[] All_score = new int[21];
	// private Point tap_angle = new Point();
	private static int select = 0;
	

	/* ====== end of custom variables ====== */

	/**
	 * Constructor using the default IP
	 * */
	public ClientNaiveAgent() {
		// int tmp;

		// the default IP is the local host
		
		ar = new ClientActionRobotJava("127.0.0.1");
		tp = new TrajectoryPlanner();
		// prevTarget = null;
		// firstShot = true;
		// randomGenerator = new Random(); // In my original source, this
		// variable wasn't exist (Maybe this added on Naive 1.3?)

	}

	/**
	 * Constructor with a specified IP
	 * */
	public ClientNaiveAgent(String ip) {
		ar = new ClientActionRobotJava(ip);
		tp = new TrajectoryPlanner();
		// randomGenerator = new Random();
		// prevTarget = null;
		// firstShot = true;

	}

	/**
	 * Constructor with a specified IP, ID
	 * */
	public ClientNaiveAgent(String ip, int id) {
		ar = new ClientActionRobotJava(ip);
		tp = new TrajectoryPlanner();
		// randomGenerator = new Random();
		// prevTarget = null;
		// firstShot = true;
		this.id = id;
	}

	public int getNextLevel() {
		int level = 0;
		boolean unsolved = false;
		// all the levels have been solved, then get the first unsolved level
		for (int i = 0; i < solved.length; i++) {
			if (solved[i] == 0) {
				unsolved = true;
				level = (byte) (i + 1);
				if (level <= currentLevel && currentLevel < solved.length)
					continue;
				else
					return level;
			}
		}

		if (unsolved) {
			if (select == 0)
				select = 1;
			else
				select = 0;

			return level;
		}
		level = (byte) ((this.currentLevel + 1) % solved.length);
		if (level == 0) {
			level = solved.length;

			if (select == 0)
				select = 1;
			else
				select = 0;
		}
		return level;
	}

	/**
	 * Run the Client (Naive Agent)
	 */
	public void run() {
		// get Game Information from Server
		// info[0] = Round Info, 1 : 1st qualification round / 2 : 2st
		// qualification round / 3 : group round / 4 : knock-out round
		// info[1] = Time Limit
		// info[2] = Number of Levels
		byte[] info = ar.configure(ClientActionRobot.intToByteArray(id));
		solved = new int[info[2]];

		STAGE_COUNT = info[2];

		LoadData(); // Load the data

		currentLevel = (byte) getNextLevel();

		/**************************************/
		// System.err.println(STAGE_COUNT);
		// System.err.println(currentLevel);
		// System.exit(0);
		/**************************************/

		ar.getMyScore();
		ar.loadLevel(currentLevel);

		GameState state;

		while (true) {

			state = solve();

			// If the level is solved , go to the next level
			if (state == GameState.WON) {				
				
				System.out.println();

				solved[currentLevel - 1] = 1;
				
				SaveData();

				currentLevel = (byte) getNextLevel();

				// System.out.println("::::: " + currentLevel);

				ar.loadLevel(currentLevel);

				tp = new TrajectoryPlanner();
			}
			// Even we lost, keep continue
			else if (state == GameState.LOST) {
				currentLevel = (byte) getNextLevel();
				ar.loadLevel(currentLevel);
			} else if (state == GameState.LEVEL_SELECTION) {
				System.out
						.println("unexpected level selection page, go to the last current level : "
								+ currentLevel);
				ar.loadLevel(currentLevel);
			} else if (state == GameState.MAIN_MENU) {
				System.out
						.println("unexpected main menu page, reload the level : "
								+ currentLevel);
				ar.loadLevel(currentLevel);
			} else if (state == GameState.EPISODE_MENU) {
				System.out
						.println("unexpected episode menu page, reload the level: "
								+ currentLevel);
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
		VisionMBR visionMBR = new VisionMBR(screenshot);

		Rectangle sling = vision.findSlingshotMBR();

		// If the level is loaded (in PLAYING State)but no slingshot detected,
		// then the agent will request to fully zoom out.
		while (sling == null && ar.checkState() == GameState.PLAYING) {
			System.out
					.println("no slingshot detected. Please remove pop up or zoom out");

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
			ar.fullyZoomOut();
			screenshot = ar.doScreenShot();
			vision = new Vision(screenshot);
			sling = vision.findSlingshotMBR();
		}

		// get all the pigs
		List<ABObject> pigs = vision.findPigsMBR();

		GameState state = ar.checkState();
		// if there is a sling, then play, otherwise skip.
		if (sling != null) {

			// If there are pigs, we pick up a pig randomly and shoot it.
			if (!pigs.isEmpty()) {
				Point releasePoint = null;

				// SHS : find bird type on sling
				// return : ABType
				// RedBird(4), YellowBird(5), BlueBird(6), BlackBird(7),
				// WhiteBird(8), Unknown(0)
				ABType birdType = ar.getBirdTypeOnSling();

				System.out.println("::::: Bird Type : " + birdType.name());
				// System.err.println("::::: Bird Type : " + birdType.id);

				int whatBird;
				
				// RedBird = 1, BlueBird = 2, YellowBird = 3, BlackBird = 4, WhiteBird = 5
				switch (birdType.id) {
				case 4:
					whatBird = 1;
					break;
				case 5:
					whatBird = 3;
					break;
				case 6:
					whatBird = 2;
					break;
				case 7:
					whatBird = 4;
					break;
				case 8:
					whatBird = 5;
					break;
				default:
					whatBird = 1;
					break;
				}

				// System.out.println("## Select Angle ##");

				// System.out.println();
				System.out.println("::::: SelectAngle: " + select);
				// System.out.println();

				if (select == 0)
					releasePoint = selectAngle0(visionMBR, sling, whatBird);
				else
					releasePoint = selectAngle1(visionMBR, sling, whatBird);

				/*********************************/

				// random pick up a pig
				// ABObject pig =
				// pigs.get(randomGenerator.nextInt(pigs.size()));

				// Point _tpt = pig.getCenter();

				// if the target is very close to before, randomly choose a
				// point near it
				// if (prevTarget != null && distance(prevTarget, _tpt) < 10) {
				// double _angle = randomGenerator.nextDouble() * Math.PI * 2;
				// _tpt.x = _tpt.x + (int) (Math.cos(_angle) * 10);
				// _tpt.y = _tpt.y + (int) (Math.sin(_angle) * 10);
				// System.out.println("Randomly changing to " + _tpt);
				// }

				// prevTarget = new Point(_tpt.x, _tpt.y);

				// estimate the trajectory
				// ArrayList<Point> pts = tp.estimateLaunchPoint(sling, _tpt);

				// do a high shot when entering a level to find an accurate
				// velocity
				// if (firstShot && pts.size() > 1) {
				// releasePoint = pts.get(1);
				// } else
				// if (pts.size() == 1)
				// releasePoint = pts.get(0);
				// else
				// if(pts.size() == 2)
				// {
				// System.out.println("first shot " + firstShot);
				// randomly choose between the trajectories, with a 1 in
				// 6 chance of choosing the high one
				// if (randomGenerator.nextInt(6) == 0)
				// releasePoint = pts.get(1);
				// else
				// releasePoint = pts.get(0);
				// }
				Point refPoint = tp.getReferencePoint(sling);

				// White Bird Bug
				if (whatBird == 5) {
					releasePoint.x = refPoint.x - 100;
					releasePoint.y = refPoint.y + 100;
				}

				System.out.println("Release Point: " + releasePoint);

				// Get the release point from the trajectory prediction module
				int tapTime = 0;
				if (releasePoint != null) {
					double releaseAngle = tp.getReleaseAngle(sling,
							releasePoint);
					System.out.println("Release Point: " + releasePoint);
					System.out.println("Release Angle: "
							+ Math.toDegrees(releaseAngle));

					/*
					 * int tapInterval = 0; switch (ar.getBirdTypeOnSling()) {
					 * 
					 * case RedBird: tapInterval = 0; break; // start of
					 * trajectory case YellowBird: tapInterval = 65 +
					 * randomGenerator.nextInt(25);break; // 65-90% of the way
					 * case WhiteBird: tapInterval = 70 +
					 * randomGenerator.nextInt(20);break; // 70-90% of the way
					 * case BlackBird: tapInterval = 70 +
					 * randomGenerator.nextInt(20);break; // 70-90% of the way
					 * case BlueBird: tapInterval = 65 +
					 * randomGenerator.nextInt(20);break; // 65-85% of the way
					 * default: tapInterval = 60; }
					 * 
					 * tapTime = tp.getTapTime(sling, releasePoint, _tpt,
					 * tapInterval);
					 */

					// test
					/****************************************************************/
					System.out.println("## Calculate Tap Time ##");
					tapTime = calculateTaptime(visionMBR, sling, releasePoint, _tpt, whatBird);
					/****************************************************************/

				} else {
					System.err.println("No Release Point Found");
					return ar.checkState();
				}

				// check whether the slingshot is changed. the change of the
				// slingshot indicates a change in the scale.
				ar.fullyZoomOut();
				screenshot = ar.doScreenShot();
				vision = new Vision(screenshot);
				Rectangle _sling = vision.findSlingshotMBR();
				if (_sling != null) {
					double scale_diff = Math.pow((sling.width - _sling.width),
							2) + Math.pow((sling.height - _sling.height), 2);
					if (scale_diff < 25) {
						int dx = (int) releasePoint.getX() - refPoint.x;
						int dy = (int) releasePoint.getY() - refPoint.y;
						if (dx < 0) {
							ar.shoot(refPoint.x, refPoint.y, dx, dy, 0,
									tapTime, false);
							state = ar.checkState();
							if (state == GameState.PLAYING) {
								screenshot = ar.doScreenShot();
								vision = new Vision(screenshot);
								List<Point> traj = vision.findTrajPoints();
								tp.adjustTrajectory(traj, sling, releasePoint);
								// firstShot = false;
							}
						}
					} else
					{
						System.out
								.println("Scale is changed, can not execute the shot, will re-segement the image");
						
					}
				}
				else
				{
					System.out
					.println("no sling detected, can not execute the shot, will re-segement the image");
					
					ar.fullyZoomOut();
					
					if (sling != null)
					{
						int dx = (int) releasePoint.getX() - refPoint.x;
						int dy = (int) releasePoint.getY() - refPoint.y;
						if (dx < 0) {
							ar.shoot(refPoint.x, refPoint.y, dx, dy, 0,
									tapTime, false);
							state = ar.checkState();
							if (state == GameState.PLAYING) {
								screenshot = ar.doScreenShot();
								vision = new Vision(screenshot);
								List<Point> traj = vision.findTrajPoints();
								tp.adjustTrajectory(traj, sling, releasePoint);
								// firstShot = false;
							}
						}
					}
				}
					

			}
		}
		return state;
	}

	private double distance(Point p1, Point p2) {
		return Math
				.sqrt((double) ((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y)
						* (p1.y - p2.y)));
	}

	public static void main(String args[]) {

		ClientNaiveAgent na;
		if (args.length > 0)
			na = new ClientNaiveAgent(args[0]);
		else
			na = new ClientNaiveAgent();
		na.run();

	}

	public Point selectAngle1(VisionMBR vision, Rectangle slingshot, int birdtype) {
	      
	      Rectangle temp_f;
	      Point temp_fp = null;
	      Point temp_fr = null;
	      
	      Point min_angle = new Point();
	      float Strength, min = 99999;
	      Point min_target = new Point();
	      
	      vision.findStonesMBR();
	      vision.findIceMBR();
	      vision.findWoodMBR();
	      
	      // target = Pig + TNT
	      List<Rectangle> target = new ArrayList<Rectangle>();
	      List<Rectangle> TNTs = vision.findTNTsMBR();
	      List<ABObject> pigs = vision.findPigs();
	            
	      for (int i = 0; i < pigs.size(); i++)
	         target.add(pigs.get(i));
	      for (int i = 0; i < TNTs.size(); i++)
	         target.add(TNTs.get(i));
	            
	      Collections.sort(target, new targetCompare());  //  sort based x-coordinate
	      
	      int[][] Land = vision.findLand(slingshot); // Detecting Land
	            
	   
	      for (int i = 0; i < target.size(); i++) {
	         temp_f = target.get(0);
	         temp_fp = new Point((int) temp_f.getCenterX(), (int) (temp_f.getCenterY())); //
	         Rectangle temp_t = target.get(i); //
	         Point temp_p = new Point((int) temp_t.getCenterX(), (int) (temp_t.getCenterY())); //

	         //ArrayList<Point> pts2 = tp.estimateLaunchPoint(slingshot, temp_p);
	         
	         // 0 : Impossible Reach
	         // 1 : Only Low Shot
	         // 2 : Both Low and High Shot
	         ArrayList<Point> ptsf2 = tp.estimateLaunchPoint(slingshot, temp_fp);
	         ArrayList<Point> pts2 = tp.estimateLaunchPoint(slingshot, temp_p);
	         //System.err.println(pts2.size());
	         
	         if (pts2.size() == 0)
	         {
	            Strength = 0.01f; //
	            
	            Point refPoint = tp.getReferencePoint(slingshot);
	            Point release = new Point();
	            release.x = refPoint.x - 100;
	            release.y = refPoint.y + 100;
	            
	            if (min > Strength && Strength > 0) {
	               min = Strength;
	               min_angle = release;
	               min_target = temp_p;
	            }
	            continue;
	         }
	         
	         //
	         //Point LowPoint = pts2.get(0);
	         //Point HighPoint = pts2.get(0);

	         //
	         //if (pts2.size() > 1) {
	            //HighPoint = pts2.get(1);
	         //}
	         //angle = LowPoint; // Defalut = Low Angle

	         //
	         
	         for (int j = 0; j < pts2.size(); j++) {
	            if(i == 0)
	            {
	               if(pts2.size() >= 2)
	               {
	                  temp_fr = pts2.get(1);
	               }
	               else
	               {
	                  temp_fr = pts2.get(0);
	               }
	            }
	            Point release = pts2.get(j); //
	            System.out.println("pts2 size : " + pts2.size());
	            System.out.println("release : " + release);
	            List<Point> pred = tp.predictTrajectory(slingshot, release); 

	            //int[][] Land = vision.findLand(); //

	            Strength = 0; //

	            for (Point p : pred) {
	               if (p.y >= 480)
	                  p.y = 479;
	               Strength += 0.01;

	               //
	               if (temp_p.x - 7 < p.x)
	                  break;
	               //
	               for (int kk = p.y - 5; kk < p.y + 5; kk++) {
	                  if (kk < 475) {
	                     if (Land[kk][p.x] == 1)
	                        Strength = -1000;
	                  }
	               }
	               //
	               switch (birdtype) {
	               case 1: //
	                  if (vision.t_stone[p.y][p.x] == 1) {
	                     Strength += (float) vision.t_stone[p.y][p.x] * 3;
	                  }
	                  if (vision.t_wood[p.y][p.x] == 1) {
	                     Strength += (float) vision.t_wood[p.y][p.x];
	                  }
	                  if (vision.t_ice[p.y][p.x] == 1) {
	                     Strength += (float) vision.t_ice[p.y][p.x] * 0.2;
	                  }
	                  break;
	               case 2: //
	                  if (vision.t_stone[p.y][p.x] == 1) {
	                     Strength += (float) vision.t_stone[p.y][p.x] * 3;
	                  }
	                  if (vision.t_wood[p.y][p.x] == 1) {
	                     Strength += (float) vision.t_wood[p.y][p.x];
	                  }
	                  if (vision.t_ice[p.y][p.x] == 1) {
	                     Strength += (float) vision.t_ice[p.y][p.x] * 0.2;
	                  }
	                  break;
	               case 3: //
	                  if (vision.t_stone[p.y][p.x] == 1) {
	                     Strength += (float) vision.t_stone[p.y][p.x] * 3;
	                  }
	                  if (vision.t_wood[p.y][p.x] == 1) {
	                     Strength += (float) vision.t_wood[p.y][p.x];
	                  }
	                  if (vision.t_ice[p.y][p.x] == 1) {
	                     Strength += (float) vision.t_ice[p.y][p.x] * 1.5;
	                  }
	                  break;
	               case 4: //
	                  if (vision.t_stone[p.y][p.x] == 1) {
	                     Strength += (float) vision.t_stone[p.y][p.x] * 3;
	                  }
	                  if (vision.t_wood[p.y][p.x] == 1) {
	                     Strength += (float) vision.t_wood[p.y][p.x];
	                  }
	                  if (vision.t_ice[p.y][p.x] == 1) {
	                     Strength += (float) vision.t_ice[p.y][p.x] * 0.2;
	                  }
	                  break;
	               case 5: //
	                  if (vision.t_stone[p.y][p.x] == 1) {
	                     Strength += (float) vision.t_stone[p.y][p.x];
	                  }
	                  if (vision.t_wood[p.y][p.x] == 1) {
	                     Strength += (float) vision.t_wood[p.y][p.x] * 3;
	                  }
	                  if (vision.t_ice[p.y][p.x] == 1) {
	                     Strength += (float) vision.t_ice[p.y][p.x] * 2;
	                  }
	                  break;
	               default:
	                  break;
	               }
	            }
	            if (j == 1)
	               Strength *= 1.5;

	            //
	            if (min > Strength && Strength > 0) {
	               min = Strength;
	               min_angle = release;
	               min_target = temp_p;
	            }

	            System.out.println("Current Strength : " + Strength);
	            System.out.println("min_target" + min_target);
	            /*
	            System.out
	                  .println("Minimum Strength : " + min + " / Angle : x "
	                        + min_angle.x + " y : " + min_angle.y);
	                        */
	         }
	      }
	      
	      
	      if(min_target.x == 0 && min_target.y == 0)
	      {
	         min_angle = temp_fr;
	         min_target = temp_fp;
	         
	      }
	      _tpt = min_target;
	      
	      System.out.println();
	      System.out.println("::::: the final target point is " + _tpt);
	      System.out.println();

	      return min_angle;
	   }

	public Point selectAngle0(VisionMBR vision, Rectangle slingshot, int birdtype) {
		
		//Point angle = new Point(); // Choice Low or High Angle 
		Point max_angle = new Point();
		Point max_target = new Point();
		float max = -1;
		//int max_object = -1;
		int LowHigh = 0; //
		
		// Object(stone, ice, wood) position check
		// Each Object bound check
		// 0 ~ stone | stone+1 ~ ice | ice+1 ~ wood
		vision.findStonesMBR();
		int stone_bound = vision.ob_count;
		vision.findIceMBR();
		int ice_bound = vision.ob_count;
		vision.findWoodMBR();
		int wood_bound = vision.ob_count;		
		
		// target = Pig + TNT
		List<Rectangle> target = new ArrayList<Rectangle>();
		List<Rectangle> TNTs = vision.findTNTsMBR();
		List<ABObject> pigs = vision.findPigs();
		
		for (int i = 0; i < pigs.size(); i++)
			target.add(pigs.get(i));
		for (int i = 0; i < TNTs.size(); i++)
			target.add(TNTs.get(i));
		
		Collections.sort(target, new targetCompare());  //  sort based x-coordinate
		
		int[][] Land = vision.findLand(slingshot); // Detecting Land

		for (int i = 0; i < target.size(); i++) {
			// get one target
			Rectangle temp_t = target.get(i);

			// get the target point
			Point temp_p = new Point((int) temp_t.getCenterX(),	(int) (temp_t.getCenterY()));
			System.out.println("::::: the target point is " + temp_p);
			
			// 0 : Impossible Reach
			// 1 : Only Low Shot
			// 2 : Both Low and High Shot
			ArrayList<Point> pts2 = tp.estimateLaunchPoint(slingshot, temp_p);
			//System.err.println(pts2.size());
			
			if (pts2.size() == 0)
			{
				
				Point refPoint = tp.getReferencePoint(slingshot);
				Point release = new Point();
				release.x = refPoint.x - 100;
				release.y = refPoint.y + 100;
				
				float influen = 0; //
				
				if (max < influen) {
					max = influen;
					max_angle = release;
					max_target = temp_p;
					//LowHigh = j;
				}
				
				continue;
			}
				

			for (int j = 0; j < pts2.size(); j++) {
				// get each angle
				Point release = pts2.get(j);

				List<Point> pred = tp.predictTrajectory(slingshot, release);

				int birdHP = 4; // base Bird HP
				int isLand = 0;
				int total_Object = 0; //
				int breakOB = 0; //
				int prevOB = 0; //
				float influen = 0; //

				if (j == 1) // If Angle is High, Bird's Power is Low
					birdHP = 3;

				for (Point p : pred) {
					if ((0 < p.y) && (p.y < 480) && (p.x < 840)) {
						
						// Check Land on Trajectory 
						for (int k = p.y - 5; k < p.y + 5; k++) {
							if ((0 < k) && (k < 480) && (p.x < 840))
								if (Land[k][p.x] == 1) {
									isLand = 1;
									birdHP -= 10;
									total_Object++;
									System.out.println("## Land Point = " + p + " / ");
									break;
								}
						}
 
						if ((temp_p.x - 7 < p.x) || (isLand == 1)) {
							if (birdHP >= 0) {
								breakOB++;
								total_Object++;
							}

							break;
						}

						int currentOB = vision.t_object[p.y][p.x];
						
						if (prevOB != currentOB) {
							if (currentOB != 0)
								total_Object++;

							switch (birdtype) {
							case 1: // red bird
								if ((0 < currentOB)
										&& (currentOB <= stone_bound))
									birdHP -= 4;
								else if ((stone_bound < currentOB)
										&& (currentOB <= ice_bound))
									birdHP -= 3;
								else if ((ice_bound < currentOB)
										&& (currentOB <= wood_bound))
									birdHP -= 4;
								break;
							case 2: // blue bird
								if ((0 < currentOB)
										&& (currentOB <= stone_bound))
									birdHP -= 5;
								else if ((stone_bound < currentOB)
										&& (currentOB <= ice_bound))
									birdHP -= 1;
								else if ((ice_bound < currentOB)
										&& (currentOB <= wood_bound))
									birdHP -= 3;
								break;
							case 3: // yellow bird
								if ((0 < currentOB)
										&& (currentOB <= stone_bound))
									birdHP -= 5;
								else if ((stone_bound < currentOB)
										&& (currentOB <= ice_bound))
									birdHP -= 3;
								else if ((ice_bound < currentOB)
										&& (currentOB <= wood_bound))
									birdHP -= 1;
								break;
							case 4: // black bird
							case 5: // white bird
								if ((0 < currentOB)
										&& (currentOB <= stone_bound))
									birdHP -= 1;
								else if ((stone_bound < currentOB)
										&& (currentOB <= ice_bound))
									birdHP -= 1;
								else if ((ice_bound < currentOB)
										&& (currentOB <= wood_bound))
									birdHP -= 1;
								break;
							default:
								break;
							}
							
							if ((currentOB != 0) && (0 <= birdHP))
								breakOB++;
						}

						prevOB = currentOB;
					}
				}

				if ((total_Object == 0) && (isLand == 1))
					influen = 0;
				else if ((j == 0) && (isLand == 1)) 
					influen = 0;
				else
					influen = (float) breakOB / (float) total_Object;

				if (max < influen) {
					max = influen;
					max_angle = release;
					max_target = temp_p;
					LowHigh = j;
				}

				double releaseAngle = tp.getReleaseAngle(slingshot, max_angle);

				System.out.println("::::: Current Influence : " + influen);
				System.out.println("::::: Max Influence : " + max + " / Max_angle : " + Math.toDegrees(releaseAngle));
			}

		}

		max_target.y -= 7;
		ArrayList<Point> t_pts = tp.estimateLaunchPoint(slingshot, max_target);
		
		_tpt = max_target;
		System.out.println("::::: the final target point is " + _tpt);
		
		if (t_pts.size() != 0)
			max_angle = t_pts.get(LowHigh);

		return max_angle;
	}

	static class targetCompare implements Comparator<Rectangle> {
		@Override
		public int compare(Rectangle a1, Rectangle a2) {
			double o1 = a1.getCenterX();
			double o2 = a2.getCenterX();

			if (o1 > o2)
				return 1;
			else if (o1 < o2)
				return -1;
			else
				return 0;
		}
	}

	public int calculateTaptime(VisionMBR vision, Rectangle slingshot, Point release, Point target, int birdColor) {
		//red : 1 blue : 2 yellow : 3 black : 4 white : 5
		
		int[][] Land = vision.findLand(slingshot); //

		List<Point> pred = tp.predictTrajectory(slingshot, release); //

		int tap = -1;
		Point tt = new Point(target);

		switch (birdColor) {
		case 2: //
			for (Point p : pred) {
				if ((0 < p.y) && (p.y < 480) && (p.x < 840)) {
					if ((vision.t_object[p.y][p.x] > 0) || (Land[p.y][p.x] > 0) || (target.x - 7 < p.x))
					{
						tt = p;
						tt.x = tt.x - 70;
						break;
					}
				}
			}
			System.out.println("::::: Tap Point x : " + tt.x + " y : " + tt.y);
			tap = tp.getTapTime(slingshot, release, tt);
			break;
		case 3: //
			for (Point p : pred) {
				if ((0 < p.y) && (p.y < 480) && (p.x < 840)) {
					if ((vision.t_object[p.y][p.x] > 0) || (Land[p.y][p.x] > 0)	|| (target.x - 7 < p.x))
					{
						tt = p;
						tt.x = tt.x - 30;
						
						break;
					}
				}
			}
			System.out.println("::::: Tap Point x : " + tt.x + " y : " + tt.y);
			tap = tp.getTapTime(slingshot, release, tt);
			break;
		case 1: //
		case 4: //
			tap = -1;
			break;
		case 5: //
			tt.x = tt.x - 20;
			for (Point p : pred) {
				if ((0 < p.y) && (p.y < 480) && (p.x < 840))
					if ((vision.t_object[p.y][p.x] > 0) || (Land[p.y][p.x] > 0)	|| (target.x - 7 < p.x)) //
					{
						tt = p;
						tt.x = tt.x - 20;
						break;
					}
			}
			System.out.println("::::: Tap Point x : " + tt.x + " y : " + tt.y);
			tap = tp.getTapTime(slingshot, release, tt);
			break;
		default:
			break;
		}

		return tap;
	}

	/**
	 * Check don't clear stage
	 */
	private void ShowUndoneList() {
		if (undone_cnt == 0) {
			if (stageLoop == true)
				System.out.print("::::: All Cleared!!! :::::");
			else
				System.out.print("::::: Everything's going fine :::::");
		} else {
			if (undone_cnt > 0) {
				System.out.print("::::: Undone Stages:");
				for (int tmp = 0; tmp < STAGE_COUNT; tmp++) {
					if (solved[tmp] == 0)
						System.out.print(tmp + 1 + " ");
				}
				System.out.println();
			}
		}
	}

	private void SaveData() {
		try {
			FileWriter fw = new FileWriter("save.txt"); // Write file. (If file
														// already exists, just
														// overwrite)
			BufferedWriter bw = new BufferedWriter(fw);
			String s;

			if (stageLoop == true)
				bw.write("T");
			else
				bw.write("F");

			for (int tmp = 0; tmp < STAGE_COUNT; tmp++) {
				//if (stageLoop == true || tmp + 1 < currentLevel) {
					s = String.valueOf(solved[tmp]); // convert int to
															// string
					bw.write(s); // save
				//} else
					//bw.write('1'); // not tried stage
			}

			bw.close();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Load the saved data Stage Clear State
	 **/
	private void LoadData() {
		try {
			FileReader fr = new FileReader("save.txt");
			BufferedReader br = new BufferedReader(fr);
			String tmp = br.readLine();
			char[] tmp2 = new char[STAGE_COUNT + 1];
			tmp.getChars(0, STAGE_COUNT + 1, tmp2, 0);

			// Loop Check
			if (tmp2[0] == 'T') // looped
			{
				System.out
						.println("::::: Loading data... Loop completed  :::::");
				stageLoop = true;
			} else if (tmp2[0] == 'F') // not looped
			{
				System.out
						.println("::::: Loading data... Loop incompleted  :::::");
				stageLoop = false;
			} else
				// wrong data
				throw new Exception("::::: File Corrupted :::::");

			// Check Stage Clear Status
			for (int x = 0; x < STAGE_COUNT; x++) // read by CHAR -> convert to
													// INT
			{
				System.out.print("::::: Loading data... Level " + (x + 1));

				// Load file and save it to levelStatus
				// 0 = don't clear
				// 1 = clear
				if (tmp2[x + 1] == '0') {
					solved[x] = 0;
					undone_cnt++;
					System.out.println(" incompleted :::::");
				} else {
					solved[x] = 1;
					System.out.println(" completed :::::");
				}
			}

			System.out.println("::::: Load complete!! :::::");
			ShowUndoneList();

		} catch (FileNotFoundException e) { // File Not Found
			System.out
					.println("::::: Save data not found. Start new game :::::");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Get first stage(NOT FIRST LEVEL) when program starts
	 */
	/*
	private int GetStartStage() {
		int tmp;

		for (tmp = 0; tmp < STAGE_COUNT; tmp++) {
			if (levelStatus[tmp] == false) {
				return tmp;
			}
		}

		return 0;
	}
	*/
	/*
	private void NewStageInitialize(boolean clearedType) {
		SaveData();
		ShowUndoneList();
		ar.loadLevel(currentLevel);

		
		// prevent error if we won last stage, we ALWAYS go to next stage. for
		// example, after we cleared stage #3 and we supposed to go #6, but we
		// go to #4, NOT #6.
		
		if (clearedType == true)
			ar.loadLevel(currentLevel);

		System.out.println(" loading the level " + currentLevel);

		// display the global best scores
		int[] scores = ar.checkScore();
		System.out.println("The global best score: ");
		for (int i = 0; i < scores.length; i++)
			System.out.print(" level " + (i + 1) + ": " + scores[i]);

		System.out.println();
		System.out.println(" My score: ");
		scores = ar.checkMyScore();
		for (int i = 0; i < scores.length; i++)
			System.out.print(" level " + (i + 1) + ": " + scores[i]);

		System.out.println();
		// make a new trajectory planner whenever a new level is entered
		tp = new TrajectoryPlanner();

		// first shot on this level, try high shot first
		// firstShot = true;
	}
	*/
	/*
	 * ============================================ Custom Function End
	 * ============================================
	 */

}
