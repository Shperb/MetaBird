
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MyConnectionToClient extends MyConnection {

	public MyConnectionToClient() throws IOException {
		System.out.println("Waiting for a client..."); 
		ServerSocket serverSocket = new ServerSocket(Constants.clientPort);
		Socket socket = serverSocket .accept();
		serverSocket.close();
		System.out.println("Client accepted");		
		mInputStream = socket.getInputStream();
		mOutputStream = socket.getOutputStream();
	}
}
