package PlayingAgent;

import DB.Features;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TimePredictionModel {

    private static final String configurationPath = "configuration/TimePredictionConstants.json";
    private static TimePredictionModel instance;
    static {
        try {
            instance = new TimePredictionModel();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private JsonObject agentsStats;

    private TimePredictionModel() throws IOException {
        initFromConfiguration();
    }

    public static TimePredictionModel getInstance() {
        return instance;
    }

    public TimeDistribution predict(String agent, Features levelFeatures) {
        int numBirds = (int) levelFeatures.numBirds;
        return new TimeDistribution(numBirds * getMean(agent), numBirds * getStd(agent));
    }

    private void initFromConfiguration() throws IOException {
        String jsonString = readConfiguration();

        JsonReader jsonReader = new JsonReader(new StringReader(jsonString));
        jsonReader.setLenient(true);
        JsonParser parser = new JsonParser();

        this.agentsStats = parser.parse(jsonReader).getAsJsonObject();
    }

    private double getMean(String agent) {
        return this.agentsStats.getAsJsonObject(agent).get("mu").getAsDouble();
    }

    private double getStd(String agent) {
        return this.agentsStats.getAsJsonObject(agent).get("sigma").getAsDouble();
    }

    private static String readConfiguration() throws IOException {
        return new String(Files.readAllBytes(Paths.get(configurationPath)));
    }
}
