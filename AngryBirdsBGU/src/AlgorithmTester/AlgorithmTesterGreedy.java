package AlgorithmTester;

import java.util.HashMap;

import DB.Game;
import MetaAgent.Distribution;
import MetaAgent.Problem;

public class AlgorithmTesterGreedy extends AlgorithmTester {

	protected HashMap<String, HashMap<String, Distribution>> mScoresDistribution;

	public AlgorithmTesterGreedy(Problem pProblem) throws Exception {
		super(pProblem);
		mScoresDistribution = getScoresDistribution();
	}

	@Override
	protected String[] getAgentAndLevel(Game pGame) throws Exception {
		String[] retVal = new String[2];
		getHighestAdditionalUtility(pGame, retVal);
		return retVal;
	}

	@Override
	protected String getName() {
		return "Greedy";
	}

	private int getHighestAdditionalUtility(Game pGame, String[] refChoice) {
		HashMap<String, Long> scores = getLevelsScores(pGame);
		double[] retVal = {0};
		refChoice[0] = pGame.agents.iterator().next();
		refChoice[1] = pGame.levelNames.iterator().next();
		mScoresDistribution.forEach((agent, v) -> {
			v.forEach((level, distribution) -> {
				double exp = distribution.getExpectation(scores.get(level));
				if (exp > retVal[0]) {
					retVal[0] = exp;
					refChoice[0] = agent;
					refChoice[1] = level;
				}
			});
		});
		return (int)retVal[0];
	}
}
