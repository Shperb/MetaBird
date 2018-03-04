package AlgorithmTester;

import java.util.HashMap;

import Clock.Clock;
import Clock.ManualClock;
import DB.Game;
import DB.Level;
import Distribution.Distribution;
import MetaAgent.MyLogger;
import MetaAgent.Problem;

public abstract class AlgorithmTester {
	//private HashMap<String, HashMap<String, ArrayList<Integer>>> mScores;
	//private HashMap<String, HashMap<String, ArrayList<Integer>>> mRunTimes;
	protected HashMap<String, HashMap<String, Distribution>> mRealScoreDistribution;
	protected HashMap<String, HashMap<String, Distribution>> mRealRunTimeDistribution;
	private HashMap<String, HashMap<String, Distribution>> mPolicyScoreDistribution;
	private HashMap<String, HashMap<String, Distribution>> mPolicyRunTimeDistribution;
	private ManualClock mClock;
	private Problem mProblem;

	protected abstract String[] getAgentAndLevel(Game pGame,long[] additionalTime) throws Exception;
	protected abstract String getName();

	public AlgorithmTester(Problem pProblem,HashMap<String,
			HashMap<String, Distribution>> realScoreDistribution,
			HashMap<String, HashMap<String, Distribution>> realTimeDistribution,
			HashMap<String, HashMap<String, Distribution>> policyScoreDistribution,
			HashMap<String, HashMap<String, Distribution>> policyTimeDistribution) throws Exception {
		mClock = new ManualClock();
		Clock.setClock(mClock);
		mProblem = pProblem;
		mRealScoreDistribution = realScoreDistribution;
		mRealRunTimeDistribution = realTimeDistribution;
		mPolicyScoreDistribution = policyScoreDistribution;
		mPolicyRunTimeDistribution = policyTimeDistribution;
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
		String toWrite = "\t"+((double)(totalTime-additionalTimeInSeconds)/pRepetitionsCount +additionalTimeInSeconds) +"\t"+ "time" +"\t" + mProblem + "\t" + getName() + "\t" + avarageScore + "\t" + variance + "\t" + getAdditionalData();
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
		return mPolicyScoreDistribution;
	}

	protected HashMap<String, HashMap<String, Distribution>> getTimeDistribution() {
		return mPolicyRunTimeDistribution;
	}
	
	protected String getNameExtension(){
		return "\t"+mPolicyScoreDistribution.values().iterator().next().values().iterator().next().distributionType();
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
			int retTime = getRunTime (agent, levelName);
			timePassed += retTime;
			mClock.proceed(changeTime(retTime)*1000);
			int retScore = getScore(agent, levelName);
			level.score = retScore;
			level.setEndTime();
			updateScoreProbablity(agent,levelName,retScore);
			updateTimeProbablity(agent,levelName,retTime);
		}
		return game.getScore();
	}
	

	
//	private final ArrayList<String> getAgentsNames() {
//		ArrayList<String> retVal = new ArrayList<>();
//		retVal.add("planA");
//		retVal.add("naive");
//		retVal.add("ihsev");
//		retVal.add("AngryBER");
//		return retVal;
//	}

	protected void updateTimeProbablity(String agent, String levelName,
			int retScore) {
		// TODO Auto-generated method stub
		
	}
	protected void updateScoreProbablity(String agent, String levelName,
			int retScore) {
		// TODO Auto-generated method stub
		
	}
	private int getScore(String pAgent, String pLevel) throws Exception {
		int retVal = get(mRealScoreDistribution, pAgent, pLevel);
		return retVal;
	}

	private int getRunTime(String pAgent, String pLevel) throws Exception {
		int retVal = get(mRealRunTimeDistribution, pAgent, pLevel);
		return retVal;
	}

	private int get(HashMap<String, HashMap<String, Distribution>> pDistribution, String pAgent, String pLevel) throws Exception{
		return pDistribution.get(pAgent).get(pLevel).drawValue();
	}

//	private ArrayList<String> selectLevels(ArrayList<String> levelsBank, int pCnt) {
////		Collections.shuffle(levelsBank);
////		return new ArrayList<>(levelsBank.subList(0, pCnt));
//		return new ArrayList<>(Arrays.asList("Level8-1,Levelcherryblossom-4,Level159,Level5-2".split(",")));
//	}

}

