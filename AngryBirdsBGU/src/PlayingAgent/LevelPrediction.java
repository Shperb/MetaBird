package PlayingAgent;

import DB.Features;
import java.io.IOException;
import java.util.*;

import DB.LevelState;
import com.google.gson.JsonSyntaxException;

public abstract class LevelPrediction {
    protected String level;

    protected Features features;
    protected HashMap<String, ScoreTimeRateCalculator> agentsPrediction;

    private int currentScore = 0;

    protected ArrayList<String> agents;
    public Features getFeatures() {
        return features;
    }

    public LevelPrediction(String level, Features features) {
        this.features = features;
        this.agentsPrediction = new HashMap<>();
        this.level = level;
    }


    public abstract void calculateAgentsDistributions(ArrayList<String> agents) throws JsonSyntaxException, IOException;
    
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

    public void updateScore(int score, String agentName, LevelState state) {
        // TODO: It is possible, if the agent did not improve the score, to "punish" his score/time rate fot this level
        this.currentScore = Math.max(currentScore, score);
        Map<String,Double> levelProfileProbabilities= agentsPrediction.get(agentName).updateProbability(score);
        if (levelProfileProbabilities != null && (state == LevelState.won || state == LevelState.lost)) {
            for (ScoreTimeRateCalculator calc : agentsPrediction.values()) {
                calc.setProbability(levelProfileProbabilities);
            }
        }
    }

    public String getLevel() {
        return level;
    }

    public int getCurrentScore() {
        return currentScore;
    }
}
