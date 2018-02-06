package MetaAgent;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import Clock.Clock;
import Clock.SystemClock;
import DB.DBHandler;
import DB.Data;
import DB.Game;
import DB.Level;
import DB.LevelState;
import DB.Shot;
import ab.vision.GameStateExtractor;
import ab.vision.GameStateExtractor.GameState;
import external.ClientMessageEncoder;
import external.ClientMessageTable;

public abstract class MetaAgent {

	public GameState mLastGameState = GameState.MAIN_MENU;

	protected Agent mWorkingAgent;
	protected String mCurrentLevel;
	protected long mLoadLevelTime;
	protected Data mData;
	protected HashMap<String, Integer> mLevels = new HashMap<>();

	private Proxy mProxy;
	private ArrayList<Agent> mAgents = new ArrayList<>();
	private int mTimeConstraint;
	ServerSocket mServerSocket;

	abstract protected String getAlgorithmName();

	abstract protected String[] GetNewAgentAndLevel() throws Exception;

	public MetaAgent(int pTimeConstraint) {
		Clock.setClock(new SystemClock());
		
		mAgents.add(new Agent("planA", this));
		mAgents.add(new Agent("naive", this));
		mAgents.add(new Agent("ihsev", this));
		mAgents.add(new Agent("AngryBER", this));

		mTimeConstraint = pTimeConstraint;
	}

	protected String selectLevels() {
		File folder = new File(Constants.levelsDir);
		File[] listOfFiles = folder.listFiles();

		mLevels.clear();
		while (mLevels.size() < 8) {
			int rnd = (int) (Math.random() * listOfFiles.length);
			String level = listOfFiles[rnd].toPath().getFileName().toString().replace(".json", "");
			if (!mLevels.containsKey(level)) {
				mLevels.put(level, mLevels.size() + 1);
			}
		}
		return String.join(",", mLevels.keySet());
	}

	private void chooseAgentAndLevel() throws Exception {
		String[] agentAndLevel = GetNewAgentAndLevel();
		String agent = agentAndLevel[0];
		String level = agentAndLevel[1];

		mWorkingAgent = getAgent(agent);
		loadLevel(level, agent);

		MyLogger.log("selected agent " + agent + ", level " + level);
		System.out.println("selected agent " + agent + ", level " + level);
	}

	private Agent getAgent(String pName) throws Exception {
		for (int i = 0; i < mAgents.size(); i++) {
			if (mAgents.get(i).getName().equals(pName)) {
				return mAgents.get(i);
			}
		}
		throw new Exception("Could not find agent " + pName);
	}

	protected ArrayList<String> getAgentsNames() {
		ArrayList<String> retVal = new ArrayList<>();
		for (int i = 0; i < mAgents.size(); i++) {
			retVal.add(mAgents.get(i).getName());
		}
		return retVal;
	}

	public void start() throws Exception {
		mProxy = new Proxy();
		mData = DBHandler.loadDate();
		mProxy.setMetaAgent(this);
		runAgents();
		startNewGame();
		mProxy.start();
	}

	private void startNewGame() throws Exception {
		MyLogger.log("starting new game");
		mLastGameState = GameState.MAIN_MENU;
		String message = Constants.newGameMessage + selectLevels();
		mProxy.mConnectionToServer.write(message.getBytes(StandardCharsets.UTF_8));
		byte[] configureResult = configure(Utils.intToByteArray(1000));
		mProxy.setConfigureResult(configureResult);
		getMyScore();// getMyScore waits for "start" button to be clicked on the server window
		createNewGameEntry();
		chooseAgentAndLevel();
	}

	protected void createNewGameEntry() {
		mData.games.add(new Game(getAlgorithmName(), mTimeConstraint));
		for (int i = 0; i < mAgents.size(); i++) {
			getGame().agents.add(mAgents.get(i).getName());
		}
	}

	protected Game getGame() {
		return mData.games.getLast();
	}

	protected Level getLevel() {
		return getGame().levels.getLast();
	}

	protected int getTimeConstraint() {
		return mTimeConstraint;
	}

	private Shot getShot() {
		return getLevel().shots.getLast();
	}

	private boolean isShotMessage(byte[] pMessage) {
		return Constants.shotsMessages.contains(getMessageType(pMessage));
	}

