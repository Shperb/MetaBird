package Distribution;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DistributionOfDistribution extends Distribution{
	HashMap<Distribution, Double> mDistributionsProbabilities;
	
	public DistributionOfDistribution(HashMap<Distribution, Double> distributionsProbabilities){
		mDistributionsProbabilities = distributionsProbabilities;
	}

	@Override
	public double getLikelihood(Integer pVal) {
		double[] retVal = {0};
		for (Entry<Distribution, Double> entry: mDistributionsProbabilities.entrySet()){
			retVal[0] += entry.getKey().getLikelihood(pVal) * entry.getValue();
		}
		return retVal[0];
	}

	@Override
	public double[] getExpectationBelowValue(long value) {
		double[] retVal = {0,0};
		mDistributionsProbabilities.forEach((dist, prob)->{
			retVal[0] += dist.getExpectationBelowValue(value)[0] * prob;
			retVal[1] += dist.getExpectationBelowValue(value)[1] * prob;
		});
		return retVal;
	}

	@Override
	public double getExpectation(long pSubstract) {
		double[] retVal = {0};
		for (Entry<Distribution, Double> entry: mDistributionsProbabilities.entrySet()){
			retVal[0] += entry.getKey().getExpectation(pSubstract) * entry.getValue();
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
				return entry.getKey().drawValue();
			}
		}
		throw new Exception("Ilegal distribution draw");
	}

	@Override
	public Set<Integer> getSupport() {
		Set<Integer> result = new HashSet<Integer>();
		for (Distribution dis: mDistributionsProbabilities.keySet()){
			result.addAll(dis.getSupport());
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

}
