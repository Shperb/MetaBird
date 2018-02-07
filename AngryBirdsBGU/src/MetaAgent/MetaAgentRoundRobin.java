package MetaAgent;

import java.util.ArrayList;
import java.util.Iterator;

import DB.Game;
import DB.Level;

public class MetaAgentRoundRobin extends MetaAgent {

	ArrayList<Agent> mAgentWhoTriedCurrentLevel = new ArrayList<>();

	public MetaAgentRoundRobin(int pTimeConstraint, String[] pAgents) {
		super(pTimeConstraint, pAgents);
	}

	@Override
	protected String getAlgorithmName() {
		return "round robin";
	}

	@Override
	protected String[] GetNewAgentAndLevel() {
		Iterator<String> gameLevelsIter = mLevels.keySet().iterator();
		while (gameLevelsIter.hasNext()) {
			String level = gameLevelsIter.next();
			ArrayList<String> agents = getAgentsNames();
			agents.removeAll(getPlayedAgents(getGame(), mCurrentLevel));
			if (!agents.isEmpty()) {
				return new String[] {agents.get(0), level}; 
			}
		}
		return new String[] {getAgentsNames().get(0), mLevels.keySet().iterator().next()}; 
	}

	private ArrayList<String> getPlayedAgents(Game pGame, String pLevel) {
		ArrayList<String> retVal = new ArrayList<>();
		Iterator<Level> playedLevelsIter = pGame.levels.iterator();
		while (playedLevelsIter.hasNext()) {
			Level level = playedLevelsIter.next();
			if (level.name.equals(pLevel)) {
				retVal.add(level.agent);
			}
		}
		return retVal;
	}

	@Override
	protected boolean shouldStartNewGame() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean shouldExit() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected ArrayList<String> getLevelsList() {
		// TODO Auto-generated method stub
		return null;
	}
}
