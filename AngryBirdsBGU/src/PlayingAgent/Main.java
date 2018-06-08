package PlayingAgent;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.json.simple.JSONObject;

import java.io.*;

public class Main {
    public static void main(String[] args) throws Exception {
        new PlayingAgent(600, new String[]{"planA"
////                , "naive", "AngryBER", "ihsev"
        }).start();
    }
}
