package PlayingAgent;

public interface ScoreTimeRateCalculator {
    double getScoreTimeRate(long remainingTime, int currentScore);

	void updateProbability(int score);
}


