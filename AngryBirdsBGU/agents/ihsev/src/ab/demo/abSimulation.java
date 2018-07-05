package ab.demo;

import ab.vision.ABObject;
import ab.vision.ABShape;
import ab.vision.HullUtils;
import ab.vision.real.shape.Circle;
import ab.vision.real.shape.Poly;
import ab.vision.real.shape.Rect;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.testbed.framework.TestbedSettings;
import org.jbox2d.testbed.framework.TestbedTest;

public class abSimulation extends TestbedTest
{
	private static int simNumber = 0;
	private String testName = "";
	private float imgHeight, imgWidth;
	private Hashtable<String, List<Rec2D> > objects;
	private float groundLvl;
	//private List<Rec2D> supportGround;
	private List<ChainShape> supportGround;
	private List<Rec2D> supports;
	private Vec2 referencePoint;
	private Vec2 shootDir;
	private float sceneScale;
	private float gravity;
	private List<Vec2> trajectory;
	private float slingHeight;
	private float slingWidth;
	
	private Hashtable<Body, String> bodyAnnotation;
	private List<Fixture> fixturesToDestroy;
	//private float pigRadius;
	
	private Point releasePoint;
	private long tapTime;
	private boolean tapped;
	private int pigsKilled;
	private int objectsDestroyed;
	private int totalCollisions;
	private long birdHit;
	private boolean birdFirstHit;
	private boolean finished;
	Body shootingBird;
	Fixture shootingBirdFix;
	private long startTime;
	private List<Body> bodiesToHoldBeforeHit;
	private List<Body> activeBodies;
	private float finishTimeRatio;
	private long timeKilledFirstPig;
	
	public int getPigsKilled()
	{
		return pigsKilled;
	}
	
	public int getObjectsDestroyed()
	{
		return objectsDestroyed;
	}
	
	public int getTotalCollisions()
	{
		return totalCollisions;
	}
	
	public void setReleasePoint(Point rp)
	{
		releasePoint = rp;
	}
	
	public Point getReleasePoint()
	{
		return releasePoint;
	}
	
	public void setFinishTimeRatio(float r)
	{
		finishTimeRatio = r;
	}
	
	public long getTimeKilledFirstPig()
	{
		return timeKilledFirstPig;
	}
	
	public abSimulation copy()
	{
		abSimulation copy = new abSimulation(sceneScale, referencePoint);
		copy.imgHeight = this.imgHeight;
		copy.imgWidth = this.imgWidth;
		copy.objects = new Hashtable<String, List<Rec2D> >();
		Set<String> keys = this.objects.keySet();
		for(String key: keys)
		{
			List<Rec2D> oList = this.objects.get(key);
			List<Rec2D> list = new ArrayList<Rec2D>();
			for (int i=0; i<oList.size(); ++i)
			{
				list.add(new Rec2D(oList.get(i)));
			}
			copy.objects.put(key, list);
		}
		copy.groundLvl = this.groundLvl;
		//copy.supportGround = new ArrayList<Rec2D>();
		copy.supportGround = new ArrayList<ChainShape>();
		for (int i=0; i<this.supportGround.size(); ++i)
		{
			copy.supportGround.add((ChainShape)this.supportGround.get(i).clone());
		}
		copy.supports = new ArrayList<Rec2D>();
		for (int i=0; i<this.supports.size(); ++i)
		{
			copy.supports.add(new Rec2D(this.supports.get(i)));
		}
		copy.shootDir = new Vec2(this.shootDir);
		copy.sceneScale = this.sceneScale;
		copy.gravity = this.gravity;
		copy.trajectory = new ArrayList<Vec2>();
		for (int i=0; i<this.trajectory.size(); ++i)
		{
			copy.trajectory.add(new Vec2(this.trajectory.get(i)));
		}
		copy.finishTimeRatio = this.finishTimeRatio;
		copy.tapTime = this.tapTime;
		copy.slingHeight = this.slingHeight;
		copy.slingWidth = this.slingWidth;
		
		return copy;
	}
	
	private boolean SHOW_TRAJECTORY = false;
	
	public void setDebugTrajectory(boolean b)
	{
		SHOW_TRAJECTORY = b;
	}
	
	public abSimulation(float scale, Vec2 refPoint)
	{
		super();
		testName = "Simulation_"+simNumber;
		simNumber++;
		
		sceneScale = scale;
		//System.out.println("scene scale: " + sceneScale);
		trajectory = new ArrayList<Vec2>();
		referencePoint = refPoint;
		
		bodyAnnotation = new Hashtable<Body, String>();
		fixturesToDestroy = new ArrayList<Fixture>();
		//pigRadius = 1.0f; //dummy
		
		pigsKilled = 0;
		objectsDestroyed = 0;
		totalCollisions = 0;
		birdHit = 0l;
		birdFirstHit = true;
		finished = false;
		shootingBird = null;
		shootingBirdFix = null;
		tapped = false;
		startTime = 0l;
		bodiesToHoldBeforeHit = new ArrayList<Body>();
		activeBodies = new ArrayList<Body>();
		finishTimeRatio = 1.0f;
		tapTime = -1l; //never
		timeKilledFirstPig = -1l;
	}
	
	public void setTrajectory(List<Point> traj)
	{
		trajectory = new ArrayList<Vec2>();
		for (int i=0; i<traj.size(); ++i)
		{
			Vec2 v = new Vec2();
			v.x = adjustValueX((float)traj.get(i).getX());
			v.y = adjustValueY((float)traj.get(i).getY());
			trajectory.add(v);
		}
	}
	
	public void setSlingSize(float height, float width)
	{
		slingHeight = height/sceneScale;
		slingWidth = width/sceneScale;
	}
	
	public void setTapTime(long tap)
	{
		tapTime = tap;
	}
	
	public int getTapTime()
	{
		return (int)tapTime;
	}
	
