package MetaAgent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyLogger {
	private static String logFilePath = Constants.logFilePath;
	
	static {
		File file = new File(logFilePath);
		file.delete();
	}
	
	public static void log(String str, int pStackIndex) {
		synchronized (logFilePath) {
			try {
				StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date date = new Date();
				File file = new File(logFilePath);
				if (!file.exists()) {
					file.createNewFile();
				}
				String toWrite = simpleDateFormat.format(date) + " " + stackTraceElements[2 + pStackIndex] + ": " + str + "\n";
				Files.write(file.toPath(), toWrite.getBytes(), StandardOpenOption.APPEND);
			}
			catch(Exception e) {
				System.err.println(e);
			}			
		}
	}
	
	public static void log(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String sStackTrace = sw.toString();
		log(e + ":\n" + sStackTrace, 1);		
	}

	public static String byteArrayPrefix(byte[] pMessage, int pLength) {
		String retVal ="";
		for (int i=0; i<pLength && i<pMessage.length; i++) {
			retVal += pMessage[i] + " ";
		}
		return retVal;
	}

	public static void log(String string) {
		log(string, 1);
	}

}
