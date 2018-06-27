package playingAgentTests;

import PlayingAgent.AgentLevelPrediction;
import PlayingAgent.AgentLevelPredictionLearning;
import PlayingAgent.LearnedTimeDistribution;
import PlayingAgent.TimeDistribution;

public class AgentLevelPredictionTests {
    public static void main(String[] args){
        AgentLevelPrediction sut = new AgentLevelPredictionLearning(4000, new double[]{0.4, 0.1, 0.2, 0.1, 0.1, 0.1},
                new LearnedTimeDistribution(40, 2));

        System.out.println(sut.getScoreTimeRate(200, 200));
        System.out.println(sut.getScoreTimeRate(50, 200));
        System.out.println(sut.getScoreTimeRate(40, 200));
    }
}