	public void setObjects(Hashtable<String, List<Rectangle> > obj)
	{
		objects = new Hashtable<String, List<Rec2D> >();
		
		Set<String> keys = obj.keySet();
		for(String key: keys)
		{
			List<Rectangle> oList = obj.get(key);
			
			List<Rec2D> list = new ArrayList<Rec2D>();
			for (int i=0; i<oList.size(); ++i)
			{
				Rec2D r = adjustRectangle(oList.get(i));
                                
                                //test for helmet pigs
                                if ("stones".equals(key) && (Math.abs(r.height-r.width)<0.0001f))
                                {
                                    boolean foundPossibleHelmet = false;
                                    List<Rec2D> pigList = objects.get("pigs");
                                    for (int j=0; j<pigList.size(); ++j)
                                    {
                                        float px = pigList.get(j).x - r.x;
                                        float py = pigList.get(j).y - r.y;
                                        if (Math.sqrt(px*px + py*py) < r.height*Math.sqrt(2))
                                        {
                                            foundPossibleHelmet = true;
                                            System.out.println("Found possible helmet !! Removing object from scene.");
                                            break;
                                        }
                                    }
                                    if (foundPossibleHelmet)
                                    {
                                        continue;
                                    }
                                }
                                
				list.add(r);
			}
			objects.put(key, list);
		}
	}
	
	public void setGroundLevel(float ground)
	{
		groundLvl = adjustValueY(ground);
	}
	
	//public void setSupportGround(List<Rectangle> support)
	public void setSupportGround(List<List<Vec2> > support)
	{
		supportGround = new ArrayList<ChainShape>();
		
		for (int i=0; i<support.size(); ++i)
		{
			ChainShape cs = new ChainShape();
			Vec2[] vertices = new Vec2[support.get(i).size()];
			support.get(i).toArray(vertices);
			for (int j=0; j<vertices.length; ++j)
			{
				vertices[j].x = adjustValueX(vertices[j].x);
				vertices[j].y = adjustValueY(vertices[j].y);
			}
			cs.createLoop(vertices, support.get(i).size());
			supportGround.add(cs);
		}
	}
	
	public void setSupportPlatforms(List<Rectangle> sups)
	{
		supports = new ArrayList<Rec2D>();
		for (int i=0; i<sups.size(); ++i)
		{
			Rec2D r = adjustRectangle(sups.get(i));
			supports.add(r);
		}
	}
	
	public void setShootingVector(Vec2 sdir)
	{
		shootDir = sdir;
	}
        
        public Vec2 getShootingVector()
        {
            return shootDir;
        }
	
	public void setGravity(float g)
	{
		gravity = g;
	}
	
	private float adjustValueX(float v)
	{
		return (v-referencePoint.x)/sceneScale;
	}
	
	private float adjustValueY(float v)
	{
		return -(v-referencePoint.y)/sceneScale;
	}
	
	private Rec2D adjustRectangle(Rectangle rec)
	{
            Rec2D r = new Rec2D();

            if (rec instanceof Rect)
            {
                Rect blue = (Rect)rec;
                //r.width = ((float)(blue.getpLength()-0.5)) / sceneScale;
                //r.height = ((float)(blue.getpWidth()-0.5)) / sceneScale;
                r.width = ((float)(blue.getpLength()-1.0)) / sceneScale;
                r.height = ((float)(blue.getpWidth()-1.0)) / sceneScale;
                r.angle = (float)(-blue.angle);

                r.shape = blue.shape;
                r.x = adjustValueX((float)blue.getCenterX());
                r.y = adjustValueY((float)blue.getCenterY());
            }
            else if (rec instanceof Circle)
            {
                Circle blue = (Circle)rec;
                float myRadius = ((float)blue.r) / sceneScale;
                r.width = myRadius*2;
                r.height = myRadius*2;
                r.angle = 0.0f;

                r.shape = blue.shape;
                r.x = adjustValueX((float)blue.getCenterX());
                r.y = adjustValueY((float)blue.getCenterY());
            }
            else if (rec instanceof Poly)
            {
                Poly blue = (Poly)rec;
                
                r.shape = blue.shape;
                
                ArrayList<Point> polyPoints = new ArrayList<>();
                java.awt.Polygon poly = blue.polygon;
                for (int i=0; i<poly.npoints; ++i)
                {
                    polyPoints.add(new Point(poly.xpoints[i], poly.ypoints[i]));
                }
                HullUtils hutils = new HullUtils(polyPoints);
                
                int shape = hutils.whatIsIt(sceneScale);
                //System.out.println("Hollow? "+blue.hollow);
                
                if (shape == 1)
                {
                    Rec2D square = hutils.getSmallestSquare();
                    r.shape = ABShape.Rect;
                    r.x = adjustValueX(square.x);
                    r.y = adjustValueY(square.y);
                    r.height = square.height/sceneScale;
                    r.width = square.width/sceneScale;
                    r.angle = -square.angle;
                }
                else if (shape >= 2 && shape <= 2+3)
                {
                    Rec2D square = hutils.getSmallestSquare();
                    r.shape = ABShape.Triangle;
                    r.x = adjustValueX(square.x);
                    r.y = adjustValueY(square.y);
                    r.height = square.height/sceneScale;
                    r.width = square.width/sceneScale;
                    r.angle = -square.angle-(float)((shape-2)*Math.PI/2.0);
                }
                
                
                /*
                System.out.println("Area: " + blue.area);
                
                r.shape = blue.shape;
                
                r.polyBlueprint = new ArrayList<>();
                java.awt.Polygon poly = blue.polygon;
                for (int i=0; i<poly.npoints; ++i)
                {
                    r.polyBlueprint.add(new Vec2(adjustValueX(poly.xpoints[i]), adjustValueY(poly.ypoints[i])));
                }
                
                System.out.println("size: " + r.polyBlueprint.size());
                System.out.println("Hollow: " + blue.hollow);
                //clean up points
                for (int i=1; i<r.polyBlueprint.size(); ++i)
                {
                    if (MathUtils.distanceSquared(r.polyBlueprint.get(i-1), r.polyBlueprint.get(i)) < Settings.linearSlop * Settings.linearSlop)
                    {
                        r.polyBlueprint.remove(i);
                        i--;
                    }
                }
                */
            }
            else
            {
                float tmpWidth = rec.width / sceneScale;
		float tmpHeight = rec.height / sceneScale;
                
                r.width = tmpWidth;
		r.height = tmpHeight;
		
		r.x = adjustValueX(rec.x);
		r.x = r.x + (tmpWidth/2.0f);
		r.y = adjustValueY(rec.y);
		r.y = r.y - (tmpHeight/2.0f);
            }

            return r;
	}
	
	@Override
	public void setCamera(Vec2 argPos)
	{
		//I don't like cameras
	}
	
	@Override
	public void setCamera(Vec2 argPos, float scale)
	{
		//I don't like cameras
	}
	
