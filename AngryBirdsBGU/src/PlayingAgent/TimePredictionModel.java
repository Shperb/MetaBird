package PlayingAgent;

import DB.Features;

public class TimePredictionModel {
    private static TimePredictionModel instance;
    private TimePredictionModel(){
        // TODO: import the model to predict time
    }

    public static TimePredictionModel getInstance(){
        if(instance == null){
            instance = new TimePredictionModel();
        }
        return instance;
    }

    public TimeDistribution predict(String agent, Features levelFeatures){
        // TODO: Calculate the approximated time from the features and the model
        return new TimeDistribution();
    }
}
