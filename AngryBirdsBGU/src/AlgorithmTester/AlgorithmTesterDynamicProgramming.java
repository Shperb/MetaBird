package AlgorithmTester;

import java.util.HashMap;
import java.util.Iterator;

import DB.Game;
import MetaAgent.ChoiceEvaluation;
import MetaAgent.Distribution;
import MetaAgent.Problem;

public  class AlgorithmTesterDynamicProgramming extends AlgorithmTester{

	private boolean mAllowCacheUpdate = true; 
	private HashMap<ChoiceEvaluation, Long> mCache = new HashMap<>();
	protected HashMap<String, HashMap<String, Distribution>> mScoresDistribution;
	protected HashMap<String, HashMap<String, Distribution>> mTimeDistribution;	

	public AlgorithmTesterDynamicProgramming(Problem pProblem) throws Exception {
		super(pProblem);
		mScoresDistribution = getScoresDistribution();
		mTimeDistribution = getTimeDistribution();
	}

	@Override
	protected String getName() {
		return "Dynamic programming (optimal)";
	}
	
	@Override
	protected String getAdditionalData() {
		return "cache size: " + mCache.size();
	}

	@Override
	protected String[] getAgentAndLevel(Game pGame) throws Exception {
//		System.out.println("mCache.size(): " + mCache.size());
		String[] refChoice = new String[2];
		HashMap<String, Long> scores = getLevelsScores(pGame);
		long timeLeft = pGame.getTimeLeft();
		getValue(pGame, scores, timeLeft, refChoice, new Object[2], 0);
		mAllowCacheUpdate = false;
		return refChoice;		
	}
	
	@Override
	protected long test() throws Exception {
		return super.test();
	}

	protected long getValue(Game pGame, HashMap<String, Long> pScores, long pTimeLeft, String[] refChoice, Object[] refData, int depth) throws Exception {
		refChoice[0] = pGame.agents.iterator().next();
		refChoice[1] = pGame.levelNames.iterator().next();			
		if (pTimeLeft <= 0) {
			return sum(pScores);
		} else {
			long bestChoiceVal = 0;
			Iterator<String> levelsIter = pGame.levelNames.iterator();
			while (levelsIter.hasNext()) {
				String level = levelsIter.next();
				Iterator<String> agentsIter = pGame.agents.iterator();
				while (agentsIter.hasNext()) {
					String agent = agentsIter.next();
					if (depth > 1000) {
						int a=0;
						a=a+5;
					}
					long choiceVal = evaluateChoice(pGame, level, agent, pScores, pTimeLeft, depth);
					if (choiceVal > bestChoiceVal) {
						bestChoiceVal = choiceVal;
						refChoice[0] = agent;
						refChoice[1] = level;
					}
				}
			}
			refData[0] = mCache.size();
			return bestChoiceVal;
		}
	}
	
	private long sum(HashMap<String, Long> pScores) {
		long retVal = 0;
		Iterator<Long> iter = pScores.values().iterator();
		while (iter.hasNext()) {
			retVal += iter.next();
		}
		return retVal;
	}
	
	private long evaluateChoice(Game pGame, String level, String agent, HashMap<String, Long> pScores, long pTimeLeft, int depth) throws Exception {
		ChoiceEvaluation choiceEvaluation = new ChoiceEvaluation(level, agent, pScores, pTimeLeft);
		if (mCache.containsKey(choiceEvaluation)) {
			return mCache.get(choiceEvaluation);
		}
		if (!mAllowCacheUpdate) {
//			throw new Exception("mAllowCacheUpdate=" + mAllowCacheUpdate);
		}
		long retVal = 0;
		Distribution timeDistribution = mTimeDistribution.get(agent).get(level);
		Distribution scoreDistribution = mScoresDistribution.get(agent).get(level);
		Iterator<Integer> timeDistributionIter = timeDistribution.mTally.keySet().iterator();
		while (timeDistributionIter.hasNext()) {
			Integer time = timeDistributionIter.next();
			Iterator<Integer> scoreDistributionIter = scoreDistribution.mTally.keySet().iterator();
			while (scoreDistributionIter.hasNext()) {
				Integer score = scoreDistributionIter.next();
				HashMap<String, Long> newScores = getNewScores(pScores, level, score);
				double likelihood = scoreDistribution.getLikelihood(score) * timeDistribution.getLikelihood(time);
				retVal += likelihood * getValue(pGame, newScores, pTimeLeft - time, new String[2], new Object[2], depth + 1);
			}
		}
		mCache.put(choiceEvaluation, retVal);
		return retVal;
	}

	private HashMap<String, Long> getNewScores(HashMap<String, Long> pScores, String level, long pValue) {
		HashMap<String, Long> retVal = new HashMap<>();
		Iterator<String> iter = pScores.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			retVal.put(key, pScores.get(key));
		}
		if (pValue > retVal.get(level)) {
			retVal.put(level, pValue);
		}
		return retVal;
	}
}
