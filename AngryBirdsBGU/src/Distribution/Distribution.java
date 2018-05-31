package Distribution;

import java.util.Set;
import java.util.SortedMap;

public abstract class Distribution {

	public abstract double getLikelihood(Integer pVal);
	public abstract double[] getExpectationBelowValue(long value);
	public abstract double getExpectation(long pSubstract);
	public abstract int drawValue() throws Exception;
	public abstract Set<Integer> getSupport();
	public abstract void round(int pFactor);
	public abstract String distributionType();
	public abstract double getMaxValue();
	public abstract SortedMap<Integer,Double> getCDF();
	public abstract void updateProbablity(int value);
	
	public double getExpectation() {
		return getExpectation(0);
	}

	public double getExpectedMaxDistance(Distribution distribution) {
		return 0;
	}
}
