package MetaAgent;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;


import DB.Game;
import Distribution.Distribution;import Distribution.ImplicitDistribution;


public class MetaAgentDynamicProgramming extends MetaAgent {
	private HashMap<String, HashMap<String, ImplicitDistribution>> mScoresDistribution;
	private HashMap<String, HashMap<String, ImplicitDistribution>> mTimeDistribution;
	private HashMap<ChoiceEvaluation, Long> mCache = new HashMap<>();
	private boolean mSamplingDistribution = true;
	private final int mSupportSize = 2;
	private boolean mGeneratePolicy = false;
	private final int mMaxHorizon = 180;
	private String[] mChoice = null;

	public MetaAgentDynamicProgramming(int pTimeConstraint, String[] pAgents) {
		super(pTimeConstraint, pAgents);
	}

//	@Override
//	protected String selectLevels() {
//		String levels = super.selectLevels();
//		mSamplingDistribution = true;
//		mGeneratePolicy = false;
//		mCache = new HashMap<>();
//		return levels;
//	}
//
//	@Override
//	protected String getAlgorithmName() {
//		if (mSamplingDistribution) {
//			return "Distribution Sampling";
//		} else {
//			return "dynamic programming horizon  " + mMaxHorizon;
//		}
//	}

	@Override
	protected String[] GetNewAgentAndLevel() throws Exception {
		if (mSamplingDistribution) {
			calcDistribution();
			final ArrayList<String[]> retVal = new ArrayList<>();
			mScoresDistribution.forEach((agent, levels) -> {
				levels.forEach((level, distribution) -> {
					if (distribution.getTotalTally() < mSupportSize) {
						retVal.add(new String[] { agent, level });
					}
				});
			});
			if (retVal.isEmpty()) {
				getGame().setEndTime();
				mSamplingDistribution = false;
				generatePolicy();
				createNewGameEntry();
				return GetNewAgentAndLevel();
			} else {
				return retVal.get(0);
			}
		} else {
			while (mChoice == null) {
				Thread.sleep(1000);
			}
			return new String[] { mChoice[0], mChoice[1] };
		}
	}

	@Override
	protected int getTimeConstraint() {
		if (mSamplingDistribution) {
			return Integer.MAX_VALUE;
		} else {
			return super.getTimeConstraint();
		}
	}

	private void generatePolicy() {
		mGeneratePolicy = true;
		new Thread() {
			public void run() {
				while (mGeneratePolicy) {
					System.out.println("generating policy");
					int horizon = mMaxHorizon;
					try {
						horizon = (int) Math.min(mMaxHorizon, getGame().getTimeLeft() - 30);
					} catch (ParseException e) {
						MyLogger.log(e);
						e.printStackTrace();
					}
					if (horizon < 0) {
						mGeneratePolicy = false;
					}
					else {
						String[] refChoice = new String[2];
						HashMap<String, Long> scores = getLevelsScores();
						mCache = new HashMap<>();
						getValue(scores, horizon, refChoice, new Object[2], 0);
						mChoice = refChoice;
						System.out.println("generating policy - done");
					}
				}
				System.out.println("stopped generating policies");
			}
		}.start();
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
//			if (depth <= 1) {
//				MyLogger.log(pTimeLeft + " seconds left. " + refChoice[0] + ", " + refChoice[1] + ": " + bestChoiceVal + ". expScore: " + mScoresDistribution.get(refChoice[0]).get(refChoice[1]).mTally + ". expTime: " + mTimeDistribution.get(refChoice[0]).get(refChoice[1]).mTally);
//			}
			refData[0] = mCache.size();
			return bestChoiceVal;
		}
	}

	private long evaluateChoice(String level, String agent, HashMap<String, Long> pScores, long pTimeLeft, int depth) {
		ChoiceEvaluation choiceEvaluation = new ChoiceEvaluation(level, agent, pScores, pTimeLeft);
		if (mCache.containsKey(choiceEvaluation)) {
			return mCache.get(choiceEvaluation);
		}
		long retVal = 0;
		ImplicitDistribution timeDistribution = mTimeDistribution.get(agent).get(level);
		ImplicitDistribution scoreDistribution = mScoresDistribution.get(agent).get(level);
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

	private long sum(HashMap<String, Long> pScores) {
		long retVal = 0;
		Iterator<Long> iter = pScores.values().iterator();
		while (iter.hasNext()) {
			retVal += iter.next();
		}
		return retVal;
	}

	private void calcDistribution() {
		mScoresDistribution = new HashMap<>();
		mTimeDistribution = new HashMap<>();

		getAgentsNames().forEach(agent -> {
			mScoresDistribution.put(agent, new HashMap<>());
			mTimeDistribution.put(agent, new HashMap<>());
			mLevels.keySet().forEach(level -> {
				//mScoresDistribution.get(agent).put(level, new ImplicitDistribution());
				//mTimeDistribution.get(agent).put(level, new ImplicitDistribution());
			});
		});

		ArrayList<Game> games = new ArrayList<>(mData.games);
		Collections.shuffle(games);
		games.forEach(game -> {
			game.levels.forEach(level -> {
				if (mLevels.keySet().contains(level.name) && getAgentsNames().contains(level.agent)) {
					if (level.getTimeTaken() != null) {
						if (mScoresDistribution.get(level.agent).get(level.name).getTotalTally() < mSupportSize) {
							mScoresDistribution.get(level.agent).get(level.name).addTally(level.score);
							mTimeDistribution.get(level.agent).get(level.name).addTally(level.getTimeTaken());
						}
					}
				}
			});
		});
	}

	@Override
	protected String getAlgorithmName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean shouldStartNewGame() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean shouldExit() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected ArrayList<String> getLevelsList() {
		// TODO Auto-generated method stub
		return null;
	}
}
