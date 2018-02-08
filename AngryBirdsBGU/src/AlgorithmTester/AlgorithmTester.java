package AlgorithmTester;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import Clock.Clock;
import Clock.ManualClock;
import DB.DBHandler;
import DB.Data;
import DB.Game;
import DB.Level;
import MetaAgent.Constants;
import MetaAgent.Distribution;

public abstract class AlgorithmTester {
	private HashMap<String, HashMap<String, ArrayList<Integer>>> mScores;
	private HashMap<String, HashMap<String, ArrayList<Integer>>> mRunTimes;
	private ManualClock mClock;
	private int mResultsPerPair = 2;
	private int mTimeConstraint;

	protected abstract String[] getAgentAndLevel(Game pGame) throws ParseException;

	public AlgorithmTester(int pTimeConstraint) throws Exception {
		mClock = new ManualClock();
		Clock.setClock(mClock);
		init();
		mTimeConstraint = pTimeConstraint;
	}
	
	public int test(int pRepetitionsCount) throws ParseException {
		long sum = 0;
		for (int i=0; i<pRepetitionsCount; i++) {
			long a = test();
			System.out.println(a);
			sum += a;
		}
		return (int) (sum / pRepetitionsCount);
	}
	
	protected HashMap<String, HashMap<String, Distribution>> getScoresDistribution() {
		return getDistribution(mScores);
	}

	protected HashMap<String, HashMap<String, Distribution>> getTimeDistribution() {
		return getDistribution(mRunTimes);
	}

	protected long test() throws ParseException {
		Game game = new Game("", mTimeConstraint);
		game.agents = getAgentsNames();
		game.levelNames = selectLevels(getLevelsBank(), 4);
		System.out.println("selected levels: " + String.join(",", game.levelNames));
		while (game.getTimeElapsed() < game.timeConstraint) {
			String[] agentAndLevel = getAgentAndLevel(game);
			String agent = agentAndLevel[0];
			String levelName = agentAndLevel[1];
			game.levels.add(new Level(levelName, agent));
			Level level = game.levels.get(game.levels.size() - 1);
			mClock.proceed(getRunTime(agent, levelName) * 1000);
			level.score = getScore(agent, levelName);
			level.setEndTime();
		}
		return game.getScore();
	}
	
	private HashMap<String, HashMap<String, Distribution>> getDistribution(HashMap<String, HashMap<String, ArrayList<Integer>>> pValues) {
		HashMap<String, HashMap<String, Distribution>> retVal = new HashMap<>();
		getAgentsNames().forEach(agent->{
			retVal.put(agent, new HashMap<>());
			HashMap<String, Distribution> agentDistribution = retVal.get(agent);
			getLevelsBank().forEach(level->{
				agentDistribution.put(level, new Distribution());
				Distribution agent_level_ditribution = agentDistribution.get(level);
				pValues.get(agent).get(level).forEach(v->{
					agent_level_ditribution.addTally(v);
				});
			});
		});
		return retVal;
	}
	
	private final ArrayList<String> getAgentsNames() {
		ArrayList<String> retVal = new ArrayList<>();
		retVal.add("planA");
		retVal.add("naive");
		retVal.add("ihsev");
		retVal.add("AngryBER");
		return retVal;
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

	private ArrayList<String> selectLevels(ArrayList<String> levelsBank, int pCnt) {
//		Collections.shuffle(levelsBank);
//		return new ArrayList<>(levelsBank.subList(0, pCnt));
		return new ArrayList<>(Arrays.asList("Level8-1,Levelcherryblossom-4,Level159,Level5-2".split(",")));
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
			getLevelsBank().forEach(level->{
				ArrayList<Integer> resultsOfPair = getResults(pData, agent, level, mResultsPerPair, pValueExtractor);
				if (resultsOfPair.size() == mResultsPerPair) {
					resultsOfAgent.put(level, resultsOfPair);
				}
				else {
					e[0] = new Exception("Found " + resultsOfPair.size() + " results for " + agent + ", " + level + " instead of " + mResultsPerPair);
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

	private ArrayList<String> getLevelsBank() {
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
