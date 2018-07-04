package PlayingAgent;

public abstract class AgentLevelPrediction implements ScoreTimeRateCalculator {

    protected TimeDistribution predictedTime;
    protected final long maxScore;
    protected int numTimesFailed = 0;


    public AgentLevelPrediction(TimeDistribution predictedTime, long maxScore) {
        super();
        this.predictedTime = predictedTime;
        this.maxScore = maxScore;
    }

    @Override
    public double getScoreTimeRate(long remainingTime, int currentScore) {
        double probabilityBelowRemainingTime = this.predictedTime.getProbabilityBelowValue(remainingTime);
        double scoreTimeRate = getExpectationTimesProbablityAboveScore(currentScore) * probabilityBelowRemainingTime
                / this.predictedTime.getExpectationBelowValue(remainingTime);
        return scoreTimeRate;
    }

    @Override
    public void reportResult(boolean didPassCurrentScore) {
        this.numTimesFailed = didPassCurrentScore ? 0 : this.numTimesFailed + 1;
    }

    protected abstract double getExpectationTimesProbablityAboveScore(int currentScore);
}
