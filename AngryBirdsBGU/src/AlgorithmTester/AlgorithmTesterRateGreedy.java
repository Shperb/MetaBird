package AlgorithmTester;

import java.util.HashMap;

import DB.Game;
import MetaAgent.Distribution;
import MetaAgent.MyLogger;
import MetaAgent.Problem;

public class AlgorithmTesterRateGreedy extends AlgorithmTester {

	protected HashMap<String, HashMap<String, Distribution>> mScoresDistribution;
	protected HashMap<String, HashMap<String, Distribution>> mTimeDistribution;
	protected boolean _isImprovment;


	public AlgorithmTesterRateGreedy(Problem pProblem,boolean isImprovment) throws Exception {
		super(pProblem);
		mScoresDistribution = getScoresDistribution();
		mTimeDistribution = getTimeDistribution();
		_isImprovment = isImprovment;
	}

	@Override
	protected String[] getAgentAndLevel(Game pGame,long[] additionalTime) throws Exception {
		String[] retVal = new String[2];
		getHighestAdditionalUtility(pGame, retVal);
		return retVal;
	}

	@Override
	protected String getName() {
		return _isImprovment? "ImprovedRateGreedy" : "RateGreedy";
	}

	private int getHighestAdditionalUtility(Game pGame, String[] refChoice) {
		HashMap<String, Long> scores = getLevelsScores(pGame);
		double[] retVal = {0};
		refChoice[0] = pGame.agents.iterator().next();
		refChoice[1] = pGame.levelNames.iterator().next();
		mScoresDistribution.forEach((agent, v) -> {
			v.forEach((level, distribution) -> {
				try {
					double[] distribusionOfRematningTime = mTimeDistribution.get(agent).get(level).getExpectationBelowValue(pGame.getTimeLeft());
					long prevScore = _isImprovment?  scores.get(level) :  0;
					double exp = distribution.getExpectation(prevScore) * distribusionOfRematningTime[1] / distribusionOfRematningTime[0];
					if (exp > retVal[0]) {
						retVal[0] = exp;
						refChoice[0] = agent;
						refChoice[1] = level;
					}
				} catch (Exception e) {
					e.printStackTrace();
					MyLogger.log(e);
				}
			});
		});
		return (int)retVal[0];
	}
}
