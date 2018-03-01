package Distribution;

import java.util.Set;

public abstract class Distribution {

	public abstract double getLikelihood(Integer pVal);
	public abstract double[] getExpectationBelowValue(long value);
	public abstract double getExpectation(long pSubstract);
	public abstract int drawValue() throws Exception;
	public abstract Set<Integer> getSupport();
	public abstract void round(int pFactor);
	public abstract String distributionType();
	
	public double getExpectation() {
		return getExpectation(0);
	}	
}
