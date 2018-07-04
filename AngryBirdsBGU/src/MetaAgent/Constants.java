package MetaAgent;
import java.util.HashSet;

import external.ClientMessageTable;

public class Constants {
//	public final static String serverIp = "132.72.47.196";
	public final static String serverIp = "localhost";
	public final static int serverPort = 2018;
	public final static int clientPort = 2004;
	protected static long timeToTerminateAgentMilis = 3 * 60 * 1000;
	public static HashSet<ClientMessageTable> shotsMessages = new HashSet<ClientMessageTable>() {
		private static final long serialVersionUID = 1L;
		{
			add(ClientMessageTable.cshoot);
			add(ClientMessageTable.pshoot);
			add(ClientMessageTable.cFastshoot);
			add(ClientMessageTable.pFastshoot);
			add(ClientMessageTable.shootSeqFast);
			add(ClientMessageTable.shootSeq);
		}
	};
    public static final String BaseDir = System.getProperty("user.dir");
    public static final int maxLevel = 21;
	public static final String logFilePath =  BaseDir + "/MetaAgent.log";
    public static final String AgentsDir =  BaseDir + "/agents/";
    public static final String levelsDir = BaseDir + "/Ai-birds/Levels";
    public static final String dataDir = BaseDir + "/MetaAgentDB/";

    protected static String newGameMessage = "new game";
}
