package MetaAgent;

import java.io.File;

public class MetaAgentDistributionSampling extends MetaAgent {
	private final int mSamplesPerPair = 2;

	public MetaAgentDistributionSampling(int pTimeConstraint) {
		super(pTimeConstraint);
	}

	@Override
	protected String getAlgorithmName() {
		return "Distribution Sampling";
	}

	@Override
	protected String selectLevels() {
		File folder = new File(Constants.levelsDir);
		File[] listOfFiles = folder.listFiles();
		
		File[] shortListOfFiles = new File[20];
		for (int i=0; i<20; i++) {
			shortListOfFiles[i] = listOfFiles[i*20];
		}

		mLevels.clear();
		String retVal = "";
		for (int i = 0; i < shortListOfFiles.length && mLevels.size() < 8; i++) {
			String level = shortListOfFiles[i].toPath().getFileName().toString().replace(".json", "");
			if (isRequired(level)) {
				mLevels.put(level, mLevels.size() + 1);
				retVal += "," + level;
			}
		}
		
		retVal = retVal.replaceFirst(",", "");
		
		return retVal;
	}

	@Override
	protected String[] GetNewAgentAndLevel() throws Exception {
		if (mLevels.isEmpty()) {
			throw new Exception("done sampling");
		}
		
		final String[] retVal = {null,null};
		mLevels.keySet().forEach(level->{
			getAgentsNames().forEach(agent->{
				System.out.println(agent + ", " + level + " : " + getCount(agent, level));
				if (getCount(agent, level) < mSamplesPerPair) {
					retVal[0] = agent;
					retVal[1] = level;
				}
			});
		});
		if (retVal[0] == null) {
			getGame().setEndTime();
			selectLevels();
			createNewGameEntry();
			return GetNewAgentAndLevel();
		}
		else {
			return retVal;			
		}
	}

	@Override
	protected int getTimeConstraint() {
		return Integer.MAX_VALUE;
	}
	
	private boolean isRequired(String pLevel) {
		boolean[] retVal = {false};
		getAgentsNames().forEach(a->{
			if (getCount(a, pLevel) < mSamplesPerPair) {
				retVal[0] = true;
			}
		});
		return retVal[0];
	}

	private int getCount(String pAgent, String pLevel) {
		int[] retVal = {0};
		mData.games.forEach(game->{
			game.levels.forEach(level->{
				if (level.isFinished() && level.name.equals(pLevel) && level.agent.equals(pAgent)) {
					retVal[0] ++ ;
				}
			});
		});
		return retVal[0];
	}
}
