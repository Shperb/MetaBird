package Distribution;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class DistributionOfDistributions extends Distribution{
	private HashMap<Distribution, Double> mDistributionsProbabilities;
	private double mMaxScore;
	
	public DistributionOfDistributions(HashMap<Distribution, Double> distributionsProbabilities,double maxScore){
		mDistributionsProbabilities = distributionsProbabilities;
		mMaxScore = maxScore;
	}
	@Override
	public double getLikelihood(Integer pVal) {
		double[] retVal = {0};
		for (Entry<Distribution, Double> entry: mDistributionsProbabilities.entrySet()){
			retVal[0] += entry.getKey().getLikelihood((int)(entry.getKey().getMaxValue()* pVal/getMaxValue())) * entry.getValue();
		}
		return retVal[0];
	}

	@Override
	public double[] getExpectationAndProbabilityBelowValue(long value) {
		double[] retVal = {0,0};
		mDistributionsProbabilities.forEach((dist, prob)->{
			retVal[0] += dist.getExpectationAndProbabilityBelowValue((int)(value/getMaxValue() * dist.getMaxValue()))[0] * prob;
			retVal[1] += dist.getExpectationAndProbabilityBelowValue((int)(value/getMaxValue() * dist.getMaxValue()))[1] * prob;
		});
		return retVal;
	}

	@Override
	public double getExpectation(long pSubstract) {
		double[] retVal = {0};
		for (Entry<Distribution, Double> entry: mDistributionsProbabilities.entrySet()){
			retVal[0] += entry.getKey().getExpectation(pSubstract)/entry.getKey().getMaxValue() * getMaxValue() * entry.getValue();
		}
		return retVal[0];
	}

	@Override
	public int drawValue() throws Exception {
		double random = Math.random();
		double sum = 0;
		Iterator<Entry<Distribution, Double>> iter = mDistributionsProbabilities.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Distribution, Double> entry = iter.next();
			sum+= entry.getValue();
			if (sum >= random){
				return (int)(entry.getKey().drawValue()/entry.getKey().getMaxValue() * getMaxValue());
			}
		}
		throw new Exception("Ilegal distribution draw");
	}

	@Override
	public Set<Integer> getSupport() {
		Set<Integer> result = new HashSet<Integer>();
		for (Distribution dis: mDistributionsProbabilities.keySet()){
			Set<Integer> supp = dis.getSupport();
			for (Integer value : supp){
				result.add((int) (value / dis.getMaxValue() * getMaxValue()));
			}
		}
		return result;
	}

	@Override
	public void round(int pFactor) {
		for (Distribution dis: mDistributionsProbabilities.keySet()){
			dis.round(pFactor);
		}		
	}

	@Override
	public String distributionType() {
		return "learned distributions";
	}

	@Override
	public double getMaxValue() {
		return mMaxScore;
	}
	
	public SortedMap<Integer,Double> getCDF(){
		SortedMap<Integer,Double> distribution = new TreeMap<Integer,Double>();
		for (Entry<Distribution, Double> entry : mDistributionsProbabilities.entrySet()){
			for (Integer value : entry.getKey().getSupport()){
				if (distribution.containsKey(value)){
					distribution.put(value, distribution.get(value)+entry.getValue()*entry.getKey().getLikelihood(value));
				}
				else{
					distribution.put(value, entry.getValue()*entry.getKey().getLikelihood(value));
				}
			}
		}
		double prevProbability = 0;
		for (Entry<Integer,Double> entry : distribution.entrySet()){
			double currentProbability = entry.getValue();
			distribution.put(entry.getKey(),currentProbability+prevProbability);
			prevProbability+= currentProbability;
		}
		return distribution;
	}
	@Override
	public void updateProbablity(int value) {
		// TODO Auto-generated method stub
		
	}

}
