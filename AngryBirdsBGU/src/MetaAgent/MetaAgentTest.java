package MetaAgent;

import java.util.ArrayList;

public class MetaAgentTest extends MetaAgent {



	public MetaAgentTest(int pTimeConstraint, String[] pAgents) {
		super(pTimeConstraint, pAgents);
	}

	@Override
	protected String getAlgorithmName() {
		return "test";
	}

	@Override
	protected String[] GetNewAgentAndLevel() {
		ArrayList<String> levels = new ArrayList<>(mLevels.keySet());
		String level = levels.get((int) (Math.random() * levels.size()));
		ArrayList<String> agents = getAgentsNames();
		String agent = agents.get((int) (Math.random() * agents.size()));
		return new String[] {agent, level};
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
