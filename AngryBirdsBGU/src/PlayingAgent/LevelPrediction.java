package PlayingAgent;

import DB.Features;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class LevelPrediction {
    private String level;

    private Features features;
    private HashMap<String, AgentLevelPrediction> agentsPrediction;
    private int currentScore = 0;
    public Features getFeatures() {
        return features;
    }

    public LevelPrediction(String level, Features features) {
        this.features = features;
        this.agentsPrediction = new HashMap<>();
    }

    public void calculateAgentsDistributions(ArrayList<String> agents) {
        agents.forEach(agent -> {
            double[] scoreBucketDistribution = ScorePredictionModel.getInstance().predict(agent, features);
            TimeDistribution timeDistribution = TimePredictionModel.getInstance().predict(agent, features);
            this.agentsPrediction.put(agent, new AgentLevelPrediction(features, scoreBucketDistribution, timeDistribution));
        });
    }

    public AgentScoreTimeRate getLevelBestAgent(long remainingTime) {
        Comparator<AgentScoreTimeRate> comparator = new AgentScoreTimeRate.AgentScoreTimeRateComparator();
        return this.agentsPrediction.keySet().stream().map(agent ->
                new AgentScoreTimeRate(
                        level,
                        agent,
                        this.agentsPrediction.get(agent).getScoreTimeRate(remainingTime, this.currentScore)))
                .max(comparator).get();
    }

    public void updateScore(int score) {
        this.currentScore = Math.max(currentScore, score);
    }

    public String getLevel() {
        return level;
    }
}
