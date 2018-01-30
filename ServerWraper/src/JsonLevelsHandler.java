import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class JsonLevelsHandler {

	String processName = "Fiddler.exe";
	String headerName = "C:/temp/Ai-birds/header.txt";
	String outputName = "C:/temp/Ai-birds/generatedResponse#.dat";
	String currentLength = "3160";

	public void copy(String[] pLevels) {
		try {
//			if (isProcessRunning(processName)) {
//
//				killProcess(processName);
//			}
//			Thread.currentThread().sleep(1000);
			
//			File folder = new File("C:/temp/Ai-birds/Levels");
//			File[] listOfFiles = folder.listFiles();

//			HashSet<String> selectedLevels = new HashSet<>();
//			while (selectedLevels.size() < 8) {
//				int rnd = (int) (Math.random() * listOfFiles.length);
//				String level = listOfFiles[rnd].getPath();
//				if (!selectedLevels.contains(level)) {
//					selectedLevels.add(level);
//				}
//			}
			
			List<String> selectedLevels = Arrays.asList(pLevels); 
			
			Iterator<String> levelsIter = selectedLevels.iterator();
			int i=1;
			while (levelsIter.hasNext()) {
				String level = new String(Files.readAllBytes(Paths.get(Constants.levelsDir + "/" + levelsIter.next() + ".json")), StandardCharsets.UTF_8);
				String header = new String(Files.readAllBytes(Paths.get(headerName)), StandardCharsets.UTF_8);
				header = header.replaceFirst(currentLength, level.length() + "");
				try (Writer writer = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(outputName.replaceAll("#", ""+i)), "utf-8"))) {
					writer.write(header + level);
				}
				i++;
			}
//			Runtime.getRuntime().exec("C:/Users/Noam/AppData/Local/Programs/Fiddler/Fiddler.exe");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static final String TASKLIST = "tasklist";
	private static final String KILL = "taskkill /F /IM ";

	public static boolean isProcessRunning(String serviceName) throws Exception {

		Process p = Runtime.getRuntime().exec(TASKLIST);
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {

			System.out.println(line);
			if (line.contains(serviceName)) {
				return true;
			}
		}

		return false;

	}

	public static void killProcess(String serviceName) throws Exception {

		Runtime.getRuntime().exec(KILL + serviceName);

	}

}
