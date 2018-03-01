package AlgorithmTester;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import DB.Game;
import Distribution.Distribution;
import MetaAgent.Problem;

public class AlgorithmTesterRoundRobinGreedy extends AlgorithmTester {

	protected HashMap<String, HashMap<String, Distribution>> mScoresDistribution;
	protected HashMap<String, List<String>> selectedPairs;
	protected int _position;


	public AlgorithmTesterRoundRobinGreedy(Problem pProblem,HashMap<String,
			HashMap<String, Distribution>> realScoreDistribution,
			HashMap<String, HashMap<String, Distribution>> realTimeDistribution,
			HashMap<String, HashMap<String, Distribution>> policyScoreDistribution,
			HashMap<String, HashMap<String, Distribution>> policyTimeDistribution) throws Exception {
		super(pProblem,realScoreDistribution,realTimeDistribution,policyScoreDistribution,policyTimeDistribution);
		mScoresDistribution = getScoresDistribution();
		selectedPairs = new HashMap<String, List<String>>();
		_position = 0;
	}

	@Override
	protected String[] getAgentAndLevel(Game pGame,long[] additionalTime) throws Exception {
		String[] retVal = new String[2];
		getHighestAdditionalUtility(pGame, retVal);
		return retVal;
	}

	@Override
	protected String getName() {
		return "RoundRobinGreedy" + getNameExtension();
	}

	private int getHighestAdditionalUtility(Game pGame, String[] refChoice) {
		double[] retVal = {-1};
		String levelToPlay = pGame.levelNames.get(_position);
		if (selectedPairs.get(levelToPlay) == null){
			selectedPairs.put(levelToPlay, new ArrayList<String>());
		}
		refChoice[1] = levelToPlay;
		_position = (_position+1) % pGame.levelNames.size();
		for (String agent : pGame.agents){
			HashMap<String, Distribution> agentDistribution = mScoresDistribution.get(agent);
			Distribution dis = agentDistribution.get(levelToPlay);
			double exp = dis.getExpectation();
			if (exp > retVal[0] && !(selectedPairs.get(levelToPlay).contains(agent))) {
				retVal[0] = exp;
				refChoice[0] = agent;	
			}
		}
		selectedPairs.get(levelToPlay).add(refChoice[0]);
		if (selectedPairs.get(levelToPlay).size() == pGame.agents.size()){
			selectedPairs.put(levelToPlay,new ArrayList<String>());
		}
		return (int)retVal[0];
	}
}
