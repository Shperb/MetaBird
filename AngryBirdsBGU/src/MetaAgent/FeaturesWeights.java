package MetaAgent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import AlgorithmTester.AlgorithmTesterRateGreedy;
import DB.DBHandler;
import DB.Features;
import DB.FeaturesData;
import learningDistributions.DistributionExtraction;

public class FeaturesWeights {
	HashMap<String, Double> mAverageScores = new HashMap<>();
	List<String> mLevelsBank;
	DistributionExtraction mDe;
	
	public void findMetricForLevels() throws Exception {
		FeaturesData featuresData = DBHandler.loadFeatures();
		
		mDe = new DistributionExtraction(new ArrayList<>(Arrays.asList("planA,naive,AngryBER,ihsev".split(","))));

		int numberOfRepetition = 10000;
		Problem problem = new Problem();
		problem.agents = new ArrayList<>(Arrays.asList("planA,naive,AngryBER,ihsev".split(",")));
		problem.timeConstraint = 200;

		mLevelsBank = mDe.getLevels();
		
		AlgorithmTesterRateGreedy algTester = new AlgorithmTesterRateGreedy(problem,mDe.getRealScoreDistribution(),mDe.getRealTimeDistribution(),mDe.getRealScoreDistribution(),mDe.getRealTimeDistribution(),true);
		for (String level : mLevelsBank) {
			double maxScore = featuresData.computeMaxScoreBasedOnFeatures(level);
			problem.levels =new ArrayList<>();
			problem.levels.add(level);
			double avgScore = algTester.getAverageScore(numberOfRepetition);
			mAverageScores.put(level, avgScore / maxScore);
		}
		
		double[] weights = new double[Features.amountOfFeatuers()];
		
		for (int i=0; i<weights.length; i++) {
			weights[i]=1;
		}
		
		
		while (true) {
			ArrayList<double[]> weightsList = generateWeights(weights);
			weights = getBest(weightsList);
			System.out.println(getDistanceFromGoalFunction(weights) + " : " + Arrays.toString(weights));
		}
	}
	
	private double[] getBest(ArrayList<double[]> pWeightsList) {
		double minDistance = getDistanceFromGoalFunction(pWeightsList.iterator().next());
		double[] retVal = pWeightsList.iterator().next();
		for (double[] weights : pWeightsList) {
			double distance = getDistanceFromGoalFunction(weights); 
			if (distance < minDistance) {
				minDistance = distance;
				retVal = weights;
			}
		}
		return retVal;
	}

	private double getDistanceFromGoalFunction(double[] pFeaturesWeights) {
		mDe.setFeaturesWeights(pFeaturesWeights);
		double error = 0;
		for (String level1 : mLevelsBank) {
			for (String level2 : mLevelsBank) {
				Double featuresDiff = mDe.computeDistance(level1, level2);
				double scoresDiff = Math.abs(mAverageScores.get(level1) - mAverageScores.get(level2));
				double desiredScoresDiff = featuresDiff;
				error += Math.pow(desiredScoresDiff - scoresDiff, 2);
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
