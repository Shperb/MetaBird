package PlayingAgent;

import Utils.DistributionHelper;

public class AgentLevelPrediction {

    private final long maxScore;

    private double[] scoreBucketsDistribution;
    private TimeDistribution predictedTime;

    public AgentLevelPrediction(long maxScore, double[] scoreBucketsDistribution, TimeDistribution predictedTime) {
        this.scoreBucketsDistribution = scoreBucketsDistribution;
        this.predictedTime = predictedTime;
        this.maxScore = maxScore;
    }

    public double[] getScoreBucketsDistribution() {
        return scoreBucketsDistribution;
    }

    public TimeDistribution getPredictedTime() {
        return predictedTime;
    }

    public double getScoreTimeRate(long remainingTime, int currentScore) {
        double probabilityAboveCurrScore = getProbabilityAboveScore(currentScore);
        double expectationAboveScore = getExpectationAboveScore(currentScore, probabilityAboveCurrScore);
        double probabilityBelowRemainingTime = this.predictedTime.getProbabilityBelowValue(remainingTime);
        double scoreTimeRate = ((expectationAboveScore - currentScore) * probabilityAboveCurrScore * probabilityBelowRemainingTime)
                / this.predictedTime.getExpectationBelowValue(remainingTime);
        return scoreTimeRate;
    }

    private double getExpectationAboveScore(int currentScore, double probabilityAboveCurrScore) {
        return this.maxScore *
                DistributionHelper.getExpectationAboveRelativeScore(this.scoreBucketsDistribution, currentScore / (double) this.maxScore, probabilityAboveCurrScore);
    }

    private double getProbabilityAboveScore(int currentScore) {
        return DistributionHelper.getProbabilityAboveRelativeScore(this.scoreBucketsDistribution, currentScore / (double) this.maxScore);
    }
}
