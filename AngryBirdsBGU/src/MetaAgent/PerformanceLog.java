package MetaAgent;

public class PerformanceLog {
	int level;
	long fromTime;
	long toTime;
	Agent agent;
	boolean won;
	int score;
	
	
	public PerformanceLog(int level, long fromTime, long toTime, Agent agent, boolean won, int score) {
		super();
		this.level = level;
		this.fromTime = fromTime;
		this.toTime = toTime;
		this.agent = agent;
		this.won = won;
		this.score = score;
	}
	
	
}
