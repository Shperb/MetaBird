package AlgorithmTester;

import java.util.HashMap;

import DB.Game;
import Distribution.Distribution;
import MetaAgent.Problem;

public class AlgorithmTesterScoreGreedy extends AlgorithmTester {

	protected HashMap<String, HashMap<String, Distribution>> mScoresDistribution;
	protected boolean _isImprovment;

	public AlgorithmTesterScoreGreedy(Problem pProblem,HashMap<String,
			HashMap<String, Distribution>> realScoreDistribution,
			HashMap<String, HashMap<String, Distribution>> realTimeDistribution,
			HashMap<String, HashMap<String, Distribution>> policyScoreDistribution,
			HashMap<String, HashMap<String, Distribution>> policyTimeDistribution,
			boolean isImprovment) throws Exception {
		super(pProblem,realScoreDistribution,realTimeDistribution,policyScoreDistribution,policyTimeDistribution);
		mScoresDistribution = getScoresDistribution();
		_isImprovment = isImprovment;
	}
	
	protected void updateScoreProbablity(String agent, String levelName,
			int value) {
		mScoresDistribution.get(agent).get(levelName).updateProbablity(value);
		
	}

	@Override
	protected String[] getAgentAndLevel(Game pGame,long[] additionalTime) throws Exception {
		String[] retVal = new String[2];
		getHighestAdditionalUtility(pGame, retVal);
		return retVal;
	}

	@Override
	protected String getName() {
		return _isImprovment? "ImprovedScoreGreedy"  + getNameExtension(): "ScoreGreedy" + getNameExtension();
	}

	private int getHighestAdditionalUtility(Game pGame, String[] refChoice) {
		HashMap<String, Long> scores = getLevelsScores(pGame);
		double[] retVal = {0};
		refChoice[0] = pGame.agents.iterator().next();
		refChoice[1] = pGame.levelNames.iterator().next();
		for (String agent : pGame.agents){
			HashMap<String, Distribution> agentDistribution = mScoresDistribution.get(agent);
			for (String level : pGame.levelNames){
				Distribution levelDistribution = agentDistribution.get(level);
				long prevScore = _isImprovment?  scores.get(level) :  0;
				double exp = levelDistribution.getExpectation(prevScore);
				if (exp > retVal[0]) {
					retVal[0] = exp;
					refChoice[0] = agent;
					refChoice[1] = level;
				}
			}
		}
		return (int)retVal[0];
	}
}
