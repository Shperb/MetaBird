package AlgorithmTester;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import Clock.Clock;
import Clock.ManualClock;
import DB.DBHandler;
import DB.Data;
import DB.Game;
import DB.Level;
import MetaAgent.Constants;

public abstract class AlgorithmTester {
	private HashMap<String, HashMap<String, ArrayList<Integer>>> mScores;
	private HashMap<String, HashMap<String, ArrayList<Integer>>> mRunTimes;
	ManualClock mClock;
	protected Game mGame;
	int mResultsPerPair = 10;
	private int mTimeConstraint;

	protected abstract String[] getAgentAndLevel() throws ParseException;

	public AlgorithmTester(int pTimeConstraint) throws Exception {
		mClock = new ManualClock();
		Clock.setClock(mClock);
		init();
		mTimeConstraint = pTimeConstraint;
	}
	
	public int test(int pRepetitionsCount) throws ParseException {
		long sum = 0;
		for (int i=0; i<pRepetitionsCount; i++) {
			sum += test();
		}
		return (int) (sum / pRepetitionsCount);
	}

	private int getScore(String pAgent, String pLevel) {
		int retVal = get(mScores, pAgent, pLevel);
		return retVal;
	}

	private int getRunTime(String pAgent, String pLevel) {
		int retVal = get(mRunTimes, pAgent, pLevel);
		return retVal;
	}

	private int get(HashMap<String, HashMap<String, ArrayList<Integer>>> pDistribution, String pAgent, String pLevel) {
		ArrayList<Integer> arr = pDistribution.get(pAgent).get(pLevel);
		Integer retVal = arr.get((int) (Math.random() * arr.size()));
		return retVal;
	}

	private long test() throws ParseException {
		mGame = new Game("", mTimeConstraint);
		while (mGame.getTimeElapsed() < mGame.timeConstraint) {
			String[] agentAndLevel = getAgentAndLevel();
			String agent = agentAndLevel[0];
			String levelName = agentAndLevel[1];
			mGame.levels.add(new Level(levelName, agent));
			Level level = mGame.levels.get(mGame.levels.size() - 1);
			mClock.proceed(getRunTime(agent, levelName));
			level.score = getScore(agent, levelName);
			level.setEndTime();
		}
		return mGame.getScore();
	}

	protected ArrayList<String> getAgentsNames() {
		ArrayList<String> retVal = new ArrayList<>();
		retVal.add("planA");
		retVal.add("naive");
		retVal.add("ihsev");
		retVal.add("AngryBER");
		return retVal;
	}

	private void init() throws Exception {
		Data data = DBHandler.loadData();
		
		mScores = getResults(data, new valueExtractorScore());
		mRunTimes = getResults(data, new valueExtractorTimeTaken());
	}
	
	private HashMap<String, HashMap<String, ArrayList<Integer>>> getResults(Data pData, valueExtractor pValueExtractor) throws Exception{
		HashMap<String, HashMap<String, ArrayList<Integer>>> results = new HashMap<>();
		Exception e[] = {null};
		getAgentsNames().forEach(agent->{
			results.put(agent, new HashMap<>());
			HashMap<String, ArrayList<Integer>> resultsOfAgent = results.get(agent);
			getLevelNames().forEach(level->{
				ArrayList<Integer> resultsOfPair = getResults(pData, agent, level, mResultsPerPair, pValueExtractor);
				if (resultsOfPair.size() == mResultsPerPair) {
					resultsOfAgent.put(level, resultsOfPair);
				}
				else {
					e[0] = new Exception("Found " + resultsOfPair + " results for " + agent + ", " + level + " instead of " + mResultsPerPair);
				}
			});
		});		
		if (e[0] != null) {
			throw e[0];
		}
		return results;
	}
	
	private ArrayList<Integer> getResults(Data data, String agent, String pLevel, int pResultsPerPair, valueExtractor pExtractor) {
		ArrayList<Integer> retVal = new ArrayList<>();
		data.games.forEach(game->{
			game.levels.forEach(level->{
				if (level.name.equals(pLevel) && level.agent.equals(agent) && level.isFinished() && retVal.size() < pResultsPerPair) {
					retVal.add(pExtractor.getValue(level));
				}
			});
		});
		return retVal;
	}

	private ArrayList<String> getLevelNames() {
		File folder = new File(Constants.levelsDir);
		File[] listOfFiles = folder.listFiles();
		
		ArrayList<String> retVal = new ArrayList<>();
		for (int i=0; i<20; i++) {
			retVal.add(listOfFiles[i*20].toPath().getFileName().toString().replace(".json", ""));
		}

		return retVal;
	}
	
abstract class valueExtractor {
		abstract int getValue(Level pLevel);		
	}
	
	class valueExtractorScore extends valueExtractor{
		@Override
		int getValue(Level pLevel) {
			return pLevel.score;
		}
	}
	
	class valueExtractorTimeTaken extends valueExtractor{

		@Override
		int getValue(Level pLevel) {
			return pLevel.getTimeTaken(); 
		}		
	}

//	private HashMap<String, HashMap<String, ArrayList<Integer>>> getResultsFromDB(Data pData, valueExtractor pExtractor) throws JsonSyntaxException, IOException {
//		HashMap<String, HashMap<String, ArrayList<Integer>>> data = new HashMap<>();
//		pData.games.forEach(game -> {
//			game.levels.forEach(level -> {
//				if (level.isFinished()) {
//					if (!data.containsKey(level.agent)) {
//						data.put(level.agent, new HashMap<>());
//					}
//					HashMap<String, ArrayList<Integer>> agentResults = data.get(level.agent);
//					if (!agentResults.containsKey(level.name)) {
//						agentResults.put(level.name, new ArrayList<>());
//					}
//					agentResults.get(level.name).add(pExtractor.getValue(level));
//				}
//			});
//		});
//		return data;
//	}
}
