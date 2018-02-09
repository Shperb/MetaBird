package AlgorithmTester;

import java.util.HashMap;

import MetaAgent.Distribution;
import MetaAgent.Problem;

public class AlgorithmTesterDynamicProgrammingBinned extends AlgorithmTesterDynamicProgramming {
	int mTimeRoundFactor;
	int mScoreRoundFactor;

	public AlgorithmTesterDynamicProgrammingBinned(Problem pProblem) throws Exception {
		super(pProblem);
		round(mScoresDistribution, mScoreRoundFactor);
		round(mTimeDistribution, mTimeRoundFactor);
	}

	@Override
	protected String getName() {
		return "Binned Dynamic programming. Time factor " + mTimeRoundFactor + ", Score factor " + mScoreRoundFactor;
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
				retVal.addTally((int)(k /pFactor) * pFactor);
			}
		});
		return retVal;
	}
}
