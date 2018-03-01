package AlgorithmTester;

import java.util.HashMap;

import DB.Game;
import Distribution.Distribution;
import MetaAgent.Problem;

public class AlgorithmTesterRandom extends AlgorithmTester {

	public AlgorithmTesterRandom(Problem pProblem,HashMap<String,
			HashMap<String, Distribution>> realScoreDistribution,
			HashMap<String, HashMap<String, Distribution>> realTimeDistribution,
			HashMap<String, HashMap<String, Distribution>> policyScoreDistribution,
			HashMap<String, HashMap<String, Distribution>> policyTimeDistribution) throws Exception {
		super(pProblem,realScoreDistribution,realTimeDistribution,policyScoreDistribution,policyTimeDistribution);
	}

	@Override
	protected String[] getAgentAndLevel(Game pGame,long[] additionalTime) throws Exception {
		String[] retVal = new String[2];
		retVal[0] = pGame.agents.get((int) (Math.random()*pGame.agents.size()));
		retVal[1] = pGame.levelNames.get((int) (Math.random()*pGame.levelNames.size()));
		return retVal;
	}

	@Override
	protected String getName() {
		return "Random" + getNameExtension();
	}

}
