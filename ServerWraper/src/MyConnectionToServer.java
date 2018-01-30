
import java.net.Socket;

public class MyConnectionToServer extends MyConnection{

	private Socket communicationWithServerSocket;

	public MyConnectionToServer() throws Exception {
		communicationWithServerSocket = new Socket(Constants.serverIp, Constants.serverPort);
		mOutputStream = communicationWithServerSocket.getOutputStream();
		mInputStream = communicationWithServerSocket.getInputStream();		
	}
}