	public void actBeforeServerResponse(byte[] pMessage) throws IOException {
		if (isShotMessage(pMessage)) {
			byte[] params = new byte[pMessage.length - 1];
			System.arraycopy(pMessage, 1, params, 0, pMessage.length - 1);
			Shot shot = new Shot(params, mWorkingAgent.getName(), getMessageType(pMessage));
			getLevel().shots.add(shot);
		}
	}

	private ClientMessageTable getMessageType(byte[] pMessage) {
		return ClientMessageTable.getValue(pMessage[0]);
	}

	public void actAfterServerResponse(byte[] pMessage) throws Exception {
		if (isShotMessage(pMessage)) {
			getShot().setEndTime();
			getShot().score = getScore(false);
		}
		
		MyLogger.log("@state = " + getGameState());

		GameState state;
		if (notPlaying()) {
			MyLogger.log("agent is not playing");
			System.out.println("agent is not playing");
			getLevel().state = LevelState.stoped_not_playing;
			state = GameState.LOST;
		} else if (scoreNotChanging()) {
			MyLogger.log("score not changing");
			System.out.println("score not changing");
			getLevel().state = LevelState.stoped_score_not_changing;
			state = GameState.LOST;
		} else {
			state = getGameState();
			if (state == GameState.WON) {
				getLevel().state = LevelState.won;
			}
			if (state == GameState.LOST) {
				getLevel().state = LevelState.lost;
			}
		}
		if (GameState.WON != mLastGameState && GameState.LOST != mLastGameState) {
			if (GameState.WON == state || GameState.LOST == state) {
				MyLogger.log("GameState: " + state.name());
				if (GameState.WON == state) {
					getLevel().score = getScore(true);
				}
				getLevel().setEndTime();
				DBHandler.save(mData);
				System.out.println("getGame().getTimeElapsed(): " + getGame().getTimeElapsed()
						+ ", getTimeConstraint(): " + getTimeConstraint());
				MyLogger.log("getGame().getTimeElapsed(): " + getGame().getTimeElapsed() + ", getTimeConstraint(): "
						+ getTimeConstraint());
				if (getGame().getTimeElapsed() > getTimeConstraint()) {
					startNewGame();
				} else {
					chooseAgentAndLevel();
				}
			}
		}
		mLastGameState = state;
	}

	private boolean notPlaying() throws ParseException {
		int threshold = 20;
		long now = new Date().getTime();
		if (getLevel().shots.isEmpty()) {
			return getLevel().getTimeElapsed() > threshold;
		} else {
			if (getShot().getEndTime() != null) {
				return (now - getShot().getEndTime()) / 1000 > threshold;
			} else {
				return false;
			}
		}
	}

	private boolean scoreNotChanging() {
		int shotsCnt = 3;
		Distribution distribution = new Distribution();
		boolean[] retVal = { false };
		getLevel().shots.forEach(shot -> {
			distribution.addTally(shot.score);
			if (distribution.getLikelihood(shot.score) * distribution.getTotalTally() + 0.5 >= shotsCnt) {
				retVal[0] = true;
			}
		});
		return retVal[0];
	}

	private int getScore(boolean pScoreForWin) throws Exception {

		int current_score = -1;
		while (current_score != _getScore()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
			if (!pScoreForWin || getGameState() == GameState.WON) {
				current_score = _getScore();
			} else
				System.out.println(" Unexpected state: PLAYING");
		}
		return current_score;
	}

	private int _getScore() throws Exception {
		int score = -1;

		BufferedImage image = doScreenShot();

		GameStateExtractor gameStateExtractor = new GameStateExtractor();
		GameState state = getGameState();
		if (state == GameState.PLAYING)
			score = gameStateExtractor.getScoreInGame(image);
		else if (state == GameState.WON)
			score = gameStateExtractor.getScoreEndGame(image);
		if (score == -1)
			System.out.println(" Game score is unavailable ");
		return score;
	}

