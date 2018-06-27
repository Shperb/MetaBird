package PlayingAgent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.google.gson.JsonSyntaxException;

import DB.Features;
import Distribution.Distribution;

public class BaysianLevelPrediction extends LevelPrediction {

	public BaysianLevelPrediction(String level, Features features) {
		super(level, features);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void calculateAgentsDistributions(ArrayList<String> agents) throws JsonSyntaxException, IOException {
        if(this.agentsPrediction.isEmpty()) {
            this.agents = agents;
            DistributionExtraction de = new DistributionExtraction(new ArrayList<>(Arrays.asList("planA,naive,AngryBER,ihsev".split(","))));
            if (features != null) {
                long maxScore = features.getMaxScore();
                agents.forEach(agent -> {
                	HashMap<String, HashMap<String, Distribution>> scoreDistribution = de.getPolicyScoreDistribution(de.getLevels().size(),0);
                	HashMap<String, HashMap<String, Distribution>> timeDistribution = de.getPolicyTimeDistribution(de.getLevels().size(),0);
                    
                	this.agentsPrediction.put(agent, new BaysianAgentLevelPrediction(scoreDistribution.get(agent).get(level),timeDistribution.get(agent).get(level),maxScore));
                });
            } else {
                agents.forEach(agent -> {
                    this.agentsPrediction.put(agent, new EmptyAgentLevelPrediction());
                });
            }
        }
	}

}
