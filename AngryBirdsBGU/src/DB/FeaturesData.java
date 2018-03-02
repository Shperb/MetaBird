package DB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class FeaturesData {
    public HashMap<String, List<Features>> features = new HashMap<>();
    
    public HashMap<String, List<Double>> getFeaturesAsList(){
    	HashMap<String,List<Double>> results = new HashMap<>();
		features.forEach(
			(k,v) -> {
				List<Double> features= new ArrayList<Double>();
				for (int i = 0; i< Features.amountOfFeatuers();i++) {
					List<Double> featuresForMajority = new ArrayList<Double>();
					for (Features f : v) {
						featuresForMajority.add(f.getFeatureAsList().get(i));
					}
					features.add(majority(featuresForMajority));
				}
				results.put(k, features);
				
			}
		);
		return results;
    }
    
	public double computeMaxScoreBasedOnFeatures(String level){
		double max = 0;
		List<Features> levelFeatures = features.get(level);
		for (Features feature : levelFeatures){
			double currentValue = feature.computeMaxScoreBasedOnFeatures();
			if (currentValue > max){
				max = currentValue;
			}
		}
		return max;
	}
    
	public static <T> T majority (List<T> lst) {
	    Map<T, Integer> map = new HashMap<T, Integer>();
	    for (T obj : lst) {
	    	Integer count = map.get(obj);
	        map.put(obj, count != null ? count+1 : 0);
	    }
	    T popular = Collections.max(map.entrySet(),
	    	    new Comparator<Map.Entry<T, Integer>>() {
	    	    @Override
	    	    public int compare(Entry<T, Integer> o1, Entry<T, Integer> o2) {
	    	        return o1.getValue().compareTo(o2.getValue());
	    	    }
	    	}).getKey();
	    
	    return popular;
	}
	
	public void printMaxLevelScores(){
		for (Entry<String,List<Features>> entry : features.entrySet()){
			System.out.println(entry.getKey() +"\t" + computeMaxScoreBasedOnFeatures(entry.getKey()));
		}
	}
	
}
