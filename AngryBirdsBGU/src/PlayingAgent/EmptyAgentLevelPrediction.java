package PlayingAgent;

public class EmptyAgentLevelPrediction implements ScoreTimeRateCalculator {

    @Override
    public double getScoreTimeRate(long remainingTime, int currentScore) {
        return 0;
    }
}
