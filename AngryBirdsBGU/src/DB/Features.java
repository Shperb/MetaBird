package DB;

import java.util.ArrayList;
import java.util.List;

public class Features {
    public long NumBlocks;
    public double targetWidth;
    public double targetHeight;
    public double closestObjDist;
    public double farthestObjDist;
    public double density;
    public long numObjects;
    public long iceObjects;
    public long woodObjects;
    public long stoneObjects;
    public long numPigs;
    public long helmetPigs;
    public long noHelmetPigs;
    public long numBirds;
    public long numRedBirds;
    public long numYellowBirds;
    public long numBlueBirds;
    public long numBlackBirds;
    public long numWhiteBirds;
    public long varietyOfBirds;
    public long feasibleObjects;
    public long feasiblePigs;
    public long roundObjectsNotPigs;
    public long icedTerritory;
    public long woodenTerritory;
    public long stonedTerritory;
    public long averagePigsInBlocks;
    public long blocksWithPigs;
    public long tnts;

    public long getMaxScore() {
        return (numBirds - 1) * 10000 + numPigs * 5300 + (stoneObjects + woodObjects + iceObjects) * 1250;
    }



    public List<Double> getFeatureAsList() {
        ArrayList<Double> lst = new ArrayList<>();

        lst.add((double) numYellowBirds);
        lst.add((double) numRedBirds);
        lst.add((double) stonedTerritory);
        lst.add((double) helmetPigs);
        lst.add((double) farthestObjDist);
        lst.add((double) icedTerritory);
        lst.add((double) numBlueBirds);
        lst.add((double) averagePigsInBlocks);
        lst.add((double) feasiblePigs);
        lst.add((double) woodenTerritory);
        lst.add((double) NumBlocks);
        lst.add((double) noHelmetPigs);
        lst.add((double) feasibleObjects);
        lst.add((double) roundObjectsNotPigs);
        lst.add((double) density);
        lst.add((double) numWhiteBirds);
        lst.add((double) blocksWithPigs);
        lst.add((double) closestObjDist);
        lst.add((double) numObjects);
        lst.add((double) iceObjects);
        lst.add((double) numPigs);
        lst.add((double) numBlackBirds);
        lst.add((double) targetHeight);
        lst.add((double) woodObjects);
        lst.add((double) numBirds);
        lst.add((double) varietyOfBirds);
        lst.add((double) stoneObjects);
        lst.add((double) targetWidth);
        lst.add((double) tnts);

        return lst;
    }

    public static int amountOfFeatuers() {
        return new Features().getFeatureAsList().size();
    }
}