	@Override
	public void setTitle(String str)
	{
		//I don't like titles
	}
	
	//-----------------------------------------------------------------------------//

	@Override
	public boolean isSaveLoadEnabled()
	{
		return true;
	}

	@Override
	public void initTest(boolean deserialized)
	{
		if (deserialized)
		{
			return;
		}
		
		bodyAnnotation = new Hashtable<Body, String>();
		fixturesToDestroy = new ArrayList<Fixture>();
		//pigRadius = 1.0f; //dummy
		pigsKilled = 0;
		objectsDestroyed = 0;
		totalCollisions = 0;
		birdHit = 0l;
		birdFirstHit = true;
		finished = false;
		shootingBird = null;
		shootingBirdFix = null;
		tapped = false;
		startTime = 0l;
		bodiesToHoldBeforeHit = new ArrayList<Body>();
		activeBodies = new ArrayList<Body>();
		//finishTimeRatio = 1.0f;
		
		m_world.setGravity(new Vec2(0.0f, gravity));
		
		int count = 20;
		{
			BodyDef bd = new BodyDef();
			bd.type = BodyType.STATIC;
			bd.position.set(adjustValueX(0.0f), groundLvl);
			Body ground = getWorld().createBody(bd);
			
			EdgeShape shape = new EdgeShape();
			shape.set(new Vec2(-40000.0f, 0f), new Vec2(40000.0f, 0f));
			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.friction = 0.8f;
			fd.restitution = 0.0f;
			fd.density = 0.5f;
			ground.createFixture(fd);
		}
		
		Rec2D o;
		
		//create support ground
		for (int i=0; i<supportGround.size(); ++i)
		{
			//o = supportGround.get(i);
			//PolygonShape shape = new PolygonShape();
			ChainShape shape = supportGround.get(i);
			//shape.setAsBox(o.width/2.0f, o.height/2.0f);
			BodyDef bd = new BodyDef();
			bd.type = BodyType.STATIC;
			//bd.position.set(new Vec2(o.x, o.y));
			Body body = getWorld().createBody(bd);
			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.friction = 0.8f;
			fd.restitution = 0.0f;
			fd.density = 0.5f;
			body.createFixture(fd);
		}
		//create support platforms
		for (int i=0; i<supports.size(); ++i)
		{
			o = supports.get(i);
			PolygonShape shape = new PolygonShape();
			shape.setAsBox(o.width/2.0f, o.height/2.0f);
			BodyDef bd = new BodyDef();
			bd.type = BodyType.STATIC;
			bd.position.set(new Vec2(o.x, o.y));
			Body body = getWorld().createBody(bd);
			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.friction = 0.7f;
			fd.restitution = 0.05f;
			fd.density = 0.5f;
			body.createFixture(fd);
			body.createFixture(shape, 5.0f); //TODO change density (5)
		}
		
		float densityParam = 4.0f;
		float restitutionParam = 1.0f; //0.8f;
		
		List<Rec2D> objs;
		
		//create stones
		objs = objects.get("stones");
		for (int i=0; i<objs.size(); ++i)
		{
			o = objs.get(i);
			Shape shape = null;
			if (o.shape.equals(ABShape.Circle))
			{
                            shape = new CircleShape();
                            shape.m_radius = Math.max(o.width/2.0f, o.height/2.0f);
			}
                        else if (o.shape.equals(ABShape.Rect))
			{
                            shape = new PolygonShape();
                            ((PolygonShape)shape).setAsBox(o.width/2.0f, o.height/2.0f);
			}
                        else if (o.shape.equals(ABShape.Triangle))
                        {
                            PolygonShape sh = new PolygonShape();
                            Vec2[] vertices = new Vec2[3];
                            vertices[0] = new Vec2(o.height/2.0f, -o.height/2.0f);
                            vertices[1] = new Vec2(o.height/2.0f, o.height/2.0f);
                            vertices[2] = new Vec2(-o.height/2.0f, -o.height/2.0f);
                            sh.set(vertices, 3);
                            shape = sh;
                        }
                        else if (o.shape.equals(ABShape.Poly))
                        {
                            continue; //Hey! We don't serve their kind here! Your Polys. They'll have to wait outside. We don't want them here.
                        }
                        else
                        {
                            System.out.println("Unknown shape ["+o.shape+"]");
                        }
			BodyDef bd = new BodyDef();
			bd.type = BodyType.DYNAMIC;
			bd.position.set(new Vec2(o.x, o.y));
			bd.angle = o.angle;
			Body body = getWorld().createBody(bd);
			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.friction = 4.0f;
			fd.restitution = 0.1f*restitutionParam;
			fd.density = 6.0f*densityParam;
                        body.createFixture(fd);
                        bodyAnnotation.put(body, "STONE");
                        bodiesToHoldBeforeHit.add(body);
		}
		
		//create ice
		objs = objects.get("ice");
		for (int i=0; i<objs.size(); ++i)
		{
			o = objs.get(i);
			Shape shape = null;
			if (o.shape.equals(ABShape.Circle))
			{
				shape = new CircleShape();
				shape.m_radius = Math.max(o.width/2.0f, o.height/2.0f);
			}
                        else if (o.shape.equals(ABShape.Rect))
			{
				shape = new PolygonShape();
				((PolygonShape)shape).setAsBox(o.width/2.0f, o.height/2.0f);
			}
                        else if (o.shape.equals(ABShape.Triangle))
                        {
                            PolygonShape sh = new PolygonShape();
                            Vec2[] vertices = new Vec2[3];
                            vertices[0] = new Vec2(o.height/2.0f, -o.height/2.0f);
                            vertices[1] = new Vec2(o.height/2.0f, o.height/2.0f);
                            vertices[2] = new Vec2(-o.height/2.0f, -o.height/2.0f);
                            sh.set(vertices, 3);
                            shape = sh;
                        }
                        else if (o.shape.equals(ABShape.Poly))
                        {
                            continue; //Hey! We don't serve their kind here! Your Polys. They'll have to wait outside. We don't want them here.
                        }
                        else
                        {
                            System.out.println("Unknown shape ["+o.shape+"]");
                        }
			BodyDef bd = new BodyDef();
			bd.type = BodyType.DYNAMIC;
			bd.position.set(new Vec2(o.x, o.y));
			bd.angle = o.angle;
			Body body = getWorld().createBody(bd);
			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.friction = 0.7f;
			fd.restitution = 0.2f*restitutionParam;
			fd.density = 0.75f*densityParam;
			body.createFixture(fd);
			bodyAnnotation.put(body, "ICE");
			bodiesToHoldBeforeHit.add(body);
		}
		
		//create wood
		objs = objects.get("wood");
		for (int i=0; i<objs.size(); ++i)
		{
			o = objs.get(i);
			Shape shape = null;
			if (o.shape.equals(ABShape.Circle))
			{
				shape = new CircleShape();
				shape.m_radius = Math.max(o.width/2.0f, o.height/2.0f);
			}
                        else if (o.shape.equals(ABShape.Rect))
			{
				shape = new PolygonShape();
				((PolygonShape)shape).setAsBox(o.width/2.0f, o.height/2.0f);
			}
                        else if (o.shape.equals(ABShape.Triangle))
                        {
                            PolygonShape sh = new PolygonShape();
                            Vec2[] vertices = new Vec2[3];
                            vertices[0] = new Vec2(o.height/2.0f, -o.height/2.0f);
                            vertices[1] = new Vec2(o.height/2.0f, o.height/2.0f);
                            vertices[2] = new Vec2(-o.height/2.0f, -o.height/2.0f);
                            sh.set(vertices, 3);
                            shape = sh;
                        }
                        else if (o.shape.equals(ABShape.Poly))
                        {
                            continue; //Hey! We don't serve their kind here! Your Polys. They'll have to wait outside. We don't want them here.
                        }
                        else
                        {
                            System.out.println("Unknown shape ["+o.shape+"]");
                        }
			BodyDef bd = new BodyDef();
			bd.type = BodyType.DYNAMIC;
			bd.position.set(new Vec2(o.x, o.y));
			bd.angle = o.angle;
			Body body = getWorld().createBody(bd);
			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.friction = 4.0f;
			fd.restitution = 0.0f*restitutionParam;
			fd.density = 1.5f*densityParam;
			body.createFixture(fd);
			bodyAnnotation.put(body, "WOOD");
			bodiesToHoldBeforeHit.add(body);
		}
		
		//create tnts
		objs = objects.get("tnts");
		for (int i=0; i<objs.size(); ++i)
		{
			o = objs.get(i);
			PolygonShape shape = new PolygonShape();
			shape.setAsBox(o.width/2.0f, o.height/2.0f);
			BodyDef bd = new BodyDef();
			bd.type = BodyType.DYNAMIC;
			bd.position.set(new Vec2(o.x, o.y));
			bd.angle = o.angle;
			Body body = getWorld().createBody(bd);
			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.friction = 0.7f;
			fd.restitution = 0.4f*restitutionParam;
			fd.density = 0.75f*densityParam;
			body.createFixture(fd);
			bodyAnnotation.put(body, "TNT");
			bodiesToHoldBeforeHit.add(body);
		}
		
		//-----------------------------------------------------------------//
		
		//create piggies
		objs = objects.get("pigs");
		for (int i=0; i<objs.size(); ++i)
		{
			o = objs.get(i);
			CircleShape shape = new CircleShape();
			//shape.m_radius = Math.max(o.width/2.0f, o.height/2.0f);
			shape.m_radius = slingHeight*0.22f*0.5f;
			BodyDef bd = new BodyDef();
			bd.type = BodyType.DYNAMIC;
			bd.position.set(new Vec2(o.x, o.y));
			Body body = getWorld().createBody(bd);
			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.friction = 0.7f;
			fd.restitution = 0.05f;
			fd.density = 2.0f;
			body.createFixture(fd);
			bodyAnnotation.put(body, "PIG");
			bodiesToHoldBeforeHit.add(body);
		}
		
		//-----------------------------------------------------------------//
		
		shootingBird = null;
		shootingBirdFix = null;
		float maxY = Float.NEGATIVE_INFINITY;
		List<Fixture> allBirds = new ArrayList<Fixture>();
		
		//create red birds
		objs = objects.get("red_birds");
		for (int i=0; i<objs.size(); ++i)
		{
			//System.out.println("Creating red bird in simulation...");
			o = objs.get(i);
			CircleShape shape = new CircleShape();
			//shape.m_radius = Math.max(o.width/2.0f, o.height/2.0f);
			shape.m_radius = slingHeight*0.21f*0.5f;
			BodyDef bd = new BodyDef();
			bd.type = BodyType.DYNAMIC;
			//bd.position.set(new Vec2(o.x, o.y));
			bd.position.set(new Vec2(0.0f, 0.0f));
			Body body = getWorld().createBody(bd);
			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.friction = 0.3f;
			fd.restitution = 0.43f*restitutionParam;
			fd.density = 6.0f;
			Fixture fix = body.createFixture(fd);
			bodyAnnotation.put(body, "RED_BIRD");
			allBirds.add(fix);
			
			float posY = o.y;
			if (posY > maxY)
			{
				maxY = posY;
				shootingBird = body;
				shootingBirdFix = fix;
			}
		}
		
		//create blue birds
		objs = objects.get("blue_birds");
		for (int i=0; i<objs.size(); ++i)
		{
			//System.out.println("Creating blue bird in simulation...");
			o = objs.get(i);
			CircleShape shape = new CircleShape();
			//shape.m_radius = Math.max(o.width/2.0f, o.height/2.0f);
			shape.m_radius = slingHeight*0.14f*0.5f;
			BodyDef bd = new BodyDef();
			bd.type = BodyType.DYNAMIC;
			//bd.position.set(new Vec2(o.x, o.y));
			bd.position.set(new Vec2(0.0f, 0.0f));
			Body body = getWorld().createBody(bd);
			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.friction = 0.3f;
			fd.restitution = 0.25f*restitutionParam;
			fd.density = 4.5f;
			Fixture fix = body.createFixture(fd);
			bodyAnnotation.put(body, "BLUE_BIRD");
			allBirds.add(fix);
			
			float posY = o.y;
			if (posY > maxY)
			{
				maxY = posY;
				shootingBird = body;
				shootingBirdFix = fix;
			}
		}
		
		//create yellow birds
		objs = objects.get("yellow_birds");
		for (int i=0; i<objs.size(); ++i)
		{
			//System.out.println("Creating yellow bird in simulation...");
			o = objs.get(i);
			CircleShape shape = new CircleShape();
			//shape.m_radius = Math.max(o.width/2.0f, o.height/2.0f);
			shape.m_radius = slingHeight*0.23f*0.5f;
			BodyDef bd = new BodyDef();
			bd.type = BodyType.DYNAMIC;
			//bd.position.set(new Vec2(o.x, o.y));
			bd.position.set(new Vec2(0.0f, 0.0f));
			Body body = getWorld().createBody(bd);
			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.friction = 0.3f;
			fd.restitution = 0.23f*restitutionParam;
			fd.density = 6.0f;
			Fixture fix = body.createFixture(fd);
			bodyAnnotation.put(body, "YELLOW_BIRD");
			allBirds.add(fix);
			
			float posY = o.y;
			if (posY > maxY)
			{
				maxY = posY;
				shootingBird = body;
				shootingBirdFix = fix;
			}
		}
		
		//create white birds
		objs = objects.get("white_birds");
		for (int i=0; i<objs.size(); ++i)
		{
			//System.out.println("Creating white bird in simulation...");
			o = objs.get(i);
			CircleShape shape = new CircleShape();
			//shape.m_radius = Math.max(o.width/2.0f, o.height/2.0f);
			shape.m_radius = slingHeight*0.30f*0.5f;
			BodyDef bd = new BodyDef();
			bd.type = BodyType.DYNAMIC;
			//bd.position.set(new Vec2(o.x, o.y));
			bd.position.set(new Vec2(0.0f, 0.0f));
			Body body = getWorld().createBody(bd);
			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.friction = 0.3f;
			fd.restitution = 0.23f*restitutionParam;
			fd.density = 4.0f;
			Fixture fix = body.createFixture(fd);
			bodyAnnotation.put(body, "WHITE_BIRD");
			allBirds.add(fix);
			
			float posY = o.y;
			if (posY > maxY)
			{
				maxY = posY;
				shootingBird = body;
				shootingBirdFix = fix;
			}
		}
		
		//create black birds
		objs = objects.get("black_birds");
		for (int i=0; i<objs.size(); ++i)
		{
			//System.out.println("Creating black bird in simulation...");
			o = objs.get(i);
			CircleShape shape = new CircleShape();
			//shape.m_radius = Math.max(o.width/2.0f, o.height/2.0f);
			shape.m_radius = slingHeight*0.27f*0.5f;
			//System.out.println("shape.m_radius = " + shape.m_radius);
			BodyDef bd = new BodyDef();
			bd.type = BodyType.DYNAMIC;
			//bd.position.set(new Vec2(o.x, o.y));
			bd.position.set(new Vec2(0.0f, 0.0f));
			Body body = getWorld().createBody(bd);
			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.friction = 0.3f;
			fd.restitution = 0.03f*restitutionParam;
			fd.density = 6.0f;
			Fixture fix = body.createFixture(fd);
			bodyAnnotation.put(body, "BLACK_BIRD");
			allBirds.add(fix);
			
			float posY = o.y;
			if (posY > maxY)
			{
				maxY = posY;
				shootingBird = body;
				shootingBirdFix = fix;
			}
		}
		
		//-----------------------------------------------------------------//
		
		{
			CircleShape shape = new CircleShape();
			shape.m_radius = 0.01f;
			BodyDef bd = new BodyDef();
			bd.type = BodyType.STATIC;
			bd.position.set(new Vec2(0.0f, 0.0f));
			Body body = getWorld().createBody(bd);
			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.friction = 0.7f;
			fd.restitution = 0.4f;
			fd.density = 0.75f;
			fd.isSensor = true;
			body.createFixture(fd);
		}
		
		if (SHOW_TRAJECTORY)
		{
			for (int i=0; i<trajectory.size(); ++i)
			{
				CircleShape shape = new CircleShape();
				shape.m_radius = 0.01f;
				BodyDef bd = new BodyDef();
				bd.type = BodyType.STATIC;
				bd.position.set(trajectory.get(i));
				Body body = getWorld().createBody(bd);
				FixtureDef fd = new FixtureDef();
				fd.shape = shape;
				fd.friction = 0.7f;
				fd.restitution = 0.4f;
				fd.density = 0.75f;
				fd.isSensor = true;
				body.createFixture(fd);
			}
		}
		
		if (shootingBird != null)
		{
			//test, remove all other birds
			for (int i=0; i<allBirds.size(); ++i)
			{
				if (allBirds.get(i).getBody() != shootingBird)
				{
					allBirds.get(i).getBody().destroyFixture(allBirds.get(i));
				}
			}
			
			/*
			shootingBird.m_xf.p.x = referencePoint.x*sizeRatio;
			shootingBird.m_xf.p.y = referencePoint.y*sizeRatio;
			shootingBird.setLinearVelocity(shootDir);
			*/
			//System.out.println("Shooting birds coords: " + shootingBird.m_xf.p.x + " " + shootingBird.m_xf.p.y);
			shootingBird.setBullet(true);
			shootingBird.setLinearVelocity(shootDir.mul(1.01f));
			activeBodies.add(shootingBird);
		}
		else
		{
			System.out.println("Shooting bird not found... wtf");
		}
		
		startTime = System.currentTimeMillis();
	}

