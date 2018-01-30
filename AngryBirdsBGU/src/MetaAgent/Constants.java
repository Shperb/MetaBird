package MetaAgent;
import java.util.HashSet;

import external.ClientMessageTable;

public class Constants {
	public final static String serverIp = "132.72.42.17";
//	public final static String serverIp = "localhost";
	public final static int serverPort = 2005;
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
	public static final int maxLevel = 21;
	public static final String logFilePath =  "c:/temp/MetaAgent.log";
	public static final String levelsDir = "C:/temp/Ai-birds/Levels";
	protected static String newGameMessage = "new game";
}
