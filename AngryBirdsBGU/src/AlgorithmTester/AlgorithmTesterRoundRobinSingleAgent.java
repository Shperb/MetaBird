package AlgorithmTester;

import java.util.HashMap;

import DB.Game;
import MetaAgent.Distribution;
import MetaAgent.Problem;

public class AlgorithmTesterRoundRobinSingleAgent extends AlgorithmTester {

	protected HashMap<String, HashMap<String, Distribution>> mScoresDistribution;
	protected String _agent;
	protected int _position;

	public AlgorithmTesterRoundRobinSingleAgent(Problem pProblem,String agent) throws Exception {
		super(pProblem);
		mScoresDistribution = getScoresDistribution();
		_agent = agent;
		_position = 0;
	}

	@Override
	protected String[] getAgentAndLevel(Game pGame,long[] additionalTime) throws Exception {
		String[] retVal = new String[2];
		String levelToPlay = pGame.levelNames.get(_position);
		_position = (_position+1) % pGame.levelNames.size();
		retVal[0] = _agent;
		retVal[1] = levelToPlay;
		return retVal;
	}

	@Override
	protected String getName() {
		return "Single Agent - " + _agent;
	}

}
