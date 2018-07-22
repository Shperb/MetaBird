package PlayingAgent;

import Clock.Clock;
import DB.Features;
import DB.LevelState;
import MetaAgent.*;
import ab.vision.GameStateExtractor;
import external.ClientMessageTable;
import featureExtractor.demo.FeatureExctractor;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import com.google.gson.JsonSyntaxException;

public class PlayingAgent extends MetaAgent {
    private final int NUM_LEVELS_TO_EXTRACT = 8;
    private int totalNumOfLevels;

    protected HashMap<Integer, LevelPrediction> levelPredictions = new HashMap<>();
    protected int currLevel = 1;
    private int levelsPlayedSinceFeatureExtraction = 0;
    private int numOfNewLevelsExtracted;

    private FeatureExctractor featureExtractor;

    @Override
    protected String getAlgorithmName() {
        return null;
    }

    @Override
    protected void actAfterLevelFinished(String plevelName, String agentName, int score, LevelState state)  throws JsonSyntaxException, IOException{
        int currScore = this.levelScores.getOrDefault(plevelName, 0);
        if (score >= currScore) {
            this.levelScores.put(plevelName, score);
            this.levelsBestAgent.put(plevelName, agentName);
        }
        int level = Integer.valueOf(plevelName);
        this.levelPredictions.get(level).updateScore(score, agentName,state);

        // Extract features for more levels if needed
        if (currLevel > totalNumOfLevels) {
            return;
        }
        this.levelsPlayedSinceFeatureExtraction++;
        if (this.levelsPlayedSinceFeatureExtraction >= numOfNewLevelsExtracted) {
            this.levelsPlayedSinceFeatureExtraction = 0;
            extractFeaturesForNextLevels(NUM_LEVELS_TO_EXTRACT / 2);
            caculateAgentsLevelDistributions();
        }
    }

    @Override
    protected String[] GetNewAgentAndLevel() {
        long timeLeft = 0;
        try {
            timeLeft = super.getGame().getTimeLeft();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Comparator<AgentScoreTimeRate> comparator = new AgentScoreTimeRate.AgentScoreTimeRateComparator();
        long finalTimeLeft = timeLeft;
        return this.levelPredictions.keySet().stream()
                .map(level -> levelPredictions.get(level).getLevelBestAgent(finalTimeLeft))
                .max(comparator)
                .map(l -> new String[]{l.getAgent(), l.getLevel()})
                .get();
    }

    @Override
    protected boolean shouldStartNewGame() {
        return false;
    }

    @Override
    protected boolean shouldExit() {
        try {
            return getGame().getTimeElapsed() > getTimeConstraint();
        } catch (ParseException e) {
            return true;
        }
    }

    @Override
    protected ArrayList<String> getLevelsList() {
        return null;
    }

    public PlayingAgent(int pTimeConstraint, String[] pAgents) {
        super(pTimeConstraint, pAgents, false);
    }

    @Override
    protected Date selectLevels() throws JsonSyntaxException, IOException {
        byte[] configureResult = configure(Utils.intToByteArray(1000));
        System.out.println("configure: " + Arrays.toString(configureResult));
        mProxy.setConfigureResult(configureResult);
        totalNumOfLevels = configureResult[2];
        totalNumOfLevels = (totalNumOfLevels > 20)? 8 : totalNumOfLevels;
        mTimeConstraint = (configureResult[1] > 0)? configureResult[1]*60 : 30*60;
        getMyScore();// getMyScore waits for "start" button to be clicked on the server window
        Date startTime = Clock.getClock().getDate();
        
        if (this.featureExtractor == null) {
            this.featureExtractor = new FeatureExctractor(this, super.mProxy);
        }

        extractFeaturesForNextLevels(NUM_LEVELS_TO_EXTRACT);
        caculateAgentsLevelDistributions();
        return startTime;
    }

    private void caculateAgentsLevelDistributions() throws JsonSyntaxException, IOException {
        ArrayList<String> agentNames = getAgentsNames();
        for (LevelPrediction levelPrediction : levelPredictions.values()){
        	levelPrediction.calculateAgentsDistributions(agentNames);
        }                      
    }

    private void extractFeaturesForNextLevels(int numLevelsToExtract) {
        System.out.println(String.format("Trying to extract features for %d more levels", numLevelsToExtract));
        Features features;
        this.numOfNewLevelsExtracted = 0;
        int firstLevelForFeatureExtraction = currLevel;
        for (; currLevel < firstLevelForFeatureExtraction + numLevelsToExtract; currLevel++) {
            if (currLevel > totalNumOfLevels) {
                return;
            }
            String pLevelName = String.valueOf(currLevel);
            super.mLevels.put(pLevelName, currLevel);
            try {
                this.loadLevelForFeatureExtraction(currLevel);
                features = this.featureExtractor.growTreeAndReturnFeatures();
                this.numOfNewLevelsExtracted++;
            } catch (Exception e) {
                System.out.println("******************************************************************************");
                System.out.println("Failed to extract features for level " + pLevelName);
                System.out.println("Saving features as null and this level will get the lowest score time rate");
                System.out.println("******************************************************************************");
                features = null;
            }
            CreateLevelPrediction(features, pLevelName);
        }
    }

	protected void CreateLevelPrediction(Features features, String pLevelName) {
		this.levelPredictions.put(currLevel, new LearnedLevelPrediction(pLevelName, features));
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