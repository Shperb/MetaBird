package AlgorithmTester;

import java.util.HashMap;
import java.util.Iterator;

import DB.Game;
import Distribution.Distribution;
import MetaAgent.ChoiceEvaluation;
import MetaAgent.Problem;

public  class AlgorithmTesterDynamicProgramming extends AlgorithmTester{

	private boolean mAllowCacheUpdate = true; 
	private HashMap<ChoiceEvaluation, Long> mCache = new HashMap<>();
	protected HashMap<String, HashMap<String, Distribution>> mScoresDistribution;
	protected HashMap<String, HashMap<String, Distribution>> mTimeDistribution;	

	public AlgorithmTesterDynamicProgramming(Problem pProblem,HashMap<String,
			HashMap<String, Distribution>> realScoreDistribution,
			HashMap<String, HashMap<String, Distribution>> realTimeDistribution,
			HashMap<String, HashMap<String, Distribution>> policyScoreDistribution,
			HashMap<String, HashMap<String, Distribution>> policyTimeDistribution) throws Exception {
		super(pProblem,realScoreDistribution,realTimeDistribution,policyScoreDistribution,policyTimeDistribution);
		mScoresDistribution = getScoresDistribution();
		mTimeDistribution = getTimeDistribution();
	}

	@Override
	protected String getName() {
		return "Dynamic programming (optimal)" + getNameExtension();
	}
	
	@Override
	protected String getAdditionalData() {
		return "cache size \t" + mCache.size();
	}

	@Override
	protected String[] getAgentAndLevel(Game pGame,long[] additionalTime) throws Exception {
//		System.out.println("mCache.size(): " + mCache.size());
		String[] refChoice = new String[2];
		HashMap<String, Long> scores = getLevelsScores(pGame);
		long timeLeft = pGame.getTimeLeft();
		getValue(pGame, scores, timeLeft, refChoice, new Object[2], 0,additionalTime,true);
		mAllowCacheUpdate = false;
		return refChoice;		
	}
	
	protected long getValue(Game pGame, HashMap<String, Long> pScores, long pTimeLeft, String[] refChoice, Object[] refData, int depth,long[] additionalTime,boolean first) throws Exception {
		refChoice[0] = pGame.agents.iterator().next();
		refChoice[1] = pGame.levelNames.iterator().next();			
		if (pTimeLeft < 0) {
			throw new Exception("negative time left");
		} else {
			long bestChoiceVal = 0;
			Iterator<String> levelsIter = pGame.levelNames.iterator();
			while (levelsIter.hasNext()) {
				String level = levelsIter.next();
				Iterator<String> agentsIter = pGame.agents.iterator();
				while (agentsIter.hasNext()) {
					String agent = agentsIter.next();
					long choiceVal = evaluateChoice(pGame, level, agent, pScores, pTimeLeft, depth,additionalTime,first);
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
	
	private long evaluateChoice(Game pGame, String level, String agent, HashMap<String, Long> pScores, long pTimeLeft, int depth,long[] additionalTime,boolean first) throws Exception {
		ChoiceEvaluation choiceEvaluation = new ChoiceEvaluation(level, agent, pScores, pTimeLeft);
		if (mCache.containsKey(choiceEvaluation)) {
			return mCache.get(choiceEvaluation);
		}
		if (!mAllowCacheUpdate) {
			compare(mScoresDistribution, mRealScoreDistribution);
			compare(mTimeDistribution, mRealRunTimeDistribution);
			compare(mRealScoreDistribution, mScoresDistribution);
			compare(mRealRunTimeDistribution, mTimeDistribution);
			throw new Exception("mAllowCacheUpdate=" + mAllowCacheUpdate);
		}
		long tStart = System.currentTimeMillis();
		long retVal = 0;
		Distribution timeDistribution = mTimeDistribution.get(agent).get(level);
		Distribution scoreDistribution = mScoresDistribution.get(agent).get(level);
		Iterator<Integer> timeDistributionIter = timeDistribution.getSupport().iterator();
		while (timeDistributionIter.hasNext()) {
			Integer time = timeDistributionIter.next();
			if (time > pTimeLeft){
				retVal += sum(pScores) * timeDistribution.getLikelihood(time);
			}
			else{
				Iterator<Integer> scoreDistributionIter = scoreDistribution.getSupport().iterator();
				while (scoreDistributionIter.hasNext()) {
					Integer score = scoreDistributionIter.next();
					HashMap<String, Long> newScores = getNewScores(pScores, level, score);
					double likelihood = scoreDistribution.getLikelihood(score) * timeDistribution.getLikelihood(time);
					retVal += likelihood * getValue(pGame, newScores, pTimeLeft - time, new String[2], new Object[2], depth + 1,additionalTime,false);
				}
			}
		}
		mCache.put(choiceEvaluation, retVal);
		if (first){
			additionalTime[0] += System.currentTimeMillis() - tStart;
		}
		return retVal;
	}

	private void compare(
			HashMap<String, HashMap<String, Distribution>> pMap1,
			HashMap<String, HashMap<String, Distribution>> pMap2) {
		for (String agent : pMap1.keySet()){
			for (String level : pMap1.get(agent).keySet()){
				double exp1 = pMap1.get(agent).get(level).getExpectation();
				double exp2 = pMap2.get(agent).get(level).getExpectation();
				if (exp1 != exp2){
					System.out.println("compare failed");
				}
			}
		}
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
