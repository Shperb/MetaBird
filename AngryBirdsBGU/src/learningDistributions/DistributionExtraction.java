package learningDistributions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonSyntaxException;

import DB.DBHandler;
import DB.Data;
import DB.FeaturesData;
import DB.ValueExtractor.ValueExtractor;
import DB.ValueExtractor.ValueExtractorScore;
import DB.ValueExtractor.ValueExtractorTimeTaken;
import MetaAgent.Distribution;

public class DistributionExtraction {
	private double epsilon = 0.0001;
	private HashMap<String,List<Double>> mLevelFeatures;
	private HashMap<String, HashMap<String, ArrayList<Integer>>> mScores;
	private HashMap<String, HashMap<String, ArrayList<Integer>>> mRunTimes;
	private List<String> mAgents;

	public DistributionExtraction(List<String> agents) throws JsonSyntaxException, IOException {
		FeaturesData featuresData = DBHandler.loadFeatures();
		mLevelFeatures = featuresData.getFeaturesAsList();
		mAgents = agents;
		Data data = DBHandler.loadData();
		mScores = getResults(data, new ValueExtractorScore());
		mRunTimes = getResults(data, new ValueExtractorTimeTaken());
		cleanLevelsDistribution(mScores);
		cleanLevelsDistribution(mRunTimes);
		cleanFeatures();
		int x = 5;

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
		HashMap<String, HashMap<String, ArrayList<Integer>>> result = new HashMap<String, HashMap<String,ArrayList<Integer>>>();
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
	public HashMap<String,Double> computeDistanceFromEachLevel(List<Double> features){
		return computeDistanceFromEachLevel(features,mLevelFeatures);
	}

	public HashMap<String,Double> computeDistanceFromEachLevel(List<Double> features,HashMap<String,List<Double>> featureSet){
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
		for (String level : featureSet.keySet()) {
			double currentDistance = computeDistance(featureSet.get(level),features,maxValues);
			double toAdd = 1/(currentDistance+epsilon);
			sumOfDistances += toAdd;
			result.put(level,toAdd);	
		}
		for (String level : result.keySet()) {
			result.put(level,result.get(level) / sumOfDistances);	
		}
		return result;

	}
	
	public HashMap<String,Double> computeDistanceFromEachLevel(String level){
		List<Double> features = mLevelFeatures.get(level);
		HashMap<String,List<Double>> LevelFeatures = new HashMap<String,List<Double>>(mLevelFeatures);
		LevelFeatures.remove(level);
		return (computeDistanceFromEachLevel(features,LevelFeatures));
		

	}

	private Double computeDistance(List<Double> v, List<Double> features, List<Double> maxValues) {
		 double Sum = 0.0;
	        for(int i=0;i<v.size();i++) {
	           Sum = Sum + Math.pow(Math.abs((v.get(i)-features.get(i))/(maxValues.get(i)+epsilon)),2.0);
	        }
	        return Math.sqrt(Sum);
	}
	
	public double evaluateLearnedDistributions(String level,HashMap<String, HashMap<String, ArrayList<Integer>>> mDistribution){
		HashMap<String,Double> distance = computeDistanceFromEachLevel(level);
		HashMap<String, HashMap<String, Distribution>> distribution = getDistribution(mDistribution);
		double score = 0;
		for (String agent : mAgents){
			HashMap<String, Distribution> agentDistribution = distribution.get(agent);
			Distribution trueDistribution = agentDistribution.get(level);
			
		}
		
		return 0;
	}
	
	private HashMap<String, HashMap<String, Distribution>> getDistribution(HashMap<String, HashMap<String, ArrayList<Integer>>> pValues) {
		HashMap<String, HashMap<String, Distribution>> retVal = new HashMap<>();
		mAgents.forEach(agent->{
			retVal.put(agent, new HashMap<>());
			HashMap<String, Distribution> agentDistribution = retVal.get(agent);
			pValues.get(agent).keySet().forEach(level->{
				agentDistribution.put(level, new Distribution());
				Distribution agent_level_ditribution = agentDistribution.get(level);
				pValues.get(agent).get(level).forEach(v->{
					agent_level_ditribution.addTally(v);
				});
			});
		});
		return retVal;
	}

}
