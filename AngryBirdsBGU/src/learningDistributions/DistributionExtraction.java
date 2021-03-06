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
import Distribution.BinnedDistributionOfDistributions;
import Distribution.Distribution;
import Distribution.DistributionOfDistributions;
import Distribution.ImplicitDistribution;

public class DistributionExtraction {
	protected double epsilon = 0.0001;
	protected HashMap<String,List<Double>> mLevelFeatures;
	protected HashMap<String, HashMap<String, ArrayList<Integer>>> mScores;
	protected HashMap<String, HashMap<String, ArrayList<Integer>>> mRunTimes;
	protected FeaturesData mfeaturesData;
	protected List<String> mAgents;
	protected List<String> mLevels;
	private double[] mFeaturesWeights = null;
	
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
	
	public DistributionExtraction(List<String> agents,List<String> levels) throws JsonSyntaxException, IOException {
		this(agents);
		for (String level: levels){
			mLevels.remove(level);
			mLevelFeatures.remove(level);
			for (HashMap<String, ArrayList<Integer>> scoreMap : mScores.values()){
				scoreMap.remove(level);
			}
			for (HashMap<String, ArrayList<Integer>> timeMap : mRunTimes.values()){
				timeMap.remove(level);
			}
		}
		
	}	
	
	public void setFeaturesWeights(double[] pFeaturesWeights) {
		mFeaturesWeights = pFeaturesWeights;
	}
		
	public HashMap<String, HashMap<String, Distribution>> getRealScoreDistribution(){
		return getDistribution(mScores,true);
	}
	public HashMap<String, HashMap<String, Distribution>> getRealTimeDistribution(){
		return getDistribution(mRunTimes,false);
	}
	
	public List<String> getLevels(){
		return mLevels;
	}
	
	public HashMap<String, HashMap<String, Distribution>> getBinnedPolicyScoreDistribution(int k, double currProb){
		return getPolicyDistribution(getRealScoreDistribution(),k,currProb,true,true);
	}
	
	public HashMap<String, HashMap<String, Distribution>> getBinnedPolicyTimeDistribution(int k,double currProb){
		return getPolicyDistribution(getRealTimeDistribution(),k,currProb,true,false);
	}
	
	public HashMap<String, HashMap<String, Distribution>> getPolicyScoreDistribution(int k, double currProb){
		return getPolicyDistribution(getRealScoreDistribution(),k,currProb,false,true);
	}
	
	public HashMap<String, HashMap<String, Distribution>> getPolicyTimeDistribution(int k, double currProb){
		return getPolicyDistribution(getRealTimeDistribution(),k, currProb,false,false);
	}
	
	public HashMap<String, HashMap<String, Distribution>> getPolicyScoreDistribution(){
		return getPolicyDistribution(getRealScoreDistribution(),false,true);
	}
	
	private HashMap<String, HashMap<String, Distribution>> getPolicyDistribution(
			HashMap<String, HashMap<String, Distribution>> distribution,boolean isBinned,boolean isScore) {
		return getPolicyDistribution(distribution, mLevels.size(),0,isBinned,isScore);
	}
	
	public HashMap<String, HashMap<String, Distribution>> getBinnedPolicyScoreDistribution(){
		return getPolicyDistribution(getRealScoreDistribution(),true,true);
	}

	public HashMap<String, HashMap<String, Distribution>> getBinnedPolicyTimeDistribution(){
		return getPolicyDistribution(getRealTimeDistribution(),true,false);
	}


