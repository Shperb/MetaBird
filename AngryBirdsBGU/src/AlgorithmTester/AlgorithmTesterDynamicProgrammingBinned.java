package AlgorithmTester;

import java.util.HashMap;

import DB.Game;
import Distribution.Distribution;
import MetaAgent.Problem;

public class AlgorithmTesterDynamicProgrammingBinned extends AlgorithmTesterDynamicProgramming {
	int mScoreRoundFactor = 1;
	int mTimeRoundFactor = 1;

	public AlgorithmTesterDynamicProgrammingBinned(Problem pProblem,HashMap<String,
			HashMap<String, Distribution>> realScoreDistribution,
			HashMap<String, HashMap<String, Distribution>> realTimeDistribution,
			HashMap<String, HashMap<String, Distribution>> policyScoreDistribution,
			HashMap<String, HashMap<String, Distribution>> policyTimeDistribution,
			int pScoreRoundFactor, int pTimeRoundFactor) throws Exception {
		super(pProblem,realScoreDistribution,realTimeDistribution,policyScoreDistribution,policyTimeDistribution);
		mScoreRoundFactor = pScoreRoundFactor;
		mTimeRoundFactor = pTimeRoundFactor;
		round(mScoresDistribution, mScoreRoundFactor);
		round(mTimeDistribution, mTimeRoundFactor);
	}

	@Override
	protected String getName() {
		return "Binned Dynamic programming. Time factor " + mTimeRoundFactor + ", Score factor " + mScoreRoundFactor  + getNameExtension();
	}

	// @Override
	// protected HashMap<String, Long> getLevelsScores(Game pGame) {
	// HashMap<String, Long> retVal = super.getLevelsScores(pGame);
	// retVal.keySet().forEach(k->{
	// retVal.
	// });
	// return retVal;
	// }
	
	protected long changeTime(long time){
		return (long)(Math.ceil((double)time / mTimeRoundFactor)) * mTimeRoundFactor;
	}

	@Override
	protected long getValue(Game pGame, HashMap<String, Long> pScores, long pTimeLeft, String[] refChoice,
			Object[] refData, int depth,long[] additionalTime,boolean first) throws Exception {
		return super.getValue(pGame, round(pScores), (int) pTimeLeft, refChoice,
				refData, depth,additionalTime,first);
	}

	private void round(HashMap<String, HashMap<String, Distribution>> pDistribution, int pFactor) {
		pDistribution.values().forEach(v -> {
			v.values().forEach(vl -> {
				vl.round(pFactor);
			});
		});
	}

	
	private HashMap<String, Long> round(HashMap<String, Long> pScores) {
		HashMap<String, Long> retVal = new HashMap<>();
		pScores.forEach((k, v) -> {
			retVal.put(k, (long) (Math.ceil((double)v / mScoreRoundFactor)* mScoreRoundFactor)) ;
		});
		return retVal;
	}

}
