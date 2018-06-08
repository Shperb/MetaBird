package playingAgentTests;

import DB.Features;
import PlayingAgent.ScorePredictionModel;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.json.simple.JSONObject;

import java.io.StringReader;

public class ScorePredictionModelTests {
    public static void main(String[] args) {
//        String agent = "bla";
//        Features levelFeatures = new Features();
//
//        JSONObject agentAndFeatures = new JSONObject();
//        agentAndFeatures.put("agent", agent);
//        agentAndFeatures.put("features", levelFeatures.getFeatureAsList());
//
//        System.out.println(agentAndFeatures.toJSONString());
//
//        String jsonString = "[1, 2, 3]";
//
//        JsonReader jsonReader = new JsonReader(new StringReader(jsonString));
//        jsonReader.setLenient(true);
//        JsonParser parser = new JsonParser();
//
//        JsonArray arr = parser.parse(jsonReader).getAsJsonArray();
//        double[] ans = new double[arr.size()];
//        for (int i = 0; i < ans.length; i++){
//            ans[i] = arr.get(i).getAsDouble();

        ScorePredictionModel sut = ScorePredictionModel.getInstance();
        double[] ans = sut.predict("naive", new Features());
        ans = sut.predict("planA", new Features());
    }


}
