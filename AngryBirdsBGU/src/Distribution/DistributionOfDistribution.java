package Distribution;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public class DistributionOfDistribution extends Distribution{
	private HashMap<Distribution, Double> mDistributionsProbabilities;
	private double mMaxScore;
	
	public DistributionOfDistribution(HashMap<Distribution, Double> distributionsProbabilities,double maxScore){
		mDistributionsProbabilities = distributionsProbabilities;
		mMaxScore = maxScore;
	}
	@Override
	public double getLikelihood(Integer pVal) {
		double[] retVal = {0};
		for (Entry<Distribution, Double> entry: mDistributionsProbabilities.entrySet()){
			retVal[0] += entry.getKey().getLikelihood((int)(entry.getKey().getMaxScore()* pVal/getMaxScore())) * entry.getValue();
		}
		return retVal[0];
	}

	@Override
	public double[] getExpectationBelowValue(long value) {
		double[] retVal = {0,0};
		mDistributionsProbabilities.forEach((dist, prob)->{
			retVal[0] += dist.getExpectationBelowValue((int)(value/getMaxScore() * dist.getMaxScore()))[0] * prob;
			retVal[1] += dist.getExpectationBelowValue((int)(value/getMaxScore() * dist.getMaxScore()))[1] * prob;
		});
		return retVal;
	}

	@Override
	public double getExpectation(long pSubstract) {
		double[] retVal = {0};
		for (Entry<Distribution, Double> entry: mDistributionsProbabilities.entrySet()){
			retVal[0] += entry.getKey().getExpectation(pSubstract)/entry.getKey().getMaxScore() * getMaxScore() * entry.getValue();
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
				return (int)(entry.getKey().drawValue()/entry.getKey().getMaxScore() * getMaxScore());
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
				result.add((int) (value / dis.getMaxScore() * getMaxScore()));
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
	public double getMaxScore() {
		return mMaxScore;
	}

}
