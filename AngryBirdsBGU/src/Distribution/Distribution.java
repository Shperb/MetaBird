package Distribution;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import PlayingAgent.TimeDistribution;

public abstract class Distribution implements TimeDistribution{

	public abstract double getLikelihood(Integer pVal);
	public abstract double[] getExpectationAndProbabilityBelowValue(long value);
	public abstract double getExpectation(long pSubstract);
	public abstract int drawValue() throws Exception;
	public abstract Set<Integer> getSupport();
	public abstract void round(int pFactor);
	public abstract String distributionType();
	public abstract double getMaxValue();
	public abstract SortedMap<Integer,Double> getCDF();
	public abstract Map<String, Double> updateProbablity(int value);
	
	public double getExpectation() {
		return getExpectation(0);
	}
	
    public double getProbabilityBelowValue(long timeLeft){
    	return getExpectationAndProbabilityBelowValue(timeLeft)[1];
    }

    public double getExpectationBelowValue(long remainingTime){
    	return getExpectationAndProbabilityBelowValue(remainingTime)[0];
    }

    public  void setProbability(Map<String, Double> levelProfileProbabilities){};
}
