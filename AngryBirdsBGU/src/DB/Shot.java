package DB;

import external.ClientMessageTable;

public class Shot extends Event{
	public byte[] params;
	public String agent;
	public int score;
	ClientMessageTable shotType;
	
	public Shot(byte[] params, String agent, ClientMessageTable shotType) {
		super();
		this.params = params;
		this.agent = agent;
		this.shotType = shotType;
	}
}
