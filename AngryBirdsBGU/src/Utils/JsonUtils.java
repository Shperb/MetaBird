package Utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;

public class JsonUtils {


    public static JsonObject parse(String jsonString){
        JsonReader jsonReader = new JsonReader(new StringReader(jsonString));
        jsonReader.setLenient(true);
        JsonParser parser = new JsonParser();

        return parser.parse(jsonReader).getAsJsonObject();
    }

    public static double[] parseToDoubleArray(String jsonString){
        JsonReader jsonReader = new JsonReader(new StringReader(jsonString));
//        jsonReader.setLenient(true);
        JsonParser parser = new JsonParser();

        JsonArray arr = parser.parse(jsonReader).getAsJsonArray();
        double[] ans = new double[arr.size()];
        for (int i = 0; i < ans.length; i++){
            ans[i] = arr.get(i).getAsDouble();
        }

        return ans;
    }
}
