package PlayingAgent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonSyntaxException;

import DB.Features;
import Distribution.Distribution;

public class BaysianLevelPrediction extends LevelPrediction {
    protected DistributionExtraction de;

	public BaysianLevelPrediction(String level, Features features,DistributionExtraction de) {
		super(level, features);
        this.de = de;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void calculateAgentsDistributions(ArrayList<String> agents) throws JsonSyntaxException, IOException {
        if(this.agentsPrediction.isEmpty()) {
            this.agents = agents;
            if (features != null) {
                long maxScore = features.getMaxScore();
                List<Double> listOfFeatures = features.getFeatureAsList();
                HashMap<String, Distribution> scoreDistribution  = de.getDistributionFromFeatures(true, true, listOfFeatures, maxScore);
                HashMap<String, Distribution> timeDistribution  = de.getDistributionFromFeatures(false,false,listOfFeatures,features.numBirds);
                agents.forEach(agent -> {
                	this.agentsPrediction.put(agent, new BaysianAgentLevelPrediction(scoreDistribution.get(agent),timeDistribution.get(agent),maxScore));
                });
            } else {
                agents.forEach(agent -> {
                    this.agentsPrediction.put(agent, new EmptyAgentLevelPrediction());
                });
            }
        }
	}

}