	public HashMap<String, HashMap<String, Distribution>> getPolicyTimeDistribution(){
		return getPolicyDistribution(getRealTimeDistribution(),false,false);
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
	private HashMap<String,Double> computeDistanceFromEachLevel(String currlevel, int k, double currProb){
		return computeDistanceFromEachLevel(currlevel, k, currProb, true);
	}
	
	private List<Double> getMaxFeaturesValues() {
		List<Double> maxValues = new ArrayList<Double>();
		for (String level : mLevelFeatures.keySet()) {
			List<Double> lst = mLevelFeatures.get(level);
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
		return maxValues;		
	}
	
	private HashMap<String,Double> computeDistanceFromEachLevel(String currlevel, int k, double currProb, boolean pNormalize){
		HashMap<String,Double> result = new HashMap<>();
		List<Double> features = mLevelFeatures.get(currlevel);
		double sumOfDistances = 0;
		PriorityQueue<LevelDistance> distancePriotiryQueue =new PriorityQueue<LevelDistance>();
		List<Double> maxValues = getMaxFeaturesValues();
	/*	for (String level : featureSet.keySet()) {
			double currentDistance = computeDistance(featureSet.get(level),features,maxValues);
			distancePriotiryQueue.add(new LevelDistance(currentDistance, level));
			double toAdd = 1/(currentDistance+epsilon);
			sumOfDistances += toAdd;
			result.put(level,toAdd);	
		}*/
		for (String level : mLevelFeatures.keySet()) {
			double currentDistance = computeDistance(mLevelFeatures.get(level),features, maxValues);
			distancePriotiryQueue.add(new LevelDistance(currentDistance, level));	
		}
		int count = 0;
		LevelDistance pair;
		while (count < k){
			pair = distancePriotiryQueue.poll();
			if (pair == null){
				break;
			}
			if (pair.getLevel().equals(currlevel)){
				result.put(currlevel,currProb);
			}
			else{
				double toAdd = 1/(pair.getDistance()+epsilon);
				result.put(pair.getLevel(),toAdd);
				sumOfDistances += toAdd;
				count++;	
			}

		}
		
		if (pNormalize) {
			for (String level : result.keySet()) {
				if (!level.equals(currlevel)){
					result.put(level,result.get(level)*(1-currProb) / sumOfDistances);	
				}
			}
		}
		return result;

	}
	
	public Double computeDistance(String pLevel1, String pLevel2) {
		List<Double> features1 = mLevelFeatures.get(pLevel1);
		List<Double> features2 = mLevelFeatures.get(pLevel2);
		return computeDistance(features1, features2, getMaxFeaturesValues());
	}
	
	private Double computeDistance(List<Double> v, List<Double> features, List<Double> maxValues) {
		 double Sum = 0.0;
	        for(int i=0;i<v.size();i++) {
	        	double weight = (mFeaturesWeights != null) ? mFeaturesWeights[i] : 1;
	           Sum = Sum + weight * Math.pow(Math.abs((v.get(i)-features.get(i))/(maxValues.get(i)+epsilon)),2.0);
	        }
	        return Math.sqrt(Sum);
	}
	
	protected HashMap<String, HashMap<String, Distribution>> getPolicyDistribution(
			HashMap<String, HashMap<String, Distribution>> distributions,int k, double currProb,boolean isBinned,boolean isScore) {
		HashMap<String, HashMap<String, Distribution>> results = new HashMap<String, HashMap<String,Distribution>>();
		for (String agent : mAgents){
			HashMap<String, Distribution> agentDistribution = distributions.get(agent);
			HashMap<String, Distribution> agentNewDistributions = new HashMap<String, Distribution>();
			for (String level: mLevels){
				HashMap<String,Double> distance = computeDistanceFromEachLevel(level,k,currProb);
				HashMap<Distribution,Double> distributionOfDistributions = new HashMap<Distribution, Double>();
				for (String lvl : distance.keySet()){
					
					distributionOfDistributions.put(agentDistribution.get(lvl), distance.get(lvl));
				}
				double maxValue = isScore? mfeaturesData.computeMaxScoreBasedOnFeatures(level) : 300;
				if (isBinned){
					agentNewDistributions.put(level, new BinnedDistributionOfDistributions(distributionOfDistributions,maxValue));

				}
				else{
					agentNewDistributions.put(level, new DistributionOfDistributions(distributionOfDistributions,maxValue));
				}
			}
			results.put(agent, agentNewDistributions);
		}
		return results;
	}
			
	private HashMap<String, HashMap<String, Distribution>> getDistribution(HashMap<String, HashMap<String, ArrayList<Integer>>> pValues,boolean isScore) {
		HashMap<String, HashMap<String, Distribution>> retVal = new HashMap<>();
		for(String agent : mAgents){
			HashMap<String, Distribution> agentMap = new HashMap<String, Distribution>();
			retVal.put(agent, agentMap);
			HashMap<String, Distribution> agentDistribution = retVal.get(agent);
			for (String level : pValues.get(agent).keySet()){
				double maxValue = isScore ? mfeaturesData.computeMaxScoreBasedOnFeatures(level) : 300;
				ImplicitDistribution dist = new ImplicitDistribution(maxValue);
				agentDistribution.put(level, dist);
				for (Integer v : pValues.get(agent).get(level)){
					dist.addTally(v);
				}
			}
		}
		return retVal;
	}

	public void printExpectedMaxDistance(){
		System.out.println("score");
		printExpectedMaxDistance(getRealScoreDistribution());
		System.out.println("time");
		printExpectedMaxDistance(getRealTimeDistribution());
	}
	
	public void printExpectedMaxDistance(HashMap<String, HashMap<String, Distribution>> pDistribution){
		for (String agent : mAgents) {
			for (String level1 : mLevels) {
				for (String level2 : mLevels) {
					if (!level1.equals(level2)) {
						double dist = pDistribution.get(agent).get(level1).getExpectedMaxDistance(pDistribution.get(agent).get(level2));
						System.out.println(agent + '\t' + level1 + "\t" + level2 + "\t" + dist);
					}
				}
			}
		}
	}
	
	public void printKolmogorovDistance(){
		System.out.println("features distances");
		for (String level : mLevels) {
			HashMap<String, Double> dist = computeDistanceFromEachLevel(level, Integer.MAX_VALUE, 0, false);
			for (String level2 : dist.keySet()) {
				if (!level.equals(level2)) {
					System.out.println(level + "\t" + level2 + "\t" + dist.get(level2));
				}
			}
		}
		System.out.println("score");
		printKolmogorovDistance(getRealScoreDistribution());
		System.out.println("time");
		printKolmogorovDistance(getRealTimeDistribution());
	}
	
	public void printKolmogorovDistance(int k,double prob){
		printKolmogorovDistance(getRealScoreDistribution(),getPolicyScoreDistribution(k,prob));
		printKolmogorovDistance(getRealTimeDistribution(),getPolicyTimeDistribution(k,prob));
	}
	
	public double sumKolmogorovDistance(int k,double prob){
		double sum = 0;
		sum += sumKolmogorovDistance(getRealScoreDistribution(),getPolicyScoreDistribution(k,prob));
		sum += sumKolmogorovDistance(getRealTimeDistribution(),getPolicyTimeDistribution(k,prob));
		return sum;
	}
	
	public void sumKolmogorovDistanceForAllLevels(){
		for (int i = 1; i <= mLevels.size(); i++){
			System.out.println(i + "\t" + sumKolmogorovDistance(i,0));
		}
	}
	

	public void printKolmogorovDistanceForAllLevels(){
		for (int i = 1; i <= mLevels.size(); i++){
			System.out.println(i);
			printKolmogorovDistance(getRealScoreDistribution(),getPolicyScoreDistribution(i,0));
			printKolmogorovDistance(getRealTimeDistribution(),getPolicyTimeDistribution(i,0));
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

	public void printKolmogorovDistances(){
		printKolmogorovDistance(getRealScoreDistribution());
		printKolmogorovDistance(getRealTimeDistribution());
	}
	
	private void printKolmogorovDistance(
			HashMap<String, HashMap<String, Distribution>> pDistributions) {
		for (Entry<String, HashMap<String, Distribution>> agentEntry: pDistributions.entrySet()){
			for (Entry<String, Distribution> level1Entry: agentEntry.getValue().entrySet()){
				for (Entry<String, Distribution> level2Entry: agentEntry.getValue().entrySet()){
					if (level1Entry != level2Entry) {
						SortedMap<Integer, Double> CDF1 =level1Entry.getValue().getCDF();
						SortedMap<Integer, Double> CDF2 =level2Entry.getValue().getCDF();
						double distance = computeKolmogorovDistanceForSingleLevel(CDF1,CDF2);
						System.out.println(agentEntry.getKey()+"\t"+ level1Entry.getKey()+"\t"+ level2Entry.getKey()+ "\t"+distance);
					}
				}
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
