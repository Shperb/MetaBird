package MetaAgent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.ArrayList;

public class Agent {
	private String mExecutableFileName;
	private MetaAgent mMetaAgent;
	private MyConnectionToClient mConnectionToClient;
	private String mProcessId = null;
	
	public Agent(String pExecutableFileName, MetaAgent pMetaAgent){
		mExecutableFileName = pExecutableFileName;
		mMetaAgent = pMetaAgent;
	}
	
	public void start(ServerSocket pServerSocket) throws Exception {
		MyLogger.log("starting agent " + getName());
		ArrayList<String> runningJavaApps_before = getRunningJavaApps();
		mConnectionToClient = new MyConnectionToClient(mExecutableFileName, pServerSocket);
		ArrayList<String> newJavaApps = getRunningJavaApps();
		newJavaApps.removeAll(runningJavaApps_before);
		if (newJavaApps.size() != 1) {
			throw new Exception("newJavaApps.size() = " + newJavaApps.size());
		}
		mProcessId = newJavaApps.get(0);
		MyLogger.log("done");
	}

	public String getName() {
		return mExecutableFileName;
	}

	private ArrayList<String> getRunningJavaApps() throws Exception {
		ArrayList<String> retVal = new ArrayList<>();
		String line;
		Process p = Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\" + "tasklist.exe /v");
		BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
		while ((line = input.readLine()) != null) {
			if (line.contains(System.getProperty("user.name")) && line.startsWith("java")) {
				retVal.add(line.replaceFirst(".*exe", "").replaceFirst("[ ]+", "").replaceFirst(" .*", ""));
			}
		}
		input.close();
		return retVal;
	}
	
//	protected byte getState() throws IOException {
//		byte state = (byte) mMetaAgent.mLastGameState.ordinal();
//		return state;
//	}

	public void loadLevel() {
		// do nothing
		// derived classes should override
	}

	public MyConnectionToClient getConnectionToClient() {
		return mConnectionToClient;
	}
	
	public void kill() throws Exception {
		MyLogger.log("killing agent " + getName());
		if (mProcessId != null) {
			Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\" + "taskkill.exe /pid " + mProcessId);
		}
		MyLogger.log("done");
	}	
}
