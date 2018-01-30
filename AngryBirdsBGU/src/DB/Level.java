package DB;

public class Level extends Event{
	public Collection<Shot> shots = new Collection<>();
	public LevelState state = LevelState.playing;
	public String name;
	public String agent;
	public int score;
	
	public Level(String pName, String pAgent){
		name = pName;		
		agent = pAgent;
	}
}
