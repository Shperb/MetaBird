package MetaAgent;

import java.util.HashMap;

public class GameResult {
    long totalScore;
    HashMap<Integer, Integer> levelScores;

    public GameResult(long totalScore, HashMap<Integer, Integer> levelScores) {
        this.totalScore = totalScore;
        this.levelScores = levelScores;
    }

    public long getTotalScore() {
        return totalScore;
    }

    public HashMap<Integer, Integer> getLevelScores() {
        return levelScores;
    }
}
