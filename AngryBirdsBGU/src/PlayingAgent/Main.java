package PlayingAgent;

import MetaAgent.EndGameException;
import MetaAgent.GameResult;

public class Main {
    public static void main(String[] args) throws Exception {
        String[] pAgents = {
                "planA",
                "naive",
                "AngryBER",
                "ihsev"
        };

        GameResult[] results = new GameResult[pAgents.length + 1];

        int pTimeConstraint = 1200;

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

//        for(int i = 0; i < pAgents.length; i++){
//            try{
//                new RoundRobinAgent(pTimeConstraint, new String[]{pAgents[i]}).start();
//            }
//            catch(EndGameException ex){
//                results[i] = ex.getGameResult();
//            }
//        }
//
//        try {
//            new PlayingAgent(pTimeConstraint, pAgents).start();
//        } catch (EndGameException e) {
//            results[4] = e.getGameResult();
//        }

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
