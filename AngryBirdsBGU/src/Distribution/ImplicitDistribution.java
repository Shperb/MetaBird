package Distribution;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Set;

public class ImplicitDistribution extends Distribution{
	public HashMap<Integer, Integer> mTally = new HashMap<>();
	private double mMaxScore;
	private SortedMap<Integer,Double> mCDF;
	
	public ImplicitDistribution(){
		
	}
	
	public ImplicitDistribution(double maxScore){
		mMaxScore = maxScore;
	}
	
	private int mTotalTally = 0;

	public void addTally(Integer pVal) {
		if (!mTally.containsKey(pVal)) {
			mTally.put(pVal, new Integer(0));
		}
		mTally.put(pVal, mTally.get(pVal) + 1);
		mTotalTally++;
	}

	public double getLikelihood(Integer pVal) {
		if (mTally.containsKey(pVal)) {
			return ((double) mTally.get(pVal)) / mTotalTally;
		} else {
			return 0;
		}
	}

	public int getTotalTally() {
		return mTotalTally;
	}

	public double[] getExpectationAndProbabilityBelowValue(long value) {
		double[] ans = new double[2];
		double[] sum = {0};
		double[] totalBelow = {0};
		for (Entry<Integer,Integer> entry : mTally.entrySet()){
			if (entry.getKey() <= value){
				sum[0] += entry.getKey() * entry.getValue();
				totalBelow[0] += entry.getValue();
			}
		}
		ans[0] =  (totalBelow[0] ==0 )? 0 : sum[0] / totalBelow[0];
		ans[1] = totalBelow[0]/getTotalTally();
		return ans;
	}
	
	public double getExpectation(long pSubstract) {
		double[] sum = { 0 };
		for (Entry<Integer,Integer> entry : mTally.entrySet()){
			sum[0] += entry.getKey() * Math.max(0, entry.getValue() - pSubstract);
		}
		return sum[0] / getTotalTally();
	}

	@Override
	public int drawValue() throws Exception {
		double random = Math.random();
		double sum = 0;
		Iterator<Map.Entry<Integer,Integer>> iter = mTally.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer,Integer> entry = iter.next();
			sum+= (double)entry.getValue()/mTotalTally;
			if (sum >= random){
				return entry.getKey();
			}
		}
		throw new Exception("Ilegal distribution draw");
	}

	@Override
	public Set<Integer> getSupport() {
		return mTally.keySet();
	}

	@Override
	public void round(int pFactor) {
		HashMap<Integer, Integer> newTally = new HashMap<Integer, Integer>();
		for (Entry<Integer,Integer> entry : mTally.entrySet()){
			int newKey = ((int) Math.ceil((double) entry.getKey() / pFactor) * pFactor);
			if (!newTally.containsKey(newKey)){
				newTally.put(newKey, 0);
			}
			newTally.put(newKey, newTally.get(newKey)+entry.getValue());
		}
		mTally = newTally;
	}
	
	@Override
	public String distributionType() {
		return "true distributions";
	}

	@Override
	public double getMaxValue() {
		return mMaxScore;
	}
	
	public SortedMap<Integer,Double> getCDF(){
		if (mCDF == null){
			SortedMap<Integer,Double> distribution = new TreeMap<Integer,Double>();
			for (Entry<Integer, Integer> entry : mTally.entrySet()){
				distribution.put(entry.getKey(), (double)entry.getValue()/mTotalTally);
			}
			double prevProbability = 0;
			for (Entry<Integer,Double> entry : distribution.entrySet()){
				double currentProbability = entry.getValue();
				distribution.put(entry.getKey(),currentProbability+prevProbability);
				prevProbability+= currentProbability;
			}
			mCDF = distribution;
		}
		
		return mCDF;
	}

	@Override
	public Map<String, Double> updateProbablity(int value) {
		return null;
	}
}
