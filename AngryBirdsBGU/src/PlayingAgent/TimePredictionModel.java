package PlayingAgent;

import DB.Features;
import Utils.JsonUtils;
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

    public LearnedTimeDistribution predict(String agent, Features levelFeatures) {
        int numBirds = (int) levelFeatures.numBirds;
        return new LearnedTimeDistribution(numBirds * getMean(agent), numBirds * getStd(agent));
    }

    private void initFromConfiguration() throws IOException {
        String jsonString = readConfiguration();
        this.agentsStats = JsonUtils.parse(jsonString);
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
