package MetaAgent;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import external.ClientMessageTable;

public class MyConnectionToClient extends MyConnection {

	public MyConnectionToClient(String pExecutableFileName, ServerSocket pServerSocket) throws IOException {
		String dir = "c:/temp/" + pExecutableFileName;
		Runtime.getRuntime().exec("cmd /c start " + dir + "/" + pExecutableFileName + ".bat", null, new File(dir));
		System.out.println("Waiting for a client " + pExecutableFileName + " ...");
		Socket socket = pServerSocket.accept();
		socket.setSoTimeout(20000);
		System.out.println("Client accepted");
		mInputStream = socket.getInputStream();
		mOutputStream = socket.getOutputStream();
	}

	@Override
	protected String getMessageType(byte[] b) {
		return "Message type: " + ClientMessageTable.getValue(b[0]).name() + ".";
	}

	@Override
	byte[] readMessage() throws ClientConnectionException {
		try {
			return super.readMessage();
		} catch (Exception e) {
			MyLogger.log(e);
			e.printStackTrace();
			throw new ClientConnectionException();
		}
	}

	@Override
	public int read() throws IOException, ClientConnectionException {
		try {
			return super.read();
		} catch (Exception e) {
			MyLogger.log(e);
			e.printStackTrace();
			throw new ClientConnectionException();
		}
	}

	@Override
	public int read(byte[] b) throws IOException, ClientConnectionException {
		try {
			return super.read(b);
		} catch (Exception e) {
			MyLogger.log(e);
			e.printStackTrace();
			throw new ClientConnectionException();
		}
	}

	@Override
	byte[] readMessage(long pLength) throws IOException, ClientConnectionException {
		try {
			return super.readMessage(pLength);
		} catch (Exception e) {
			MyLogger.log(e);
			e.printStackTrace();
			throw new ClientConnectionException();
		}
	}

	@Override
	public void write(byte[] message) throws IOException, ClientConnectionException {
		try {
			super.write(message);
		} catch (Exception e) {
			MyLogger.log(e);
			e.printStackTrace();
			throw new ClientConnectionException();
		}
	}
}