	public BufferedImage doScreenShot() throws Exception {
		BufferedImage bfImage = null;
		byte[] sceenShotBytes = mProxy.doScreenShot();

		// Read the message head : 4-byte width and 4-byte height, respectively
		byte[] bytewidth = Arrays.copyOfRange(sceenShotBytes, 0, 4);
		byte[] byteheight = Arrays.copyOfRange(sceenShotBytes, 4, 8);
		int width, height;
		width = Utils.bytesToInt(bytewidth);
		height = Utils.bytesToInt(byteheight);

		byte[] imgbyte = Arrays.copyOfRange(sceenShotBytes, 8, sceenShotBytes.length);

		// set RGB data using BufferedImage
		bfImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int R = imgbyte[(y * width + x) * 3] & 0xff;
				int G = imgbyte[(y * width + x) * 3 + 1] & 0xff;
				int B = imgbyte[(y * width + x) * 3 + 2] & 0xff;
				Color color = new Color(R, G, B);
				int rgb;
				rgb = color.getRGB();
				bfImage.setRGB(x, y, rgb);
			}
		}
		return bfImage;

	}

	GameState getGameState() throws IOException {
		MyLogger.log("in");
		GameState state = GameState.UNKNOWN;
		try {
			mProxy.mConnectionToServer.write(ClientMessageEncoder.getState());
			byte stateByte = (byte) mProxy.mConnectionToServer.read();
			state = GameState.values()[stateByte];
		} catch (Exception e) {
			e.printStackTrace();
			Thread t = new Thread() {
				@Override
				public void run() {
					byte[] buffer = new byte[2048];
					try {
						mProxy.mConnectionToServer.read(buffer);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			t.start();
			try {
				t.join(5000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			t.interrupt();
		}
		MyLogger.log("out");
		return state;
	}

	private void loadLevel(String pLevelName, String pAgent) throws Exception {
		byte level = (byte) (int) (mLevels.get(pLevelName));
		byte[] message = { ClientMessageTable.getValue(ClientMessageTable.loadLevel), level };
		mProxy.mConnectionToServer.write(message);
		getGame().levels.add(new Level(pLevelName, pAgent));
		DBHandler.save(mData);
		int loaded = mProxy.mConnectionToServer.read();
		GameState state = getGameState();
		if (state == GameState.PLAYING) {
			if (loaded == 1) {
				mCurrentLevel = pLevelName;
				MyLogger.log("loaded level " + level);
				System.out.println("loaded level " + level);
			} else {
				MyLogger.log("failed to load level " + level);
				System.err.println("failed to load level " + level);
			}
			mLoadLevelTime = System.currentTimeMillis();
		}
		else {
			MyLogger.log("for loading level " + pLevelName + ": state = " + state + " instead of " + GameState.PLAYING);
			System.out.println("for loading level " + pLevelName + ": state = " + state + " instead of " + GameState.PLAYING);
			loadLevel(pLevelName, pAgent);
		}
	}

	// register team id
	public byte[] configure(byte[] team_id) {
		try {
			mProxy.mConnectionToServer.write(ClientMessageEncoder.configure(team_id));
			byte[] result = new byte[4];
			mProxy.mConnectionToServer.read(result);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	// send a message to score of each level
	public byte[] getMyScore() {
		int level = 21;
		int totalBytes = level * 4;
		byte[] buffer = new byte[totalBytes];
		try {
			mProxy.mConnectionToServer.write(ClientMessageEncoder.getMyScore());

			mProxy.mConnectionToServer.read(buffer);
			return buffer;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return buffer;
	}

	void runAgents() {
		try {
			mServerSocket = new ServerSocket(Constants.clientPort);
			mServerSocket.setSoTimeout(20000);
			Iterator<Agent> iter = mAgents.iterator();
			while (iter.hasNext()) {
				iter.next().start(mServerSocket);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Agent getWorkingAgent() {
		return mWorkingAgent;
	}

	public void handleClientConnectionError() throws Exception {
		System.out.println("restarting agent " + mWorkingAgent.getName());
		MyLogger.log("restarting agent " + mWorkingAgent.getName());
		mWorkingAgent.kill();
		mWorkingAgent.start(mServerSocket);
		getLevel().state = LevelState.connection_error;
		getLevel().setEndTime();
		DBHandler.save(mData);
		if (getGame().getTimeElapsed() > getTimeConstraint()) {
			startNewGame();
		} else {
			chooseAgentAndLevel();
		}
	}
}
