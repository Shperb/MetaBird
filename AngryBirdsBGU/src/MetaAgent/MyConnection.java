package MetaAgent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MyConnection {
	
	protected OutputStream mOutputStream;
	protected InputStream mInputStream;
	
	byte[] readMessage() throws IOException, ClientConnectionException {
		return readMessage(0);
	}
	
	byte[] readMessage(long pLength) throws IOException, ClientConnectionException {
		logEnter();
		ArrayList<byte[]> arr = new ArrayList<>();
		ArrayList<Integer> sizes = new ArrayList<>();
		int totalMessageSize = 0;
		int lastReadLength = 0;
		boolean done = false;
		while (!done) {
			byte[] byteArray = new byte[2048];
			int nBytes = mInputStream.read(byteArray);
			if (nBytes == -1)
				break;
			totalMessageSize += nBytes;
			sizes.add(nBytes);
			arr.add(byteArray);
			if (pLength == 0) {
				done = mInputStream.available() == 0;
			}
			else {
				done = totalMessageSize == pLength;
			}
		}

		byte[] message = new byte[totalMessageSize];
		int position = 0;
		for (int i=0; i<arr.size(); i++) {
			try {
				System.arraycopy(arr.get(i), 0, message, position, sizes.get(i));
			}
			catch (Exception e) {
				e.printStackTrace(System.out);
			}
			position += sizes.get(i);
		}

		System.arraycopy(arr.get(arr.size() - 1), 0, message, position, lastReadLength);
		logExitRead(message);
		return message;
	}
	
	public void write(byte[] message) throws IOException, ClientConnectionException {
		logEnter();
		mOutputStream.write(message);
		mOutputStream.flush();		
		logExit(message);
	}
	
	public int read(byte[] b) throws IOException, ClientConnectionException {
		logEnter();
		int retVal = mInputStream.read(b);
		logExit(b);
		return retVal;
	}
	
	public int read() throws IOException, ClientConnectionException {
		logEnter();
		int retVal = mInputStream.read();
		logExit(new byte[]{(byte)retVal});
		return retVal;		
	}


	private void logExit(byte[] b) {
		//MyLogger.log(this.getClass().getSimpleName() + " exit " + MyLogger.byteArrayPrefix(b, 10) , 1);
	}


	private void logEnter() {
		//MyLogger.log(this.getClass().getSimpleName() + " enter", 1);
	}
	
	void logExitRead(byte[] b) {
		//MyLogger.log(this.getClass().getSimpleName() + " exit. " + getMessageType(b) + " size: " + b.length +". message: " + MyLogger.byteArrayPrefix(b, 10) , 1);
	}

	protected String getMessageType(byte[] b) {
		return "";
	}
	
}
