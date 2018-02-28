package AlgorithmTester;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import Clock.Clock;
import Clock.ManualClock;
import DB.DBHandler;
import DB.Data;
import DB.Game;
import DB.Level;
import DB.ValueExtractor.ValueExtractor;
import DB.ValueExtractor.ValueExtractorScore;
import DB.ValueExtractor.ValueExtractorTimeTaken;
import MetaAgent.Constants;
import MetaAgent.Distribution;
import MetaAgent.MyLogger;
import MetaAgent.Problem;

public abstract class AlgorithmTester {
	private HashMap<String, HashMap<String, ArrayList<Integer>>> mScores;
	private HashMap<String, HashMap<String, ArrayList<Integer>>> mRunTimes;
	private ManualClock mClock;
	private int mResultsPerPair = 10;
	private Problem mProblem;

	protected abstract String[] getAgentAndLevel(Game pGame,long[] additionalTime) throws Exception;
	protected abstract String getName();

	public AlgorithmTester(Problem pProblem) throws Exception {
		mClock = new ManualClock();
		Clock.setClock(mClock);
		mProblem = pProblem;
		init();
	}
	
	public String test(int pRepetitionsCount, String[] refAdditionalData) throws Exception {
		long sum = 0;
		double totalTime = 0;
		long[] additionalTime = new long[1];
		double avarageScore = 0;
		double variance = 0;
		additionalTime[0] = 0;
		for (int i=0; i<pRepetitionsCount; i++) {
			long tStart = System.currentTimeMillis();
			long gameScore = test(additionalTime);
			long tEnd = System.currentTimeMillis();
			long tDelta = tEnd - tStart;
			double oldAvarageScore = avarageScore;
			avarageScore = (1.0/(i+1))*(gameScore + i * avarageScore );
			if (i>0){
				variance = (double)(i-1)/i * variance + (1.0/(i+1))*Math.pow(gameScore-oldAvarageScore,2);
			}
			totalTime+= tDelta / 1000.0;
//			System.out.println(mProblem + "\t" + getName() + "\t" + gameScore + "\t" + getAdditionalData());
			sum += gameScore/pRepetitionsCount;
		}
		double additionalTimeInSeconds = additionalTime[0] / 1000.0;
		refAdditionalData[0] = getAdditionalData();
		String toWrite = "\t"+((double)(totalTime-additionalTimeInSeconds)/pRepetitionsCount +additionalTimeInSeconds) +"\t"+ "time" +"\t" + mProblem + "\t" + getName() + "\t" + avarageScore + "\t" + variance + "\t" + sum + "\t" + getAdditionalData();
		System.out.println(toWrite);
		MyLogger.log(toWrite);
		return toWrite;
	}
	
	protected String getAdditionalData() {
		return "";
	}

	protected HashMap<String, Long> getLevelsScores(Game pGame) {
		HashMap<String, Long> retVal = new HashMap<>();
		pGame.levelNames.forEach(level -> {
			retVal.put(level, (long) 0);
		});		
		pGame.levels.forEach(level->{
			retVal.put(level.name, Math.max(retVal.get(level.name), (long) level.score));			
		});
		return retVal;
	};
	
	protected HashMap<String, HashMap<String, Distribution>> getScoresDistribution() {
		return getDistribution(mScores);
	}

	protected HashMap<String, HashMap<String, Distribution>> getTimeDistribution() {
		return getDistribution(mRunTimes);
	}

	protected long changeTime(long time){
		return time;
	}
	
	protected long test(long[] additionalTime) throws Exception {
		Game game = new Game("", mProblem.timeConstraint);
		game.agents = mProblem.agents;
		game.levelNames = mProblem.levels;
		long timePassed = 0;
//		System.out.println("selected levels: " + String.join(",", game.levelNames));
		while (timePassed < game.timeConstraint && game.getTimeElapsed() < game.timeConstraint) {
			String[] agentAndLevel = getAgentAndLevel(game,additionalTime);
			String agent = agentAndLevel[0];
			String levelName = agentAndLevel[1];
			game.levels.add(new Level(levelName, agent));
			Level level = game.levels.get(game.levels.size() - 1);
			int retVal = get(mRunTimes, agent, levelName);
			timePassed += retVal;
			mClock.proceed(changeTime(retVal)*1000);
			level.score = (retVal > game.timeConstraint - timePassed) ? 0 : getScore(agent, levelName);
			level.setEndTime();
		}
		return game.getScore();
	}
	
	private HashMap<String, HashMap<String, Distribution>> getDistribution(HashMap<String, HashMap<String, ArrayList<Integer>>> pValues) {
		HashMap<String, HashMap<String, Distribution>> retVal = new HashMap<>();
		mProblem.agents.forEach(agent->{
			retVal.put(agent, new HashMap<>());
			HashMap<String, Distribution> agentDistribution = retVal.get(agent);
			mProblem.levels.forEach(level->{
				agentDistribution.put(level, new Distribution());
				Distribution agent_level_ditribution = agentDistribution.get(level);
				pValues.get(agent).get(level).forEach(v->{
					agent_level_ditribution.addTally(v);
				});
			});
		});
		return retVal;
	}
	
//	private final ArrayList<String> getAgentsNames() {
//		ArrayList<String> retVal = new ArrayList<>();
//		retVal.add("planA");
//		retVal.add("naive");
//		retVal.add("ihsev");
//		retVal.add("AngryBER");
//		return retVal;
//	}
	
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

//	private ArrayList<String> selectLevels(ArrayList<String> levelsBank, int pCnt) {
////		Collections.shuffle(levelsBank);
////		return new ArrayList<>(levelsBank.subList(0, pCnt));
//		return new ArrayList<>(Arrays.asList("Level8-1,Levelcherryblossom-4,Level159,Level5-2".split(",")));
//	}

	private void init() throws Exception {
		Data data = DBHandler.loadData();
		
		mScores = getResults(data, new ValueExtractorScore());
		mRunTimes = getResults(data, new ValueExtractorTimeTaken());
	}
	
	private HashMap<String, HashMap<String, ArrayList<Integer>>> getResults(Data pData, ValueExtractor pValueExtractor) throws Exception{
		HashMap<String, HashMap<String, ArrayList<Integer>>> results = new HashMap<>();
		Exception e[] = {null};
		mProblem.agents.forEach(agent->{
			results.put(agent, new HashMap<>());
			HashMap<String, ArrayList<Integer>> resultsOfAgent = results.get(agent);
			mProblem.levels.forEach(level->{
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
	
	private ArrayList<Integer> getResults(Data data, String agent, String pLevel, int pResultsPerPair, ValueExtractor pExtractor) {
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

	private ArrayList<String> _getLevelsBank() {
		File folder = new File(Constants.levelsDir);
		File[] listOfFiles = folder.listFiles();
		
		ArrayList<String> retVal = new ArrayList<>();
		for (int i=0; i<20; i++) {
			
			retVal.add(listOfFiles[i*20].toPath().getFileName().toString().replace(".json", ""));
		}

		return retVal;
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
