package PlayingAgent;

import DB.Features;

public class ScorePredictionModel {
    private static ScorePredictionModel instance;
    private ScorePredictionModel(){
        // TODO: import the keras model to private member
    }

    public static ScorePredictionModel getInstance(){
        if(instance == null){
            instance = new ScorePredictionModel();
        }
        return instance;
    }

    public double[] predict(String agent, Features levelFeatures){
        // TODO: Take the features, parse into input vector and use the model to predict output vector
        return new double[]{};
    }
}
