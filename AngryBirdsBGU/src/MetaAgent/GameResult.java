package MetaAgent;

import java.util.HashMap;

public class GameResult {
    long totalScore;
    HashMap<String, Integer> levelScores;
    HashMap<String, String> levelsBestAgents;

    public GameResult(long totalScore, HashMap<String, Integer> levelScores, HashMap<String, String> levelsBestAgents) {
        this.totalScore = totalScore;
        this.levelScores = levelScores;
        this.levelsBestAgents = levelsBestAgents;
    }

    public long getTotalScore() {
        return totalScore;
    }

    public HashMap<String, Integer> getLevelScores() {
        return levelScores;
    }

    public HashMap<String, String> getLevelsBestAgents() {
        return levelsBestAgents;
    }
}
