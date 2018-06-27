package PlayingAgent;

import java.util.ArrayList;
import java.util.Arrays;


import DB.Features;

public class LearnedLevelPrediction extends LevelPrediction {

	public LearnedLevelPrediction(String level, Features features) {
		super(level, features);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void calculateAgentsDistributions(ArrayList<String> agents)  {
        if(this.agentsPrediction.isEmpty()) {
            this.agents = agents;
            if (features != null) {
                long maxScore = features.getMaxScore();
                agents.forEach(agent -> {
                    double[] scoreBucketDistribution = ScorePredictionModel.getInstance().predict(agent, features);
					System.out.println(String.format("Score distribution for agent %s for level %s is " + Arrays.toString(scoreBucketDistribution), agent, this.level));
                    LearnedTimeDistribution timeDistribution = TimePredictionModel.getInstance().predict(agent, features);
                    this.agentsPrediction.put(agent, new AgentLevelPredictionLearning(maxScore, scoreBucketDistribution, timeDistribution));
                });
            } else {
                agents.forEach(agent -> {
                    this.agentsPrediction.put(agent, new EmptyAgentLevelPrediction());
                });
            }
        }
    }

}
