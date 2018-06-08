package PlayingAgent;

import DB.Features;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class LevelPrediction {
    private String level;

    private Features features;
    private HashMap<String, AgentLevelPrediction> agentsPrediction;
    private int currentScore = 0;
    private ArrayList<String> agents;

    public Features getFeatures() {
        return features;
    }

    public LevelPrediction(String level, Features features) {
        this.features = features;
        this.agentsPrediction = new HashMap<>();
        this.level = level;
    }

    public void calculateAgentsDistributions(ArrayList<String> agents) {
        this.agents = agents;
        long maxScore = features.getMaxScore();
        if(features != null) {
            agents.forEach(agent -> {
                double[] scoreBucketDistribution = ScorePredictionModel.getInstance().predict(agent, features);
                TimeDistribution timeDistribution = TimePredictionModel.getInstance().predict(agent, features);
                this.agentsPrediction.put(agent, new AgentLevelPrediction(maxScore, scoreBucketDistribution, timeDistribution));
            });
        }
    }

    public AgentScoreTimeRate getLevelBestAgent(long remainingTime) {
        Comparator<AgentScoreTimeRate> comparator = new AgentScoreTimeRate.AgentScoreTimeRateComparator();
        System.out.println();
        return features != null ?
                this.agentsPrediction.keySet().stream().map(agent ->
                {
                    double scoreTimeRate = this.agentsPrediction.get(agent).getScoreTimeRate(remainingTime, this.currentScore);
                    System.out.println("Score time rate for level " + this.level + " for agent " + agent + " above " +
                            currentScore + " score with " + remainingTime + " remaining time is: " + scoreTimeRate);
                    return new AgentScoreTimeRate(
                            level,
                            agent,
                            scoreTimeRate);
                })
                        .max(comparator).get()
                : new AgentScoreTimeRate(level, agents.get(0), 0);
    }

    public void updateScore(int score) {
        this.currentScore = Math.max(currentScore, score);
    }

    public String getLevel() {
        return level;
    }
}
