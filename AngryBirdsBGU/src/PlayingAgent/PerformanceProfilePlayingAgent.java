package PlayingAgent;

import DB.Features;

public class PerformanceProfilePlayingAgent extends PlayingAgent {

	public PerformanceProfilePlayingAgent(int pTimeConstraint, String[] pAgents) {
		super(pTimeConstraint, pAgents);
	}
	
	protected void CreateLevelPrediction(Features features, String pLevelName) {
		this.levelPredictions.put(currLevel, new BaysianLevelPrediction(pLevelName, features));
	}
	

}
