package MetaAgent;
import java.util.HashMap;
import java.util.Iterator;

public class ChoiceEvaluation {
	String mLevel;
	String mAgent;
	HashMap<String, Long> mScores = new HashMap<>();
	long mTimeLeft;
	
	public ChoiceEvaluation(String level, String agent, HashMap<String, Long> pScores, long pTimeLeft) {
		this.mLevel = level;
		this.mAgent = agent;
		Iterator<String> iter = pScores.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			mScores.put(key, pScores.get(key));
		}
		this.mScores = pScores;
		this.mTimeLeft = pTimeLeft;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mAgent == null) ? 0 : mAgent.hashCode());
		result = prime * result + ((mLevel == null) ? 0 : mLevel.hashCode());
		Iterator<Long> iter = mScores.values().iterator();
		while (iter.hasNext()) {
			result = prime * result + iter.next().hashCode();
		}
		result = prime * result + (int) (mTimeLeft ^ (mTimeLeft >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChoiceEvaluation other = (ChoiceEvaluation) obj;
		if (mAgent == null) {
			if (other.mAgent != null)
				return false;
		} else if (!mAgent.equals(other.mAgent))
			return false;
		if (mLevel == null) {
			if (other.mLevel != null)
				return false;
		} else if (!mLevel.equals(other.mLevel))
			return false;
		if (mScores == null) {
			if (other.mScores != null)
				return false;
		} else if (!scoresEquals(other.mScores))
			return false;
		if (mTimeLeft != other.mTimeLeft)
			return false;
		return true;
	}

	private boolean scoresEquals(HashMap<String, Long> mScores2) {
		 Iterator<String> iter = mScores.keySet().iterator();
		 while (iter.hasNext()) {
			 String key = iter.next();
			 if (!mScores.get(key).equals(mScores2.get(key))) {
				 return false;
			 }
		 }
		 return true;
	}
	
	
}
