package PlayingAgent;

import DB.Features;
import MetaAgent.*;
import ab.vision.GameStateExtractor;
import external.ClientMessageTable;
import featureExtractor.demo.FeatureExctractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class PlayingAgent extends MetaAgent {
    private final int NUM_LEVELS = 10;
    private HashMap<Integer, LevelPrediction> levelPredictions = new HashMap<>();
    private int currLevel = 0;

    private FeatureExctractor featureExtractor;

    @Override
    protected String getAlgorithmName() {
        return null;
    }

    @Override
    protected String[] GetNewAgentAndLevel() {
        long timeLeft = super.getGame().getTimeLeft();
        Comparator<AgentScoreTimeRate> comparator = new AgentScoreTimeRate.AgentScoreTimeRateComparator();
        return this.levelPredictions.keySet().stream()
                .map(level -> levelPredictions.get(level).getLevelBestAgent(timeLeft))
                .max(comparator)
                .map(l -> new String[] {l.getAgent(), l.getLevel()})
                .get();
    }

    @Override
    protected boolean shouldStartNewGame() {
        return false;
    }

    @Override
    protected boolean shouldExit() {
        return false;
    }

    @Override
    protected ArrayList<String> getLevelsList() {
        return null;
    }

    public PlayingAgent(int pTimeConstraint, String[] pAgents) {
        super(pTimeConstraint, pAgents);
    }

    @Override
    protected void selectLevels() throws Exception {
        if (this.featureExtractor == null) {
            this.featureExtractor = new FeatureExctractor(this, super.mProxy);
        }

        Features features;
        for (int i = 0; i < NUM_LEVELS; i++) {
            String pLevelName = String.valueOf(i);
            super.mLevels.put(pLevelName, i);
            this.loadLevelForFeatureExtraction(++currLevel);
            features = this.featureExtractor.growTreeAndReturnFeatures();
            this.levelPredictions.put(currLevel, new LevelPrediction(pLevelName, features));
        }

        ArrayList<String> agentNames = getAgentsNames();
        this.levelPredictions.forEach(
                (levelNum, levelPrediction) ->
                        levelPrediction.calculateAgentsDistributions(agentNames));

    }

    @Override
    protected void actAfterLevelFinished(String plevelName, int score) {
        int level = Integer.valueOf(plevelName);
        this.levelPredictions.get(level).updateScore(score);
    }

    private void loadLevelForFeatureExtraction(int i) throws IOException, ClientConnectionException {
        byte level = (byte) i;
        byte[] message = {ClientMessageTable.getValue(ClientMessageTable.loadLevel), level};
        mProxy.mConnectionToServer.write(message);
        int loaded = mProxy.mConnectionToServer.read();
        GameStateExtractor.GameState state = getGameState();
        if (state == GameStateExtractor.GameState.PLAYING) {
            if (loaded == 1) {
                MyLogger.log("loaded level " + level);
                System.out.println("loaded level " + level);
            } else {
                MyLogger.log("failed to load level " + level);
                System.err.println("failed to load level " + level);
            }
            mLoadLevelTime = System.currentTimeMillis();
        } else {
            String pLevelName = String.valueOf(i);
            MyLogger.log("for loading level " + pLevelName + ": state = " + state + " instead of " + GameStateExtractor.GameState.PLAYING);
            System.out.println(
                    "for loading level " + pLevelName + ": state = " + state + " instead of " + GameStateExtractor.GameState.PLAYING);
            loadLevelForFeatureExtraction(i);
        }
    }
}