	@Override
	public String getTestName()
	{
		return testName;
	}
	
	static boolean shownMultip = false;
	
	@Override
	public void postSolve(Contact contact, ContactImpulse impulse)
	{
		Body[] bd = new Body[2];
		bd[0] = contact.getFixtureA().getBody();
		bd[1] = contact.getFixtureB().getBody();
		Fixture[] fx = new Fixture[2];
		fx[0] = contact.getFixtureA();
		fx[1] = contact.getFixtureB();
		
		int count = contact.getManifold().pointCount;

		float maxImpulse = 0.0f;
		for (int i = 0; i < count; ++i)
		{
			//maxImpulse = Math.max(maxImpulse, (float)Math.sqrt(impulse.normalImpulses[i]*impulse.normalImpulses[i] + impulse.tangentImpulses[i]*impulse.tangentImpulses[i]));
			maxImpulse = Math.max(maxImpulse, impulse.normalImpulses[i]);
		}
		
		//float multiplier = (pigRadius/21.5f)*1.52f; //= 0.00533f;
		float multiplier = (120.0f/sceneScale)*0.0025f; //(pigRadius/21.5f)*1.7f;
		
		if (!shownMultip)
		{
			//System.out.println("Multiplier: " + multiplier);
			shownMultip = true;
		}
		
		//System.out.println("calculated multiplier1: "+(pigRadius/(21.5f*sceneScale)));
		//System.out.println("calculated multiplier2: "+(1.0f/sceneScale));
		//System.out.println("calculated multiplier3: "+(pigRadius/21.5f));
		
		for (int i=0; i<2; ++i)
		{
			Body b = bd[i];
			Body otherB = bd[1-i];
			Fixture f = fx[i];
			String annot = bodyAnnotation.get(b);
			String otherAnnot = bodyAnnotation.get(otherB);
			float race_damage = 1.0f;
			
			if (annot == "STONE")
			{
				if ((maxImpulse > 0.000001f) && (activeBodies.contains(otherB)))
				{
					bodiesToHoldBeforeHit.remove(b);
					if (!activeBodies.contains(b)) activeBodies.add(b);
					totalCollisions++;
				}
				
				if (otherAnnot == "YELLOW_BIRD") race_damage = 1.0f;
				else if (otherAnnot == "BLUE_BIRD") race_damage = 1.0f;
				else if (otherAnnot == "WHITE_BIRD") race_damage = 1.0f;
				else if (otherAnnot == "BLACK_BIRD") race_damage = 7.0f; //2.5f;
				else if (otherAnnot == "RED_BIRD") race_damage = 1.0f;
				else race_damage = 0.5f;
				
				if (maxImpulse > 250.0f*multiplier/race_damage) //25
				{
					//System.out.println("STONE impulse = "+maxImpulse);
					if (!fixturesToDestroy.contains(f)) fixturesToDestroy.add(f);
					
					if ((otherB == shootingBird) && (bodyAnnotation.get(shootingBird) != "BLACK_BIRD"))
					{
						otherB.m_linearVelocity.mulLocal(0.2f);
						otherB.m_angularVelocity *= 0.2f;
					}
				}
			}
			else if (annot == "ICE")
			{
				if ((maxImpulse > 0.000001f) && (activeBodies.contains(otherB)))
				{
					bodiesToHoldBeforeHit.remove(b);
					if (!activeBodies.contains(b)) activeBodies.add(b);
					totalCollisions++;
				}
				if (otherAnnot == "YELLOW_BIRD") race_damage = 1.2f;
				else if (otherAnnot == "BLUE_BIRD") race_damage = 10.0f; //4.0f;
				else if (otherAnnot == "WHITE_BIRD") race_damage = 0.8f;
				else if (otherAnnot == "BLACK_BIRD") race_damage = 1.0f;
				else if (otherAnnot == "RED_BIRD") race_damage = 1.0f;
				else race_damage = 0.5f;
				
				//if (maxImpulse > 20.0f*multiplier/race_damage) //8
				if (maxImpulse > 80.0f*multiplier/race_damage) //8
				{
					//System.out.println("ICE impulse = "+maxImpulse);
					if (!fixturesToDestroy.contains(f)) fixturesToDestroy.add(f);
					
					if ((otherB == shootingBird) && (bodyAnnotation.get(shootingBird) != "BLUE_BIRD"))
					{
						otherB.m_linearVelocity.mulLocal(0.2f);
						otherB.m_angularVelocity *= 0.2f;
					}
				}
			}
			else if (annot == "WOOD")
			{
				if ((maxImpulse > 0.000001f) && (activeBodies.contains(otherB)))
				{
					bodiesToHoldBeforeHit.remove(b);
					if (!activeBodies.contains(b)) activeBodies.add(b);
					totalCollisions++;
				}
				if (otherAnnot == "YELLOW_BIRD") race_damage = 7.0f; //2.4f;
				else if (otherAnnot == "BLUE_BIRD") race_damage = 1.0f;
				else if (otherAnnot == "WHITE_BIRD") race_damage = 0.8f;
				else if (otherAnnot == "BLACK_BIRD") race_damage = 1.0f;
				else if (otherAnnot == "RED_BIRD") race_damage = 1.0f;
				else race_damage = 0.5f;
				
				//if (maxImpulse > 50.0f*multiplier/race_damage) //15
				if (maxImpulse > 120.0f*multiplier/race_damage)
				{
					//System.out.println("WOOD impulse = "+maxImpulse);
					if (!fixturesToDestroy.contains(f)) fixturesToDestroy.add(f);
					
					if ((otherB == shootingBird) && (bodyAnnotation.get(shootingBird) != "YELLOW_BIRD"))
					{
						otherB.m_linearVelocity.mulLocal(0.2f);
						otherB.m_angularVelocity *= 0.2f;
					}
				}
			}
			else if (annot == "TNT")
			{
				if ((maxImpulse > 0.000001f) && (activeBodies.contains(otherB)))
				{
					bodiesToHoldBeforeHit.remove(b);
					if (!activeBodies.contains(b)) activeBodies.add(b);
					totalCollisions++;
					//System.out.println("PIG impulse = "+maxImpulse);
					
					if (maxImpulse > 9.0f*multiplier/race_damage) //4
					{
						//System.out.println("PIG impulse = "+maxImpulse);
						if (!fixturesToDestroy.contains(f)) fixturesToDestroy.add(f);
					}
				}
			}
			else if (annot == "PIG")
			{
				if ((maxImpulse > 0.000001f) && (activeBodies.contains(otherB)))
				{
					bodiesToHoldBeforeHit.remove(b);
					if (!activeBodies.contains(b)) activeBodies.add(b);
					totalCollisions++;
					//System.out.println("PIG impulse = "+maxImpulse);
				}
				if (maxImpulse > 9.0f*multiplier/race_damage) //4
				{
                                    if (activeBodies.contains(b))
                                    {
					if (!fixturesToDestroy.contains(f)) fixturesToDestroy.add(f);
                                    }
                                    /*
                                    if (otherB == shootingBird)
                                    {
                                            otherB.m_linearVelocity.mulLocal(0.5f);
                                            otherB.m_angularVelocity *= 0.5f;
                                    }
                                    */
				}
			}
			else if (annot == "EGG")
			{
				if (maxImpulse > 3.0f*multiplier/race_damage)
				{
					if (!fixturesToDestroy.contains(f))
					{
						fixturesToDestroy.add(f);
					}
				}
			}
			else if (b == shootingBird)
			{
				if (birdFirstHit)
				{
					birdHit = System.currentTimeMillis();
					birdFirstHit = false;
					
					if (annot == "BLACK_BIRD")
					{
                                            tapTime = (long)((System.currentTimeMillis()-startTime)/finishTimeRatio);
                                            //System.out.println("Reset taptime BB");
					}
                                        else if (annot == "WHITE_BIRD")
                                        {
                                            long testTapTime = (long)((System.currentTimeMillis()-startTime)/finishTimeRatio);
                                            if (tapTime > testTapTime)
                                            {
                                                    tapTime = testTapTime-150;
                                            }
                                        }
					else
					{
                                            long testTapTime = (long)((System.currentTimeMillis()-startTime)/finishTimeRatio);
                                            if (tapTime > testTapTime)
                                            {
                                                    tapTime = testTapTime-200; //former 150
                                            }
					}
					
					if (otherAnnot != "STONE" && otherAnnot != "ICE" && otherAnnot != "WOOD" && otherAnnot != "PIG")
					{
						b.m_linearVelocity.mulLocal(1.02f);
						//b.m_angularVelocity *= 1.02f;
						//System.out.println(testName+": Hit bird and lowered velocity");
					}
				}
			}
		}
	}
	
