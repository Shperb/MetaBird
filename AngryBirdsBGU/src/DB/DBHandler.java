package DB;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class DBHandler {
	private static Object lock = new Object();
	private static Object featuresLock = new Object();

	public static Data loadData() throws JsonSyntaxException, IOException {
		if (getDataFile().exists()) {
			String json = readFile(getDataFile().toPath(), StandardCharsets.UTF_8);
			return new Gson().fromJson(json, Data.class);
		}
		else {
			return new Data();
		}
	}

	public static FeaturesData loadFeatures() throws JsonSyntaxException, IOException {
		if (getFeaturesFile().exists()) {
			String json = readFile(getFeaturesFile().toPath(), StandardCharsets.UTF_8);
			return new Gson().fromJson(json, FeaturesData.class);
		}
		else {
			return new FeaturesData();
		}
	}

	private static String readFile(Path path, Charset encoding) throws IOException {
		byte[] encoded;
		synchronized (lock) {
			encoded = Files.readAllBytes((path));
		}
		return new String(encoded, encoding);
	}

	public static void saveData(Data pData) throws IOException {
		synchronized (lock) {
			String json = new Gson().toJson(pData);
			Files.write(getDataFile().toPath(), json.getBytes());
		}
	}

	public static void saveFeatures(String level, Features fData) throws IOException {
		synchronized (lock) {
			FeaturesData oldData = loadFeatures();
			List<Features> levelFeatures = oldData.features.getOrDefault(level, new ArrayList<>());
			levelFeatures.add(fData);
			oldData.features.put(level, levelFeatures);
			String json = new Gson().toJson(oldData);
			Files.write(getFeaturesFile().toPath(), json.getBytes());
		}
	}

	private static File getFeaturesFile() throws IOException {
		return getFile("features.json");
	}

	private static File getDataFile() throws IOException {
		return getFile("data.json");
	}

	private static File getFile(String fileName) throws IOException {
		String dirName = "c:/MetaAgentDB2/";
		String FileName = dirName + fileName;
		File dir = new File(dirName);
		if (!dir.exists()) {
			Files.createDirectories(dir.toPath());
		}
		File file = new File(FileName);
		return file;
	}
}
