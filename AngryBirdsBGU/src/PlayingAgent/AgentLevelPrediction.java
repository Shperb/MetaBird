package PlayingAgent;

import DB.Features;
import Utils.DistributionHelper;

public class AgentLevelPrediction {

    private Features features;

    private double[] scoreBucketsDistribution;
    private TimeDistribution predictedTime;

    public AgentLevelPrediction(Features features, double[] scoreBucketsDistribution, TimeDistribution predictedTime) {
        this.features = features;
        this.scoreBucketsDistribution = scoreBucketsDistribution;
        this.predictedTime = predictedTime;
    }

    public double[] getScoreBucketsDistribution() {
        return scoreBucketsDistribution;
    }

    public TimeDistribution getPredictedTime() {
        return predictedTime;
    }

    public long getScoreExpectation() {
        double expectedScorePercentage = DistributionHelper.getBucketsExpectation(this.scoreBucketsDistribution);
        return (long) (expectedScorePercentage * features.getMaxScore());
    }

    public double getScoreTimeRate(long remainingTime, int currentScore) {
        return
                ((getScoreExpectation() - currentScore)
                        * this.predictedTime.getProbabilityBelowValue(remainingTime))
                        / TimeDistribution.getExpectation();
    }
}
