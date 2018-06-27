import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;

import ab.server.Proxy;
import ab.server.proxy.message.ProxyReloadMessage;

public class ServerWrap {
	private String mABServerPid = null;
	private MyConnectionToClient[] mMyConnectionToClient = {null};
	private MyConnectionToServer[] mMyConnectionToServer = {null};
	private Proxy proxyToChrome;

	public void start() throws Exception {
		connectToChrome();
		setMyConnectionToClient(new MyConnectionToClient());
		newConnectionThread(mMyConnectionToClient, mMyConnectionToServer);
	}
	
	private void setMyConnectionToClient(MyConnectionToClient pMyConnectionToClient) {
		mMyConnectionToClient[0] = pMyConnectionToClient;
	}
	
	private void setMyConnectionToServer(MyConnectionToServer pMyConnectionToServer) {
		mMyConnectionToServer[0] = pMyConnectionToServer;
	}
	
	private void newConnectionThread(MyConnection[] pFrom, MyConnection[] pTo) {
		new Thread() {
			public void run() {
				boolean error = false;
				while (!error) {
					byte[] message;
					try {
						message = pFrom[0].readMessage();
						if (new String(message).startsWith(Constants.newGameMessage)) {
							String[] levels = new String(message).replaceFirst(Constants.newGameMessage, "").split(",");
							startNewGame(levels);
						}
						else {
							pTo[0].write(message);
						}
					} catch (Exception e) {
						error = true;
						System.out.println((pFrom[0]!=null?pFrom[0].getClass().getSimpleName():"null") + " to " + (pTo[0]!=null?pTo[0].getClass().getSimpleName():"null"));
						if (pFrom[0]!=null && !pFrom[0].isDisabled()) {
							MyLogger.log(e);
							e.printStackTrace();
						}
					}
				}
			}
		}.start();		
	}

	private void connectToChrome() throws UnknownHostException {
		proxyToChrome = new Proxy(9001);
		proxyToChrome.start();
		proxyToChrome.waitForClients(1);
	}

	private void startNewGame(String[] pLevels) throws Exception {
		System.out.println("starting new game");
		proxyToChrome.waitForClients(1);
		proxyToChrome.send(new ProxyReloadMessage());			
		closeABServer();
//		new JsonLevelsHandler().copy(pLevels);
		ExtensionLevelsHandler.copy(pLevels);
		runABServer();
		Thread.sleep(10000);// wait for page to load
		clickStartABServer();		
	};

	
	// private void initPid() throws Exception {
	// ArrayList<String> javaApps = getRunningJavaApps();
	// if (javaApps.size() != 1) {
	// throw new Exception("Found " + javaApps.size() + " running java apps");
	// }
	//
	// mPid = javaApps.get(0).replaceFirst(".*exe", "").replaceFirst("[ ]+",
	// "").replaceFirst(" .*", "");
	// }

	private void closeABServer() throws Exception {
		// ArrayList<String> javaApps = getRunningJavaApps();
		// if (javaApps.size() != 2) {
		// throw new Exception("Found " + javaApps.size() + " running java apps");
		// }
		// for (int i = 0; i < javaApps.size(); i++) {
		// if (!javaApps.get(i).equals(mPid)) {
		// Runtime.getRuntime()
		// .exec(System.getenv("windir") + "\\system32\\" + "taskkill.exe /pid " +
		// javaApps.get(i));
		// }
		// }
		if (mABServerPid != null) {
			mMyConnectionToServer[0].setIsDisabled();
			Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\" + "taskkill.exe /pid " + mABServerPid);
		}
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

	private void runABServer() throws Exception {
		ArrayList<String> runningJavaApps_before = getRunningJavaApps();
		Runtime.getRuntime().exec("c:/temp/naive/server.bat", null, new File("c:/temp/naive"));
		ArrayList<String> newJavaApps = getRunningJavaApps();
		newJavaApps.removeAll(runningJavaApps_before);
		if (newJavaApps.size() != 1) {
			throw new Exception("newJavaApps.size() = " + newJavaApps.size());
		}
		mABServerPid = newJavaApps.get(0);
		Thread.sleep(1000);
		setMyConnectionToServer(new MyConnectionToServer());
		newConnectionThread(mMyConnectionToServer, mMyConnectionToClient);
	}

	private void clickStartABServer() throws Exception {
		Robot robot = new Robot();
		robot.mouseMove(150, 360);
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
	}	
}
