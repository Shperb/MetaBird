package PlayingAgent;

import DB.DBHandler;
import DB.Data;
import DB.FeaturesData;
import DB.Queries;
import MetaAgent.*;
import com.google.gson.Gson;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


public class Main {
    public static void main(String[] args) throws Exception {
        String[] pAgents = {
                "planA",
                "naive",
                "AngryBER",
                "ihsev"
        };

        String ip;
        if (args.length == 1){
            ip = args[0];
        }
        else{
            ip = Constants.serverIp;
        }
/*
        Data merged = new Data();
        for (int i=1; i<=6; i++) {
            String json = DBHandler.readFile(new File("c:/temp/json/data" + i + ".json").toPath(),StandardCharsets.UTF_8);
            Data data = new Gson().fromJson(json, Data.class);
            merged.games.addAll(data.games);
        }

        String json = new Gson().toJson(merged);
        Files.write(new File("c:/temp/json/data.json").toPath(), json.getBytes());

        FeaturesData merged = new FeaturesData();
        for (int i=1; i<=6; i++) {
            String json = DBHandler.readFile(new File("c:/temp/json/features" + i + ".json").toPath(),StandardCharsets.UTF_8);
            FeaturesData data = new Gson().fromJson(json, FeaturesData.class);
            merged.features.putAll(data.features);
        }

        String json = new Gson().toJson(merged);
        Files.write(new File("c:/temp/json/features.json").toPath(), json.getBytes());

        Data data = DBHandler.loadData();
        Queries q = new Queries();
        q.getLevelsResults(data);

        FeaturesWeights fw = new FeaturesWeights();
        fw.findMetricForLevels();
*/

        GameResult[] results = new GameResult[pAgents.length + 1];

        int pTimeConstraint = 1200;

    /*
        try{
                new RoundRobinAgent(pTimeConstraint, new String[]{pAgents[0]}).start();
            }
            catch(EndGameException ex) {
                System.out.println("*********************************************************************");
                System.out.println("Total Score of " + pAgents[0] + " is: " + ex.getGameResult().getTotalScore());
                ex.getGameResult().getLevelScores().forEach(
                        (l, score) ->
                                System.out.println(String.format("Score for level %d is: %d", l, score))
                );
                System.out.println("*********************************************************************");
            }
*/
//        for(int i = 0; i < pAgents.length; i++){
//            try{
//                new RoundRobinAgent(pTimeConstraint, new String[]{pAgents[i]}).start();
//            }
//            catch(EndGameException ex){
//                results[i] = ex.getGameResult();
//            }
//        }
//

       try {
          new PerformanceProfilePlayingAgent(pTimeConstraint, pAgents).start(Constants.serverPort, ip);
       } catch (EndGameException e) {
           results[4] = e.getGameResult();
       }

        /*
        try {
            new PlayingAgent(pTimeConstraint, pAgents).start(Constants.serverPort, ip);
        } catch (EndGameException e) {
            results[4] = e.getGameResult();
        }
        */

//        for(int i = 0; i < pAgents.length; i++){
//            System.out.println("*********************************************************************");
//            System.out.println("Total Score of " + pAgents[i] + " is: " + results[i].getTotalScore());
//            results[i].getLevelScores().forEach(
//                    (l, score) ->
//                            System.out.println(String.format("Score for level %d is: %d", l, score))
//            );
//            System.out.println("*********************************************************************");
//        }
//
//        System.out.println("*********************************************************************");
//        System.out.println("Total Score of metaAgent is: " + results[4].getTotalScore());
//        results[4].getLevelScores().forEach(
//                (l, score) ->
//                        System.out.println(String.format("Score for level %d is: %d", l, score))
//        );
//        System.out.println("*********************************************************************");
    }
}
