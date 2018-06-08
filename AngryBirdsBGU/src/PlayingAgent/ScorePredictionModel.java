package PlayingAgent;

import DB.Features;

public class ScorePredictionModel {
    private static final String pythonLoaderPath = "python/"
    private static ScorePredictionModel instance;
    static{
        try{
            instance = new ScorePredictionModel();
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    private ScorePredictionModel(){

    }

    public static ScorePredictionModel getInstance(){
        if(instance == null){
            instance = new ScorePredictionModel();
        }
        return instance;
    }

    public double[] predict(String agent, Features levelFeatures){
        // TODO: Take the features, parse into input vector and use the model to predict output vector
        return new double[]{0.4, 0.1, 0.2, 0.1, 0.1, 0.1};
    }
}
