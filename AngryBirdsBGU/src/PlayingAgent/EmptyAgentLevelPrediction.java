package PlayingAgent;

import java.util.Map;

public class EmptyAgentLevelPrediction implements ScoreTimeRateCalculator {

    @Override
    public double getScoreTimeRate(long remainingTime, int currentScore) {
        return 0;
    }

	@Override
	public Map<String, Double> updateProbability(int score) {
        return null;
	}

    @Override
    public void setProbability(Map<String, Double> levelProfileProbabilities) {
    }
}