	private int isAnnotationPair(Body[] bd, String p1, String p2)
	{
		if ((bodyAnnotation.get(bd[0]) == p1) && (bodyAnnotation.get(bd[1]) == p2))
		{
			return 0;
		}
		else if ((bodyAnnotation.get(bd[1]) == p1) && (bodyAnnotation.get(bd[0]) == p2))
		{
			return 1;
		}
		
		return -1;
	}
	
	public synchronized boolean isFinished()
	{
		return finished;
	}
        
        public synchronized void abort()
        {
            finished = true;
        }
	
	@Override
	public void step(TestbedSettings settings)
	{
		if (((birdHit > 0l) && (System.currentTimeMillis()-birdHit > (10000l*finishTimeRatio))) || (System.currentTimeMillis()-startTime > (15000l*finishTimeRatio))) //10 secs since bird hit, or 15secs max, for a normal 60hz rate. depends on rate
		{
			finished = true;
		}
		
		if (finished)
		{
			return;
		}
		
		//special tap behavior
		if ((!tapped) && (tapTime >= 0l) && (System.currentTimeMillis()-startTime >= tapTime*finishTimeRatio))
		{
			//System.out.println("finishTimeRatio = " + finishTimeRatio);
			/*
			if ((bodyAnnotation.get(shootingBird) != "BLACK_BIRD") && (birdHit > 0l))
			{
				//System.out.println("^^COLLISION BEFORE TAPPING: " + tapTime);
				//System.out.println(birdHit-startTime);
				//System.out.println((birdHit-startTime)/finishTimeRatio);
				tapTime = (long)((birdHit-startTime)/finishTimeRatio) - 250l;
				//System.out.println("__COLLISION BEFORE TAPPING: " + tapTime);
			}
			*/
			if (bodyAnnotation.get(shootingBird) == "BLUE_BIRD")
			{
				//System.out.println("Tapping BLUE BIRD");
				double angle = Math.PI/11;
				//create two extra birds
				{
					BodyDef bd = new BodyDef();
					bd.type = BodyType.DYNAMIC;
					
					Vec2 extraPos = new Vec2(shootingBird.getLinearVelocity());
					extraPos.normalize();
					float oldX = extraPos.x;
					float oldY = extraPos.y;
					extraPos.x = -oldY; extraPos.y = oldX; //rotate vector upwards
					
					Vec2 extraVel = new Vec2(shootingBird.getLinearVelocity());
					oldX = extraVel.x;
					oldY = extraVel.y;
					extraVel.x = (float)(oldX*Math.cos(angle) - oldY*Math.sin(angle));
					extraVel.y = (float)(oldX*Math.sin(angle) + oldY*Math.cos(angle));
					
					bd.position.set(shootingBird.getPosition().add(extraPos.mul(shootingBirdFix.m_shape.getRadius()*2.01f)));
					Body body = getWorld().createBody(bd);
					body.setBullet(true);
					body.setLinearVelocity(extraVel);
					activeBodies.add(body);
					FixtureDef fd = new FixtureDef();
					fd.shape = shootingBirdFix.m_shape;
					fd.friction = shootingBirdFix.m_friction;
					fd.restitution = shootingBirdFix.m_restitution;
					fd.density = shootingBirdFix.m_density;
					Fixture fix = body.createFixture(fd);
					bodyAnnotation.put(body, "BLUE_BIRD");
				}
				
				{
					BodyDef bd = new BodyDef();
					bd.type = BodyType.DYNAMIC;
					
					Vec2 extraPos = new Vec2(shootingBird.getLinearVelocity());
					extraPos.normalize();
					float oldX = extraPos.x;
					float oldY = extraPos.y;
					extraPos.x = oldY; extraPos.y = -oldX; //rotate vector downwards
					
					Vec2 extraVel = new Vec2(shootingBird.getLinearVelocity());
					oldX = extraVel.x;
					oldY = extraVel.y;
					extraVel.x = (float)(oldX*Math.cos(-angle) - oldY*Math.sin(-angle));
					extraVel.y = (float)(oldX*Math.sin(-angle) + oldY*Math.cos(-angle));
					
					bd.position.set(shootingBird.getPosition().add(extraPos.mul(shootingBirdFix.m_shape.getRadius()*2.01f)));
					Body body = getWorld().createBody(bd);
					body.setBullet(true);
					body.setLinearVelocity(extraVel);
					activeBodies.add(body);
					FixtureDef fd = new FixtureDef();
					fd.shape = shootingBirdFix.m_shape;
					fd.friction = shootingBirdFix.m_friction;
					fd.restitution = shootingBirdFix.m_restitution;
					fd.density = shootingBirdFix.m_density;
					Fixture fix = body.createFixture(fd);
					bodyAnnotation.put(body, "BLUE_BIRD");
				}
			}
			else if (bodyAnnotation.get(shootingBird) == "YELLOW_BIRD")
			{
				//System.out.println("Tapping YELLOW BIRD");
				
				shootingBird.setLinearVelocity(shootingBird.getLinearVelocity().mul(1.9f));
			}
			else if (bodyAnnotation.get(shootingBird) == "WHITE_BIRD")
			{
				//System.out.println("Tapping WHITE BIRD");
				
				//lay egg
				CircleShape shape = new CircleShape();
				shape.m_radius = slingHeight*0.21f*0.5f;
				BodyDef bd = new BodyDef();
				bd.type = BodyType.DYNAMIC;
				Vec2 extraPos = new Vec2(shootingBird.getPosition());
				extraPos.y -= shootingBirdFix.m_shape.getRadius()*2.01f;
				bd.position.set(extraPos);
				Body body = getWorld().createBody(bd);
				body.setBullet(true);
				body.setLinearVelocity(new Vec2(0.0f, -shootingBird.getLinearVelocity().length()*2.0f));
				activeBodies.add(body);
				FixtureDef fd = new FixtureDef();
				fd.shape = shape;
				fd.friction = 1.0f;
				fd.restitution = 0.0f;
				fd.density = 5.0f;
				Fixture fix = body.createFixture(fd);
				bodyAnnotation.put(body, "EGG");
				
				//fly away
				double angle = Math.PI/3; // 4
				Vec2 extraVel = new Vec2();
				extraVel.x = (float)(Math.cos(angle));
				extraVel.y = (float)(Math.sin(angle));
				extraVel.normalize();
				shootingBird.setLinearVelocity(extraVel.mul(shootingBird.getLinearVelocity().length()*2.0f));
			}
			else if (bodyAnnotation.get(shootingBird) == "BLACK_BIRD" && (birdHit > 0l))
			{
				//System.out.println("Tapping BLACK BIRD");
				
				float maxDist = 1.2f;
				float impulseStrength = 0.4f;
				
				Iterator<Map.Entry<Body, String>> it = bodyAnnotation.entrySet().iterator();

				while (it.hasNext())
				{
					Map.Entry<Body, String> entry = it.next();

					// Remove entry if key is null or equals 0.
					if (bodyAnnotation.get(entry.getKey()) == "STONE" || bodyAnnotation.get(entry.getKey()) == "ICE" || bodyAnnotation.get(entry.getKey()) == "WOOD" || bodyAnnotation.get(entry.getKey()) == "TNT" || bodyAnnotation.get(entry.getKey()) == "PIG")
					{
						Vec2 dir = entry.getKey().getPosition().sub(shootingBird.getPosition());
						
						if (dir.length() <= maxDist)
						{
							float power = 1.0f - dir.length()/maxDist;
							dir.normalize();
							dir.mulLocal(power);
						
							bodiesToHoldBeforeHit.remove(entry.getKey());
							if (!activeBodies.contains(entry.getKey())) activeBodies.add(entry.getKey());
							entry.getKey().applyLinearImpulse(dir.mul(impulseStrength), entry.getKey().getPosition());
						}
					}
				}
				
				//destroy bird
				shootingBird.destroyFixture(shootingBirdFix);
			}
			else
			{
				//System.out.println("Tapping (probably) RED BIRD");
			}
			
			tapped = true;
		}
		
		//System.out.println("Before step");
		super.step(settings);
		
		for (int i=0; i<bodiesToHoldBeforeHit.size(); ++i)
		{
			bodiesToHoldBeforeHit.get(i).m_linearVelocity.setZero();
			bodiesToHoldBeforeHit.get(i).m_angularVelocity = 0.0f;
		}
		
		while (fixturesToDestroy.size() > 0)
		{
			Fixture f = fixturesToDestroy.get(0);
			Body b = f.getBody();
			if (bodyAnnotation.get(b) == "PIG")
			{
				pigsKilled++;
				
				if (timeKilledFirstPig < 0l)
				{
					timeKilledFirstPig = System.currentTimeMillis()-startTime;
				}
			}
			else if (bodyAnnotation.get(b) == "EGG")
			{
				float maxDist = 1.2f;
				float impulseStrength = 0.2f;
				
				Iterator<Map.Entry<Body, String>> it = bodyAnnotation.entrySet().iterator();
				
				while (it.hasNext())
				{
					Map.Entry<Body, String> entry = it.next();
					
					if (bodyAnnotation.get(entry.getKey()) == "STONE" || bodyAnnotation.get(entry.getKey()) == "ICE" || bodyAnnotation.get(entry.getKey()) == "WOOD" || bodyAnnotation.get(entry.getKey()) == "TNT" || bodyAnnotation.get(entry.getKey()) == "PIG")
					{
						Vec2 dir = entry.getKey().getPosition().sub(b.getPosition());
						
						if (dir.length() <= maxDist)
						{
							float power = 1.0f - dir.length()/maxDist;
							dir.normalize();
							dir.mulLocal(power);
						
							bodiesToHoldBeforeHit.remove(entry.getKey());
							if (!activeBodies.contains(entry.getKey())) activeBodies.add(entry.getKey());
							entry.getKey().applyLinearImpulse(dir.mul(impulseStrength), entry.getKey().getPosition());
						}
					}
				}
			}
			else if (bodyAnnotation.get(b) == "TNT")
			{
				float maxDist = 1.2f;
				float impulseStrength = 0.2f;
				
				Iterator<Map.Entry<Body, String>> it = bodyAnnotation.entrySet().iterator();

				while (it.hasNext())
				{
					Map.Entry<Body, String> entry = it.next();
					
					if (bodyAnnotation.get(entry.getKey()) == "STONE" || bodyAnnotation.get(entry.getKey()) == "ICE" || bodyAnnotation.get(entry.getKey()) == "WOOD" || bodyAnnotation.get(entry.getKey()) == "TNT" || bodyAnnotation.get(entry.getKey()) == "PIG")
					{
						Vec2 dir = entry.getKey().getPosition().sub(b.getPosition());
						
						if (dir.length() <= maxDist)
						{
							float power = 1.0f - dir.length()/maxDist;
							dir.normalize();
							dir.mulLocal(power);
						
							bodiesToHoldBeforeHit.remove(entry.getKey());
							if (!activeBodies.contains(entry.getKey())) activeBodies.add(entry.getKey());
							entry.getKey().applyLinearImpulse(dir.mul(impulseStrength), entry.getKey().getPosition());
						}
					}
				}
			}
			else
			{
				//System.out.println(getTestName()+": "+bodyAnnotation.get(b)+" destroyed");
				objectsDestroyed++;
			}
			b.destroyFixture(f);
			fixturesToDestroy.remove(0);
		}
	}
}
