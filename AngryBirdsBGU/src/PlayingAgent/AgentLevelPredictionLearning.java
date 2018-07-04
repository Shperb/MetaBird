package PlayingAgent;

import Utils.DistributionHelper;

import java.util.Map;

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
    public double getScoreTimeRate(long remainingTime, int currentScore) {
        double probabilityBelowRemainingTime = this.predictedTime.getProbabilityBelowValue(remainingTime);
        double scoreTimeRate = getExpectationTimesProbablityAboveScore(currentScore) * probabilityBelowRemainingTime
                / this.predictedTime.getExpectationBelowValue(remainingTime)
                / Math.pow(1.1, this.numTimesFailed);
        return scoreTimeRate;
    }

	@Override
	public Map<String, Double> updateProbability(int score) {
        return null;
	}

    @Override
    public void setProbability(Map<String, Double> levelProfileProbabilities) {
    }
}
