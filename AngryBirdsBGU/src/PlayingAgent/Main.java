package PlayingAgent;

import MetaAgent.EndGameException;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.json.simple.JSONObject;

import java.io.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String[] pAgents = {
                "planA",
                "naive",
                "AngryBER",
                "ihsev"
        };

        int pTimeConstraint = 1200;

        try {
            new PlayingAgent(pTimeConstraint, pAgents).start();
        } catch (EndGameException e) {
            System.out.println("*********************************************************************");
            System.out.println("Total Score of metaAgent is: " + e.getGameResult().getTotalScore());
            e.getGameResult().getLevelScores().forEach(
                    (l, score) ->
                            System.out.println(String.format("Score for level %d is: %d", l, score))
            );
            System.out.println("*********************************************************************");
        }
    }
}
