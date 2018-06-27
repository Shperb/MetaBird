package PlayingAgent;

import Distribution.Distribution;

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
	public void updateProbability(int score) {
		distribution.updateProbablity(score);
		
	}


}
