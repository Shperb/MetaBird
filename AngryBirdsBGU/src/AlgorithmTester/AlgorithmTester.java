package AlgorithmTester;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import DB.Game;
import DB.Level;

public abstract class AlgorithmTester {
	private HashMap<String, HashMap<String, ArrayList<Integer>>> mScores;
	private HashMap<String, HashMap<String, ArrayList<Integer>>> mRunTimes;
	
	protected abstract String[] getAgentAndLevel();
			
	private int getScore(String pAgent, String pLevel) {
		int retVal = get(mScores, pAgent, pLevel);
		return retVal;
	}
	
	private int getRunTime(String pAgent, String pLevel) {
		int retVal = get(mRunTimes, pAgent, pLevel);
		return retVal;
	}

	private int get(HashMap<String, HashMap<String, ArrayList<Integer>>> pDistribution, String pAgent, String pLevel) {
		ArrayList<Integer> arr = pDistribution.get(pAgent).get(pLevel);
		Integer retVal = arr.get((int) (Math.random() * arr.size()));
		return retVal;
	}
	
	private long test(int pTimeConstraint) throws ParseException {
		Game game = new Game("", pTimeConstraint);
		while (game.getTimeElapsed() < game.timeConstraint) {
			String[] agentAndLevel = getAgentAndLevel();
			String agent = agentAndLevel[0];
			String levelName = agentAndLevel[1];
			game.levels.add(new Level(levelName, agent));
			Level level = game.levels.get(game.levels.size() - 1);
			level.score = getScore(agent, levelName);
		}
		return game.getScore();
	}
}
