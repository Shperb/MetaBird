package featureExtractor.demo;

import MetaAgent.MetaAgent;
import MetaAgent.Proxy;
import external.ClientMessageEncoder;
import featureExtractor.planner.TrajectoryPlanner;
import featureExtractor.utils.ABUtil;
import featureExtractor.vision.ABObject;
import featureExtractor.vision.ABType;
import featureExtractor.vision.Vision;
import featureExtractor.vision.real.shape.Poly;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static MetaAgent.MyLogger.log;


// Feature Extract based on angreBer plan mechanism
public class FeatureExctractor
{
    private final Proxy mProxy;
    private final MetaAgent agent;
    private Rectangle room = null;                            // Rectangle to allocate game scene
    private Rectangle slingshot = new Rectangle();			// help variable for slingshot detection

    private TrajectoryPlanner tp;
    private Settings settings;                                // to set the method's parameters
    private Features features;                                // to manipulate available features
    private double [] half_heights = {24,14,14,14,50};		// used in setLimit(): bird's constant heights

    /*
     * Constructor with specified IP and Team ID
     */
    public FeatureExctractor(MetaAgent agent, Proxy mProxy){
        this.mProxy = mProxy;
        this.agent = agent;
        tp = new TrajectoryPlanner();
        initialization();
    }

    // initialize parameters' files
    private void initialization()
    {
        settings = new Settings();
        features = new Features(settings);
    }


    /*
     * Grow a tree for a particular level
     * @return the grown tree
    */
    public DB.Features growTreeAndReturnFeatures() throws Exception {

        BufferedImage screenshot = this.agent.doScreenShot();
        // process image
        Vision vision = new Vision(screenshot);

        // find the slingshot
        Rectangle sling = vision.findSlingshotMBR();


        ///////////////////////////////////////////////////////////////////////////////////////////
        /*Scene Detection*/
        List<ABObject> objects = new ArrayList<ABObject>();
        List<ABObject> pigs = new ArrayList<ABObject>();
        List<ABObject> PigsObjects = new ArrayList<ABObject>();
        List<ABObject> tnts = new ArrayList<ABObject>();
        List<Poly> hills = new ArrayList<Poly>();
        ABObject mostDistantObj = new ABObject();

        mostDistantObj = ABUtil.SceneDetection(vision, objects, pigs, PigsObjects, tnts, hills);
        int initialpigs = pigs.size();
        /*end of Scene detection*/
        ///////////////////////////////////////////////////////////////////////////////////////////

        // allocate our room to find states of game
        room = ABUtil.findOurRoom(pigs, objects);
        room = new Rectangle(room.x - 8, room.y - 8, room.width + 16, room.height + 16);

        // bird type on sling detection
        ABType bird = ABType.getType(getBirdTypeOnSling(sling).ordinal());
        if (bird == null)
        {
            log("Solve() - Just after getBirdTypeOnSling().....");
        }
        int birdtype = bird.ordinal();

        boolean whitebird = bird.equals(ABType.WhiteBird);
        double limit = setLimit(birdtype);

        //Tree Construction
        Tree tree = new Tree(tp);
        tree.TreeConstruction(room, PigsObjects, room.x, room.y, room.width, pigs, tnts, sling, bird, limit, getBirdsList());

        //Check feasibility of each node
        Feasibility feasibility = new Feasibility(room, tp);
        feasibility.CheckFeasibility(tree, bird, sling, hills, mostDistantObj, limit);

        tree.SetFeatures(room, pigs, tnts, sling, bird, limit);
        // set Phi Matrix for each feasible Node of our Tree
        features.setPhiX(tree);

        // print the constructed Tree
        log(tree.toString(settings.get_type_of_features()));
        DB.Features featuresObject = tree.getFeatures();

        // Garbage Collector
        tree.myfree();

        hills.clear();
        vision = null;

        return featuresObject;
    }

    /*
     * function to get the limit according to bird type
     * used to check feasibility
     */
    private double setLimit(int bird){
        switch(bird){
            case 2:
                return(half_heights[0]);
            case 3:
                return(half_heights[1]);
            case 4:
                return(half_heights[2]);
            case 5:
                return(half_heights[3]);
            case 6:
                return(half_heights[4]);
            case 14:
                return(half_heights[2]);
            default:
                log("Error in RightBird, called by getNodeType...");
                return(-1);
        }
    }


    /*
     * @return the list of the birds. from the sling and back!
     *
     * **/
    public List<ABObject> getBirdsList() throws Exception {
        mProxy.mConnectionToServer.write(ClientMessageEncoder.fullyZoomIn());
        mProxy.mConnectionToServer.read();

        BufferedImage screenshot = agent.doScreenShot();
        Vision vision = new Vision(screenshot);

        mProxy.mConnectionToServer.write(ClientMessageEncoder.fullyZoomOut());
        mProxy.mConnectionToServer.read();


        List<ABObject> birds = vision.findBirdsRealShape();

        if(birds.isEmpty())
            return new ArrayList<ABObject>();

        Collections.sort(birds, new Comparator<Rectangle>(){

            @Override
            public int compare(Rectangle o1, Rectangle o2) {

                return ((Integer)(o2.x)).compareTo((Integer)(o1.x));
            }
        });
        return birds;
    }

    public ABType getBirdTypeOnSling(Rectangle sling) throws Exception {

        if (sling == null) {
            log("getBirdTypeOnSling() returns ABType.Unknown due to non-detected slingshot....");
            return ABType.Unknown;
        } else {
            int birdindex = 0;

            BufferedImage screenshot = this.agent.doScreenShot();
            if (screenshot == null) {
                log("getBirdTypeOnSling - doScreenShot()....");
                return null;
            }

            Vision vision = new Vision(screenshot);

            // find birds before zoom out
            List<ABObject> _birds = vision.findBirdsRealShape();

            if (_birds == null) {
                _birds = vision.findBirdsMBR();
            }

            if (_birds == null) {
                return ABType.Unknown;
            } else {
                if (_birds.isEmpty()) {
                    return ABType.Unknown;
                } else {
                    int birdsminy = 480;
                    boolean flagfindbird = false;

                    for (int i = 0; i < _birds.size(); i++) {
                        if ((Math.abs(_birds.get(i).x - sling.x) < 50) && (_birds.get(i).y < birdsminy)) {
                            birdsminy = _birds.get(i).y;
                            birdindex = i;
                            flagfindbird = true;
                        }
                    }

                    if (flagfindbird) {
                        return _birds.get(birdindex).getType();
                    }
                }
            }
        }
        return ABType.Unknown;
    }

}
