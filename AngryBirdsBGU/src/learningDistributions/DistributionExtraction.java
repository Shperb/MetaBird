package learningDistributions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.SortedMap;

import com.google.gson.JsonSyntaxException;

import DB.DBHandler;
import DB.Data;
import DB.FeaturesData;
import DB.ValueExtractor.ValueExtractor;
import DB.ValueExtractor.ValueExtractorScore;
import DB.ValueExtractor.ValueExtractorTimeTaken;
import Distribution.Distribution;
import Distribution.DistributionOfDistribution;
import Distribution.ImplicitDistribution;

public class DistributionExtraction {
	protected double epsilon = 0.0001;
	protected HashMap<String,List<Double>> mLevelFeatures;
	protected HashMap<String, HashMap<String, ArrayList<Integer>>> mScores;
	protected HashMap<String, HashMap<String, ArrayList<Integer>>> mRunTimes;
	protected FeaturesData mfeaturesData;
	protected List<String> mAgents;
	protected List<String> mLevels;
	
	public DistributionExtraction(List<String> agents) throws JsonSyntaxException, IOException {
		FeaturesData featuresData = DBHandler.loadFeatures();
		mfeaturesData = featuresData;
		//mfeaturesData.printMaxLevelScores();
		mLevelFeatures = featuresData.getFeaturesAsList();
		mAgents = agents;
		Data data = DBHandler.loadData();
		mScores = getResults(data, new ValueExtractorScore());
		mRunTimes = getResults(data, new ValueExtractorTimeTaken());
		cleanLevelsDistribution(mScores);
		cleanLevelsDistribution(mRunTimes);
		cleanFeatures();
		mLevels = new ArrayList<String>();
		mLevels.addAll(mLevelFeatures.keySet());
	}
	

		
	public HashMap<String, HashMap<String, Distribution>> getRealScoreDistribution(){
		return getDistribution(mScores);
	}
	public HashMap<String, HashMap<String, Distribution>> getRealTimeDistribution(){
		return getDistribution(mRunTimes);
	}
	
	public List<String> getLevels(){
		return mLevels;
	}
	
	public HashMap<String, HashMap<String, Distribution>> getPolicyScoreDistribution(int k){
		return getPolicyDistribution(getRealScoreDistribution(),k);
	}
	
	public HashMap<String, HashMap<String, Distribution>> getPolicyTimeDistribution(int k){
		return getPolicyDistribution(getRealTimeDistribution(),k);
	}
	
	public HashMap<String, HashMap<String, Distribution>> getPolicyScoreDistribution(){
		return getPolicyDistribution(getRealScoreDistribution());
	}

	private HashMap<String, HashMap<String, Distribution>> getPolicyDistribution(
			HashMap<String, HashMap<String, Distribution>> distribution) {
		return getPolicyDistribution(distribution, mLevels.size());
	}



	public HashMap<String, HashMap<String, Distribution>> getPolicyTimeDistribution(){
		return getPolicyDistribution(getRealTimeDistribution());
	}

	private void cleanFeatures() {
		Iterator<Entry<String, List<Double>>> iter = mLevelFeatures.entrySet().iterator();
		while (iter.hasNext()){
		    Map.Entry<String, List<Double>> entry = iter.next();
		    if (!mScores.values().iterator().next().containsKey(entry.getKey())){
		    	iter.remove();
		    }
		}
	}

	private HashMap<String, HashMap<String, ArrayList<Integer>>> getResults(Data pData, ValueExtractor pValueExtractor) {
		HashMap<String, HashMap<String, ArrayList<Integer>>> results = new HashMap<>();
		mAgents.forEach(agent->{
			results.put(agent, new HashMap<>());
			HashMap<String, ArrayList<Integer>> resultsOfAgent = results.get(agent);
			mLevelFeatures.keySet().forEach(level->{
				ArrayList<Integer> resultsOfPair = getResults(pData, agent, level, pValueExtractor);
				resultsOfAgent.put(level, resultsOfPair);
			});
		});		
		return results;
	}

