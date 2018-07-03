package MetaAgent;

import java.util.*;

import AlgorithmTester.AlgorithmTesterRateGreedy;
import DB.DBHandler;
import DB.Features;
import DB.FeaturesData;
import PlayingAgent.DistributionExtraction;

public class FeaturesWeights {
    HashMap<String,HashMap<String, Double[]>> mAverageScores = new HashMap<>();
    List<String> mLevelsBank;
    DistributionExtraction mDe;

    public void findMetricForLevels() throws Exception {
        FeaturesData featuresData = DBHandler.loadFeatures();

        mDe = new DistributionExtraction(new ArrayList<>(Arrays.asList("planA,naive,AngryBER,ihsev".split(","))));

        int numberOfRepetition = 10000;
        Problem problem = new Problem();
        ArrayList<String> AgentList = new ArrayList<String>(Arrays.asList("planA,naive,AngryBER,ihsev".split(",")));

       // problem.agents = new ArrayList<>(Arrays.asList("planA,naive,AngryBER,ihsev".split(",")));
       // problem.timeConstraint = 200;

        mLevelsBank = mDe.getLevels();

        AlgorithmTesterRateGreedy algTester = new AlgorithmTesterRateGreedy(problem,mDe.getRealScoreDistribution(),mDe.getRealTimeDistribution(),mDe.getRealScoreDistribution(),mDe.getRealTimeDistribution(),true);
        for (String agent : AgentList) {
            problem.agents = new ArrayList<>();
            problem.agents.add(agent);
            HashMap<String, Double[]> levelsScore = new HashMap<String, Double[]>();
            mAverageScores.put(agent,levelsScore);
            for (String level : mLevelsBank) {
                double maxScore = featuresData.computeMaxScoreBasedOnFeatures(level);
                problem.levels = new ArrayList<>();
                problem.levels.add(level);
                Double[] avgScoreTime = new Double[2];
                double[] doubleRes = algTester.getAverageScoreTimeRateOnce(numberOfRepetition);
                avgScoreTime[0] = doubleRes[0];
                avgScoreTime[1] = doubleRes[1];
                avgScoreTime[0] = avgScoreTime[0] / maxScore;
                levelsScore.put(level, avgScoreTime);
            }
        }

        double[] weights = new double[Features.amountOfFeatuers()];

        for (int i=0; i<weights.length; i++) {
            weights[i]=1;
        }


        while (true) {
            ArrayList<double[]> weightsList = generateWeights(weights);
            Set<String> bankForIteration = new HashSet<>();
            int numOfLevels = 50;
            while (bankForIteration.size() < numOfLevels){
                bankForIteration.add(mLevelsBank.get( new Random().nextInt(50)));
            }
            weights = getBest(weightsList,bankForIteration);
            System.out.println(getDistanceFromGoalFunction(weights, bankForIteration) + " : " + Arrays.toString(weights));
        }
    }

    private double[] getBest(ArrayList<double[]> pWeightsList, Set<String> bankForIteration) {
        double minDistance = getDistanceFromGoalFunction(pWeightsList.iterator().next(), bankForIteration);
        double[] retVal = pWeightsList.iterator().next();
        for (double[] weights : pWeightsList) {
            double distance = getDistanceFromGoalFunction(weights,bankForIteration);
            if (distance < minDistance) {
                minDistance = distance;
                retVal = weights;
            }
        }
        return retVal;
    }

    private double getDistanceFromGoalFunction(double[] pFeaturesWeights, Set<String> bankForIteration) {
        mDe.setFeaturesWeights(pFeaturesWeights);
        double error = 0;
        ArrayList<String> AgentList = new ArrayList<String>(Arrays.asList("planA,naive,AngryBER,ihsev".split(",")));
        for (String level1 : bankForIteration) {
            for (String level2 : bankForIteration) {
                for (String agent : AgentList) {
                    Double featuresDiff = mDe.computeDistance(level1, level2);
                    double scoresDiff = Math.abs(mAverageScores.get(agent).get(level1)[0] - mAverageScores.get(agent).get(level2)[0]);
                    double timesDiff = Math.abs(mAverageScores.get(agent).get(level1)[1] - mAverageScores.get(agent).get(level2)[1]);
                    double diff = Math.sqrt(Math.pow(scoresDiff,2) + Math.pow(timesDiff,2));
                    double desiredScoresDiff = featuresDiff;
                    error += Math.pow(desiredScoresDiff - diff, 2);
                }
            }
        }
        return Math.sqrt(error);
    }

    private ArrayList<double[]> generateWeights(double[] pWeights) {
        ArrayList<double[]> retVal = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            double[] weights = Arrays.copyOf(pWeights, pWeights.length);
            mutate(weights);
            retVal.add(weights);
        }
        return retVal;
    }

    private void mutate(double[] pWeights) {
        for (int i = 0; i < pWeights.length; i++) {
            for (int j = 0; j < 10; j++) {
                double mul = 0.8 + Math.random() * 0.4;
                pWeights[i] = pWeights[i] * mul;
            }
        }
    }
}