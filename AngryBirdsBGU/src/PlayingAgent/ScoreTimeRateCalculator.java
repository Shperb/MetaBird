package PlayingAgent;

import java.util.Map;

public interface ScoreTimeRateCalculator {
    double getScoreTimeRate(long remainingTime, int currentScore);

	Map<String, Double> updateProbability(int score);

    void setProbability(Map<String, Double> levelProfileProbabilities);

    void reportResult(boolean didPassCurrentScore);
}


