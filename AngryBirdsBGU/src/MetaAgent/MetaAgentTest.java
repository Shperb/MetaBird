package MetaAgent;

import java.util.ArrayList;

public class MetaAgentTest extends MetaAgent {



	public MetaAgentTest(int pTimeConstraint) {
		super(pTimeConstraint);
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
}
