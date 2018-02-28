package AlgorithmTester;

import DB.Game;
import MetaAgent.Problem;

public class AlgorithmTesterRandom extends AlgorithmTester {

	public AlgorithmTesterRandom(Problem pProblem) throws Exception {
		super(pProblem);
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
		return "Random";
	}

}
