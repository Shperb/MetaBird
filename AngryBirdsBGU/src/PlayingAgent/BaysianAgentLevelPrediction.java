package PlayingAgent;

import Distribution.Distribution;

import java.util.Map;

public class BaysianAgentLevelPrediction extends
		AgentLevelPrediction {
	
	private Distribution distribution;

	public BaysianAgentLevelPrediction(Distribution scoreDistribution,TimeDistribution predictedTime,
			long maxScore) {
		super(predictedTime, maxScore);
		this.distribution = scoreDistribution;
	}

	@Override
	protected double getExpectationTimesProbablityAboveScore(int currentScore) {
		return distribution.getExpectation(currentScore);
	}

	@Override
	public Map<String, Double> updateProbability(int score) {
		return distribution.updateProbablity(score);
	}

    @Override
    public void setProbability(Map<String, Double> levelProfileProbabilities) {
        distribution.setProbability(levelProfileProbabilities);
    }


}
