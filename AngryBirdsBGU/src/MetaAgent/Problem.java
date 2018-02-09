package MetaAgent;

import java.util.ArrayList;
import java.util.Collections;

public class Problem {
	public ArrayList<String> agents = new ArrayList<>();
	public ArrayList<String> levels = new ArrayList<>();
	public int timeConstraint;
	
	@Override
	public String toString() {
		ArrayList<String> sortedAgents = new ArrayList<>(agents);
		Collections.sort(sortedAgents);
		
		ArrayList<String> sortedLevels = new ArrayList<>(levels);
		Collections.sort(sortedLevels);
		
		return timeConstraint + " seconds " + String.join(",", sortedAgents) + " " + String.join(",", levels);
	}
}
