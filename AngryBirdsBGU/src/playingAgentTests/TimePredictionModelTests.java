package playingAgentTests;

import DB.Features;
import PlayingAgent.TimePredictionModel;

public class TimePredictionModelTests {
    public static void main(String[] args){
        TimePredictionModel model = TimePredictionModel.getInstance();

        Features oneBird = new Features(), twoBirds = new Features();
        oneBird.numBirds = 1;
        twoBirds.numBirds = 2;
        System.out.println(model.predict("naive", oneBird).getProbabilityBelowValue(80));
        System.out.println(model.predict("naive", twoBirds).getProbabilityBelowValue(80));
    }
}
