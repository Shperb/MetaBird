package MetaAgent;
import java.io.IOException;

import ab.vision.GameStateExtractor.GameState;
import external.ClientMessageTable;

public class Proxy {

	public MyConnectionToServer mConnectionToServer;
	private byte[] mConfigureResult;
	private MetaAgent mMetaAgent;

    public Proxy(int serverport, String serverIp) throws Exception {
        mConnectionToServer = new MyConnectionToServer(serverport,serverIp);
    }

    public void setConfigureResult(byte[] pConfigureResult) {
		mConfigureResult = pConfigureResult;
	}
	
	public void start() throws Exception {

		try {
			while (true)
			{		
				Object[] refMessage = new Object[1];
				boolean deliverd = false;
				try {
					deliverd = deliverMessageFromClient(refMessage);
					mMetaAgent.actBeforeServerResponse((byte[])(refMessage[0]));
					if (deliverd) {
						deliverMessageFromServer();
					}
					mMetaAgent.actAfterServerResponse((byte[])(refMessage[0]));
                } catch (Exception e) {
                    e.printStackTrace();
                    MyLogger.log(e);
                    mMetaAgent.handleClientConnectionError();
                }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	boolean deliverMessageFromClient(Object[] refMessage) throws IOException, ClientConnectionException
	{
//		MyLogger.log("in");
		byte[] message = getConnectionToClient().readMessage();
//		MyLogger.log("from client : " + ClientMessageTable.getValue(message[0]) + ". size: " + message.length + ". message: " + MyLogger.byteArrayPrefix(message, 100));
//		System.out.println("from client : " + ClientMessageTable.getValue(message[0]) + ". size: " + message.length + ". message: " + MyLogger.byteArrayPrefix(message, 100));
		boolean retVal = false;
		if (message[0] == ClientMessageTable.getValue(ClientMessageTable.loadLevel) || message[0] == ClientMessageTable.getValue(ClientMessageTable.restartLevel)) {
			MyLogger.log("Agent requests " + ClientMessageTable.getValue(message[0]) + ". Ignoring.");
			getConnectionToClient().write(new byte[] {1});
			mMetaAgent.getWorkingAgent().loadLevel();
		}
		else if (message[0] == ClientMessageTable.getValue(ClientMessageTable.configure)) {
			getConnectionToClient().write(mConfigureResult);
		}
		else if (message[0] == ClientMessageTable.getValue(ClientMessageTable.doScreenShot)) {
			// sometimes the screen shot comes from the server in two parts.
			// the method doScreenShot(pInputStream) makes sure that the whole image arrives	
			mConnectionToServer.write(message);
			byte[] screenshot = readScreenShot();
			getConnectionToClient().write(screenshot);
		}
		else if (message[0] == ClientMessageTable.getValue(ClientMessageTable.getState)) {
			getConnectionToClient().write(new byte[] {(byte)GameState.PLAYING.ordinal()});	
		}
		else {
			mConnectionToServer.write(message);
			retVal = true;
		}
		refMessage[0] = message;
		return retVal;
	}
	
	void deliverMessageFromServer() throws IOException, ClientConnectionException
	{  
		MyLogger.log("in");
		byte[] message = mConnectionToServer.readMessage();
		MyLogger.log("from server: " + MyLogger.byteArrayPrefix(message, 10) + ".  size: " + message.length);
//		System.out.println("got message from server. size: " + message.length);
		getConnectionToClient().write(message);
	}	
	
	public byte[] doScreenShot() throws Exception {
		byte[] message = new byte[1];
		message[0] = ClientMessageTable.getValue(ClientMessageTable.doScreenShot);
		mConnectionToServer.write(message);
		byte[] retVal = readScreenShot();
		return retVal;
	}
	
	public  byte[] readScreenShot() {
		byte[] retVal = {};
		try {
			//Read the message head : 4-byte width and 4-byte height, respectively
			byte[] bytewidth = new byte[4];
			byte[] byteheight = new byte[4];
			int width, height;
			mConnectionToServer.read(bytewidth);
			width = Utils.bytesToInt(bytewidth);
			mConnectionToServer.read(byteheight);
			height = Utils.bytesToInt(byteheight);
			
			//initialize total bytes of the screenshot message
			//not include the head
			int imageBytes = width * height * 3;

			//System.out.println(width + "  " + height);
			retVal = new byte[imageBytes + 8];
			System.arraycopy(bytewidth, 0, retVal, 0, 4);
			System.arraycopy(byteheight, 0, retVal, 4, 4);
			byte[] image = mConnectionToServer.readMessage(imageBytes);
			System.arraycopy(image, 0, retVal, 8, imageBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retVal;
	}
	
	private MyConnection getConnectionToClient() {
		return mMetaAgent.mWorkingAgent.getConnectionToClient();
	}

	public void setMetaAgent(MetaAgent pMetaAgent) {
		mMetaAgent = pMetaAgent;
	}	
}
