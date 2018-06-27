package PlayingAgent;

import Utils.DistributionHelper;

public class AgentLevelPredictionLearning extends AgentLevelPrediction {

    private double[] scoreBucketsDistribution;

    public AgentLevelPredictionLearning(long maxScore, double[] scoreBucketsDistribution, LearnedTimeDistribution predictedTime) {
        super(predictedTime,maxScore);
    	this.scoreBucketsDistribution = scoreBucketsDistribution;
    }

    protected double getExpectationAboveScore(int currentScore, double probabilityAboveCurrScore) {
        return maxScore *
                DistributionHelper.getExpectationAboveRelativeScore(this.scoreBucketsDistribution, currentScore / (double) this.maxScore, probabilityAboveCurrScore);
    }

    protected double getProbabilityAboveScore(int currentScore) {
        return DistributionHelper.getProbabilityAboveRelativeScore(this.scoreBucketsDistribution, currentScore / (double) this.maxScore);
    }

	@Override
	protected double getExpectationTimesProbablityAboveScore(int currentScore) {
		double probabilityAboveCurrScore = getProbabilityAboveScore(currentScore);
        double expectationAboveCurrentScore = getExpectationAboveScore(currentScore, probabilityAboveCurrScore);
		return ((expectationAboveCurrentScore - currentScore) * probabilityAboveCurrScore);

	}

	@Override
	public void updateProbability(int score) {		
	}
}
