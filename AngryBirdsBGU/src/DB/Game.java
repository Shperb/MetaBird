package DB;

import java.text.ParseException;

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
}