	private ArrayList<Integer> getResults(Data data, String agent, String pLevel, ValueExtractor pExtractor) {
		ArrayList<Integer> retVal = new ArrayList<>();
		data.games.forEach(game->{
			game.levels.forEach(level->{
				if (level.name.equals(pLevel) && level.agent.equals(agent) && level.isFinished()) {
					retVal.add(pExtractor.getValue(level));
				}
			});
		});
		return retVal;
	}
	
	private void cleanLevelsDistribution(HashMap<String, HashMap<String, ArrayList<Integer>>> distribution){
		Iterator<Map.Entry<String, HashMap<String, ArrayList<Integer>>>> iter = distribution.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry<String, HashMap<String, ArrayList<Integer>>> entry = iter.next();
			Iterator<Map.Entry<String, ArrayList<Integer>>> innerIter = entry.getValue().entrySet().iterator();
			while (innerIter.hasNext()) {
			    Map.Entry<String, ArrayList<Integer>> innerEntry = innerIter.next();
			    if (innerEntry.getValue().size() < 5){
			    	innerIter.remove();
			    }
			}
		}
		iter = distribution.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry<String, HashMap<String, ArrayList<Integer>>> entry = iter.next();
		    Iterator<Map.Entry<String, ArrayList<Integer>>> innerIter = entry.getValue().entrySet().iterator();
			while (innerIter.hasNext()) {
			    Map.Entry<String, ArrayList<Integer>> innerEntry = innerIter.next();
				Iterator<Map.Entry<String, HashMap<String, ArrayList<Integer>>>> secondIter = distribution.entrySet().iterator();
				while(secondIter.hasNext()){
				    Map.Entry<String, HashMap<String, ArrayList<Integer>>> secEntry = secondIter.next();
				    if (!secEntry.getValue().containsKey(innerEntry.getKey())){
				    	innerIter.remove();
				    	break;
				    }
			    }
			}
		}
	}
	
	private HashMap<String,Double> computeDistanceFromEachLevel(List<Double> features,HashMap<String,List<Double>> featureSet, int k){
		HashMap<String,Double> result = new HashMap<>();
		double sumOfDistances = 0;
		List<Double> maxValues = new ArrayList<Double>();
		for (String level : featureSet.keySet()) {
			List<Double> lst = featureSet.get(level);
			for (int i = 0;i<lst.size();i++){
				if (maxValues.size() <= i){
					maxValues.add(lst.get(i));
				}
				else{
					if (maxValues.get(i) < lst.get(i)){
						maxValues.set(i, lst.get(i));
					}
				}
			}	
		}
		PriorityQueue<LevelDistance> distancePriotiryQueue =new PriorityQueue<LevelDistance>();
	/*	for (String level : featureSet.keySet()) {
			double currentDistance = computeDistance(featureSet.get(level),features,maxValues);
			distancePriotiryQueue.add(new LevelDistance(currentDistance, level));
			double toAdd = 1/(currentDistance+epsilon);
			sumOfDistances += toAdd;
			result.put(level,toAdd);	
		}*/
		for (String level : featureSet.keySet()) {
			double currentDistance = computeDistance(featureSet.get(level),features,maxValues);
			distancePriotiryQueue.add(new LevelDistance(currentDistance, level));	
		}
		int count = 0;
		LevelDistance pair;
		while (count < k){
			pair = distancePriotiryQueue.poll();
			if (pair == null){
				break;
			}
			double toAdd = 1/(pair.getDistance()+epsilon);
			result.put(pair.getLevel(),toAdd);
			sumOfDistances += toAdd;
			count++;
		}
		
		for (String level : result.keySet()) {
			result.put(level,result.get(level) / sumOfDistances);	
		}
		return result;

	}
	
	public HashMap<String,Double> computeDistanceFromEachLevel(String level,int k){
		List<Double> features = mLevelFeatures.get(level);
		HashMap<String,List<Double>> LevelFeatures = new HashMap<String,List<Double>>(mLevelFeatures);
		LevelFeatures.remove(level);
		return (computeDistanceFromEachLevel(features,LevelFeatures,k));
	}

	private Double computeDistance(List<Double> v, List<Double> features, List<Double> maxValues) {
		 double Sum = 0.0;
	        for(int i=0;i<v.size();i++) {
	           Sum = Sum + Math.pow(Math.abs((v.get(i)-features.get(i))/(maxValues.get(i)+epsilon)),2.0);
	        }
	        return Math.sqrt(Sum);
	}
	
	protected HashMap<String, HashMap<String, Distribution>> getPolicyDistribution(
			HashMap<String, HashMap<String, Distribution>> distributions,int k) {
		HashMap<String, HashMap<String, Distribution>> results = new HashMap<String, HashMap<String,Distribution>>();
		for (String agent : mAgents){
			HashMap<String, Distribution> agentDistribution = distributions.get(agent);
			HashMap<String, Distribution> agentNewDistributions = new HashMap<String, Distribution>();
			for (String level: mLevels){
				HashMap<String,Double> distance = computeDistanceFromEachLevel(level,k);
				HashMap<Distribution,Double> distributionOfDistributions = new HashMap<Distribution, Double>();
				for (String lvl : distance.keySet()){
					
					distributionOfDistributions.put(agentDistribution.get(lvl), distance.get(lvl));
				}
				agentNewDistributions.put(level, new DistributionOfDistribution(distributionOfDistributions,mfeaturesData.computeMaxScoreBasedOnFeatures(level)));
			}
			results.put(agent, agentNewDistributions);
		}
		return results;
	}
		
	private HashMap<String, HashMap<String, Distribution>> getDistribution(HashMap<String, HashMap<String, ArrayList<Integer>>> pValues) {
		HashMap<String, HashMap<String, Distribution>> retVal = new HashMap<>();
		mAgents.forEach(agent->{
			HashMap<String, Distribution> agentMap = new HashMap<String, Distribution>();
			retVal.put(agent, agentMap);
			HashMap<String, Distribution> agentDistribution = retVal.get(agent);
			pValues.get(agent).keySet().forEach(level->{
				ImplicitDistribution dist = new ImplicitDistribution(mfeaturesData.computeMaxScoreBasedOnFeatures(level));
				agentDistribution.put(level, dist);
				pValues.get(agent).get(level).forEach(v->{
					dist.addTally(v);
				});
			});
		});
		return retVal;
	}
	
	public void printKolmogorovDistance(){
		printKolmogorovDistance(getRealScoreDistribution(),getPolicyScoreDistribution());
		printKolmogorovDistance(getRealTimeDistribution(),getPolicyTimeDistribution());
	}
	
	public void printKolmogorovDistance(int k){
		printKolmogorovDistance(getRealScoreDistribution(),getPolicyScoreDistribution(k));
		printKolmogorovDistance(getRealTimeDistribution(),getPolicyTimeDistribution(k));
	}
	
	public double sumKolmogorovDistance(int k){
		double sum = 0;
		sum += sumKolmogorovDistance(getRealScoreDistribution(),getPolicyScoreDistribution(k));
		sum += sumKolmogorovDistance(getRealTimeDistribution(),getPolicyTimeDistribution(k));
		return sum;
	}
	
	public void sumKolmogorovDistanceForAllLevels(){
		for (int i = 1; i <= mLevels.size(); i++){
			System.out.println(i + "\t" + sumKolmogorovDistance(i));
		}
	}
	

	public void printKolmogorovDistanceForAllLevels(){
		for (int i = 1; i <= mLevels.size(); i++){
			System.out.println(i);
			printKolmogorovDistance(getRealScoreDistribution(),getPolicyScoreDistribution(i));
			printKolmogorovDistance(getRealTimeDistribution(),getPolicyTimeDistribution(i));
		}
	}
	

	private void printKolmogorovDistance(
			HashMap<String, HashMap<String, Distribution>> realDistribution,
			HashMap<String, HashMap<String, Distribution>> policyDistribution) {
		for (Entry<String, HashMap<String, Distribution>> agentEntry: realDistribution.entrySet()){
			for (Entry<String, Distribution> levelEntry: agentEntry.getValue().entrySet()){
				SortedMap<Integer, Double> realCDF =levelEntry.getValue().getCDF();
				SortedMap<Integer, Double> policyCDF =policyDistribution.get(agentEntry.getKey()).get(levelEntry.getKey()).getCDF();
				double distance = computeKolmogorovDistanceForSingleLevel(realCDF,policyCDF);
				System.out.println(agentEntry.getKey()+"\t"+ levelEntry.getKey()+ "\t"+distance);
			}
		}
		
	}
	
	private double sumKolmogorovDistance(
			HashMap<String, HashMap<String, Distribution>> realDistribution,
			HashMap<String, HashMap<String, Distribution>> policyDistribution) {
		double sumOfDistances = 0;
		for (Entry<String, HashMap<String, Distribution>> agentEntry: realDistribution.entrySet()){
			for (Entry<String, Distribution> levelEntry: agentEntry.getValue().entrySet()){
				SortedMap<Integer, Double> realCDF =levelEntry.getValue().getCDF();
				SortedMap<Integer, Double> policyCDF =policyDistribution.get(agentEntry.getKey()).get(levelEntry.getKey()).getCDF();
				double distance = computeKolmogorovDistanceForSingleLevel(realCDF,policyCDF);
				sumOfDistances += distance;
			}
		}
		return sumOfDistances;
		
	}



	private double computeKolmogorovDistanceForSingleLevel(
			SortedMap<Integer, Double> realCDF,
			SortedMap<Integer, Double> policyCDF) {
		double currentDistance = 0;
		Iterator<Entry<Integer, Double>> realIter = realCDF.entrySet().iterator();
		Iterator<Entry<Integer, Double>> policyIter = policyCDF.entrySet().iterator();
	/*	if (!(realIter.hasNext() || policyIter.hasNext())){
			return 0;
		}
		if (!(realIter.hasNext() && policyIter.hasNext())){
			return 1;
		}*/
		Entry<Integer, Double> nextReal = realIter.next();
		Entry<Integer, Double> nextPolicy = policyIter.next();
		if (nextReal.getKey()<nextPolicy.getKey()){
			currentDistance = nextReal.getValue();
		}
		else if (nextReal.getKey()>nextPolicy.getKey()){
			currentDistance = nextPolicy.getValue();
		}

		while(realIter.hasNext() && policyIter.hasNext()){
			double currentValue = Math.abs(nextReal.getValue()-nextPolicy.getValue());
			if (nextReal.getKey()<nextPolicy.getKey()){
				nextReal = realIter.next();
			}
			else if (nextReal.getKey()>nextPolicy.getKey()){
				nextPolicy = policyIter.next();
			}
			else{
				Entry<Integer, Double> oldReal = nextReal;
				Entry<Integer, Double> oldPolicy = nextPolicy;
				nextReal = realIter.next();
				nextPolicy = policyIter.next();
				if (nextReal.getKey() > nextPolicy.getKey()){
					currentValue = Math.abs(oldReal.getValue()-nextPolicy.getValue());
				}
				else if (nextReal.getKey() < nextPolicy.getKey()){
					currentValue = Math.abs(oldPolicy.getValue()-nextReal.getValue());
				}
			}
			currentDistance = Math.max(currentDistance, currentValue);
		}
		while(realIter.hasNext()){
			double currentValue = Math.abs(nextReal.getValue()-nextPolicy.getValue());
			nextReal = realIter.next();
			currentDistance = Math.max(currentDistance, currentValue);
		}	
		while(policyIter.hasNext()){
			double currentValue = Math.abs(nextReal.getValue()-nextPolicy.getValue());
			nextPolicy = policyIter.next();
			currentDistance = Math.max(currentDistance, currentValue);
		}

		return currentDistance;
		
		
	}

}
