package DB;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class DBHandler {
	private static Object lock = new Object();
	
	public static Data loadData() throws JsonSyntaxException, IOException {
		if (getFile().exists()) {
			String json = readFile(getFile().toPath(), StandardCharsets.UTF_8);
			return new Gson().fromJson(json, Data.class);
		}
		else {
			return new Data();
		}
	}

	private static String readFile(Path path, Charset encoding) throws IOException {
		byte[] encoded;
		synchronized (lock) {
			encoded = Files.readAllBytes((path));
		}
		return new String(encoded, encoding);
	}

	public static void save(Data pData) throws IOException {
		synchronized (lock) {
			String json = new Gson().toJson(pData);
			Files.write(getFile().toPath(), json.getBytes());
		}
	}

	private static File getFile() throws IOException {
		String dirName = "c:/MetaAgentDB/";
		String FileName = dirName + "data.json";
		File dir = new File(dirName);
		if (!dir.exists()) {
			Files.createDirectories(dir.toPath());
		}
		File file = new File(FileName);
		return file;
	}
}
