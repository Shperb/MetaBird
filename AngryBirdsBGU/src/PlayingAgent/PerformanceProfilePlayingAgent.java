package PlayingAgent;

import DB.Features;
import MetaAgent.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class PerformanceProfilePlayingAgent extends PlayingAgent {
    protected  DistributionExtraction de;

	public PerformanceProfilePlayingAgent(int pTimeConstraint, String[] pAgents) {
        super(pTimeConstraint, pAgents);
        try {
            de = new DistributionExtraction(new ArrayList<>(Arrays.asList(Constants.LIST_OF_AGENTS.split(","))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	protected void CreateLevelPrediction(Features features, String pLevelName) {
		this.levelPredictions.put(currLevel, new BaysianLevelPrediction(pLevelName, features,de));
	}
	

}
