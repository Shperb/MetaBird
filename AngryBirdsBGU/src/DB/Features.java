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

	public List<Double> getFeatureAsList() {
		ArrayList<Double> lst = new ArrayList<>();
		lst.add((double) NumBlocks);
		lst.add(targetWidth);
		lst.add(targetHeight);
		lst.add(closestObjDist);
		lst.add(farthestObjDist);

		lst.add(density);
		lst.add((double) numObjects);
		lst.add((double)iceObjects);
		lst.add((double)woodObjects);
		lst.add((double)stoneObjects);
		lst.add((double)numPigs);
		lst.add((double)helmetPigs);
		lst.add((double)noHelmetPigs);

		lst.add((double)numBirds);
		lst.add((double)numRedBirds);
		lst.add((double)numYellowBirds);
		lst.add((double)numBlueBirds);
		lst.add((double)numBlackBirds);
		lst.add((double)numWhiteBirds);
		return lst;
	}

	public static int amountOfFeatuers() {
		return 19;
	}
	
	public double computeMaxScoreBasedOnFeatures(){
		return (numBirds-1)*10000+ numPigs*5200+(stoneObjects+woodObjects+iceObjects)*1250;
	}

    public long getMaxScore() {
		return (numBirds - 1) * 10000 + numPigs * 5300 + (stoneObjects + woodObjects + iceObjects) * 1250;
	}
}
