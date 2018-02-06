package AlgorithmTester;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;

import DB.Game;
import MetaAgent.ChoiceEvaluation;
import MetaAgent.Distribution;

public  class AlgorithmTesterDynamicProgramming extends AlgorithmTester{

	public AlgorithmTesterDynamicProgramming(int pTimeConstraint) throws Exception {
		super(pTimeConstraint);
	}

	protected HashMap<String, Integer> mLevels = new HashMap<>();
	private HashMap<ChoiceEvaluation, Long> mCache = new HashMap<>();
	private HashMap<String, HashMap<String, Distribution>> mScoresDistribution;
	private HashMap<String, HashMap<String, Distribution>> mTimeDistribution;	

	@Override
	protected String[] getAgentAndLevel() throws ParseException {
		System.out.println("generating policy");
		String[] refChoice = new String[2];
		HashMap<String, Long> scores = getLevelsScores();
		mCache = new HashMap<>();
		long timeLeft = getGame().getTimeLeft();
		getValue(scores, timeLeft, refChoice, new Object[2], 0);
		System.out.println("generating policy - done");
		return refChoice;		
	}

	private Game getGame() {
		return mGame;
	}

	private HashMap<String, Long> getLevelsScores() {
		HashMap<String, Long> retVal = new HashMap<>();
		mLevels.keySet().forEach(level -> {
			retVal.put(level, (long) 0);
		});		
		getGame().levels.forEach(level->{
			retVal.put(level.name, (long) level.score);			
		});
		return retVal;
	};
	
	private long getValue(HashMap<String, Long> pScores, long pTimeLeft, String[] refChoice, Object[] refData, int depth) {
		if (pTimeLeft <= 0) {
			return sum(pScores);
		} else {
			long bestChoiceVal = 0;
			refChoice[0] = getAgentsNames().iterator().next();
			refChoice[1] = mLevels.keySet().iterator().next();			
			Iterator<String> levelsIter = mLevels.keySet().iterator();
			while (levelsIter.hasNext()) {
				String level = levelsIter.next();
				Iterator<String> agentsIter = getAgentsNames().iterator();
				while (agentsIter.hasNext()) {
					String agent = agentsIter.next();
					long choiceVal = evaluateChoice(level, agent, pScores, pTimeLeft, depth);
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
	
	private long evaluateChoice(String level, String agent, HashMap<String, Long> pScores, long pTimeLeft, int depth) {
		ChoiceEvaluation choiceEvaluation = new ChoiceEvaluation(level, agent, pScores, pTimeLeft);
		if (mCache.containsKey(choiceEvaluation)) {
			return mCache.get(choiceEvaluation);
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
				retVal += likelihood * getValue(newScores, pTimeLeft - time, new String[2], new Object[2], depth + 1);
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
