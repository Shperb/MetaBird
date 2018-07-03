package MetaAgent;
import java.net.Socket;

public class MyConnectionToServer extends MyConnection{

	private Socket communicationWithServerSocket;

    public MyConnectionToServer(int serverport, String serverIp) throws Exception{
        super();
        communicationWithServerSocket = new Socket(serverIp, serverport);
        mOutputStream = communicationWithServerSocket.getOutputStream();
        mInputStream = communicationWithServerSocket.getInputStream();
    }
}
