package DB;

import java.text.ParseException;
import java.util.HashMap;

import MetaAgent.MyLogger;

public class Game extends Event{
	public Collection<String> agents = new Collection<>();
	public Collection<Level> levels = new Collection<>();
	public String algorithm;
	public int timeConstraint;
	
	public Game(String pAlgorithm, int pTimeConstraint) {
		algorithm = pAlgorithm;
		timeConstraint = pTimeConstraint;
	}

	public long getTimeLeft() throws ParseException {
		return timeConstraint - getTimeElapsed();
	}

	public long getScore() {
		HashMap<String, Integer> levelsScores = new HashMap<>();
		levels.forEach(level->{
			levelsScores.put(level.name, 0);
		});
		levels.forEach(level->{
			try {
				if (level.isFinished() && level.getEndTime() <= getBeginTime() + timeConstraint * 1000) {
					if (level.score > levelsScores.get(level.name)) {
						levelsScores.put(level.name, level.score);						
					}
				}
			} catch (ParseException e) {
				e.printStackTrace();
				MyLogger.log(e);
			}
		});
		
		long[] retVal = {0};
		levelsScores.values().forEach(s->{
			retVal[0] += s;
		});
		
		return retVal[0];
	}
}
