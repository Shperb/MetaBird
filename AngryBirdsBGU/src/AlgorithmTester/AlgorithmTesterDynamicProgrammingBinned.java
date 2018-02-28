package AlgorithmTester;

import java.util.HashMap;

import DB.Game;
import MetaAgent.Distribution;
import MetaAgent.Problem;

public class AlgorithmTesterDynamicProgrammingBinned extends AlgorithmTesterDynamicProgramming {
	int mScoreRoundFactor = 1;
	int mTimeRoundFactor = 1;

	public AlgorithmTesterDynamicProgrammingBinned(Problem pProblem, int pScoreRoundFactor, int pTimeRoundFactor)
			throws Exception {
		super(pProblem);
		mScoreRoundFactor = pScoreRoundFactor;
		mTimeRoundFactor = pTimeRoundFactor;
		round(mScoresDistribution, mScoreRoundFactor);
		round(mTimeDistribution, mTimeRoundFactor);
	}

	@Override
	protected String getName() {
		return "Binned Dynamic programming. Time factor " + mTimeRoundFactor + ", Score factor " + mScoreRoundFactor;
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
			v.keySet().forEach(k -> {
				v.put(k, round(v.get(k), pFactor));
			});
		});
	}

	private Distribution round(Distribution pDistribution, int pFactor) {
		Distribution retVal = new Distribution();
		pDistribution.mTally.forEach((k, v) -> {
			for (int i = 0; i < v; i++) {
				retVal.addTally((int) Math.ceil((double) k / pFactor) * pFactor);
			}
		});
		return retVal;
	}

	private HashMap<String, Long> round(HashMap<String, Long> pScores) {
		HashMap<String, Long> retVal = new HashMap<>();
		pScores.forEach((k, v) -> {
			retVal.put(k, (long) (Math.ceil((double)v / mScoreRoundFactor)* mScoreRoundFactor)) ;
		});
		return retVal;
	}

}
