package Distribution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class BinnedDistributionOfDistributions extends Distribution{
	private HashMap<Distribution, Double> mDistributionsProbabilities;
	private double mMaxValue;
	private List<Bin> mBins;
	private final double epsilon =0.0001;
	
	public BinnedDistributionOfDistributions(HashMap<Distribution, Double> distributionsProbabilities,double maxScore){
		mDistributionsProbabilities = distributionsProbabilities;
		mMaxValue = maxScore;
		mBins= new ArrayList<Bin>();
		double prev = 0;
		for (double i = 0; i<=1; i+=0.1){
			mBins.add(new Bin(prev,i));
			prev = i;
		}
	}
	@Override
	public double getLikelihood(Integer pVal) {
		double[] retVal = {0};
		Bin bin = getBin(pVal/mMaxValue);
		//if (Math.abs(bin.getMid()*mMaxValue - pVal)> epsilon){
		//	return 0;
		//}
		for (Entry<Distribution, Double> entry: mDistributionsProbabilities.entrySet()){
			double prob = computeBinProbablityInCDF(entry.getKey().getCDF(),bin,entry.getKey().getMaxValue());
			retVal[0] += prob * entry.getValue();
		}
		return retVal[0];
	}

	private double computeBinProbablityInCDF(SortedMap<Integer, Double> cdf,
			Bin bin, double max) {
		double binProb = 0;
		double prev = 0;
		for (Integer val :cdf.keySet()){
			if (bin.isInBin(val/max)){
				binProb+= cdf.get(val) - prev;
			}
			prev = cdf.get(val);
		}
		return binProb;
	}
	@Override
	public double[] getExpectationBelowValue(long value) {
		double retVal = 0;
		double retProb = 0;
		for (Integer val : getSupport()){
			if (val <= value){
				double likelihood = getLikelihood(val);
				retVal += likelihood*val;
				retProb += likelihood;
			}
		}
		double[] ans = {retVal,retProb};
		return ans;
	}

	@Override
	public double getExpectation(long pSubstract) {
		double retVal = 0;
		for (Integer val : getSupport()){
			retVal += getLikelihood(val)*val;
		}
		return retVal;
	}

	@Override
	public int drawValue() throws Exception {
		/*
		double random = Math.random();
		double sum = 0;
		Iterator<Entry<Distribution, Double>> iter = mDistributionsProbabilities.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Distribution, Double> entry = iter.next();
			sum+= entry.getValue();
			if (sum >= random){
				return (int)(getBin(entry.getKey().drawValue()/entry.getKey().getMaxScore() * getMaxScore()).getMid());
			}
		}
		throw new Exception("Ilegal distribution draw");
		*/
		double random = Math.random();
		double sum = 0;
		for (Integer val : getSupport()){
			sum += getLikelihood(val);
			if (sum >= random){
				return val;
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
				result.add((int) (getBin(value / dis.getMaxValue()).getMid()*getMaxValue()));
			}
		}
		return result;
	}
	private Bin getBin(double num)
	{
		for (Bin bin :mBins){
			if (bin.getStart()<= num && bin.getEnd()>=num){
				return bin;
			}
		}
		return mBins.get(mBins.size()-1);
	}
	@Override
	public void round(int pFactor) {
		for (Distribution dis: mDistributionsProbabilities.keySet()){
			dis.round(pFactor);
		}		
	}

	@Override
	public String distributionType() {
		return "Binned learned distributions";
	}

	@Override
	public double getMaxValue() {
		return mMaxValue;
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
	
	public void updateProbablity(int value){
		
		Bin bin = getBin(value/mMaxValue);
		double mid = bin.getMid();
		double valueProbability = getLikelihood((int)(mid*mMaxValue));
		for (Entry<Distribution, Double> entry : mDistributionsProbabilities.entrySet()){
			double prob = computeBinProbablityInCDF(entry.getKey().getCDF(),bin,entry.getKey().getMaxValue());
			entry.setValue(prob * entry.getValue() / valueProbability);
		}
		
	}

}
