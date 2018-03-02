package MetaAgent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import learningDistributions.DistributionExtraction;
import AlgorithmTester.AlgorithmTesterDynamicProgramming;
import AlgorithmTester.AlgorithmTesterDynamicProgrammingBinned;
import AlgorithmTester.AlgorithmTesterRoundRobinGreedy;
import AlgorithmTester.AlgorithmTesterRoundRobinSingleAgent;
import AlgorithmTester.AlgorithmTesterScoreGreedy;
import AlgorithmTester.AlgorithmTesterRateGreedy;
import AlgorithmTester.AlgorithmTesterRandom;

public class Main {

	public static void main(String[] args) {
		try {
			compareIndependentAlgorithms();
			
			//compateDistributions();


			//			new MetaAgentDistributionSampling(600, new String[] {"planA", "naive", "AngryBER", "ihsev"}).start();



			//


			//			Data data = DBHandler.loadData();
			//			new Queries().getLevelsResults(data );
			//			System.out.println("score:");
			//			new Queries().getSupportSize(data, new ValueExtractorScore());
			//			System.out.println("time:");
			//			new Queries().getSupportSize(data, new ValueExtractorTimeTaken());

			//			new Queries().getAgentLevelWinsCount(data);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void compareIndependentAlgorithms() throws Exception {
		long seed = 953808075;
		//long seed = 934208575;
		Random generator = new Random(seed);
		DistributionExtraction de = new DistributionExtraction(new ArrayList<>(Arrays.asList("planA,naive,AngryBER,ihsev".split(","))));
		de.sumKolmogorovDistanceForAllLevels();
		List<String> levelsBank = de.getLevels();
		for (int levelNumber = 2; levelNumber<= 4; levelNumber++){
			for (int timeConstraint = 200; timeConstraint <= 1000; timeConstraint = timeConstraint+200){
				for (int j = 1; j <= 50; j++){
					Problem problem = new Problem();
					problem.agents = new ArrayList<>(Arrays.asList("planA,naive,AngryBER,ihsev".split(",")));
					problem.timeConstraint = timeConstraint;

					HashSet<String> levels = new HashSet<>();
					while(levels.size() < levelNumber) {
						levels.add(levelsBank.get((int) (generator.nextDouble() * levelsBank.size())));
					}

					problem.levels =new ArrayList<>(levels);  //new ArrayList<>(Arrays.asList("Level8-1,Levelcherryblossom-4,Level159,Level5-2".split(",")));
					String[] additionalData = {null};
					int numberOfRepetition = 10000;
					
					//original
					/*
					if (levelNumber <4 || timeConstraint < 600){
						new AlgorithmTesterDynamicProgramming(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getRealScoreDistribution(),de.getRealTimeDistribution()).test(numberOfRepetition, additionalData);
						new AlgorithmTesterDynamicProgrammingBinned(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getRealScoreDistribution(),de.getRealTimeDistribution(), 1000, 1).test(numberOfRepetition, additionalData);
						new AlgorithmTesterDynamicProgrammingBinned(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getRealScoreDistribution(),de.getRealTimeDistribution(), 1, 10).test(numberOfRepetition, additionalData);
						new AlgorithmTesterDynamicProgrammingBinned(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getRealScoreDistribution(),de.getRealTimeDistribution(), 1, 25).test(numberOfRepetition, additionalData);
						new AlgorithmTesterDynamicProgrammingBinned(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getRealScoreDistribution(),de.getRealTimeDistribution(), 10000, 1).test(numberOfRepetition, additionalData);
						new AlgorithmTesterDynamicProgrammingBinned(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getRealScoreDistribution(),de.getRealTimeDistribution(), 1000, 10).test(numberOfRepetition, additionalData);
					}
					new AlgorithmTesterDynamicProgrammingBinned(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getRealScoreDistribution(),de.getRealTimeDistribution(), 10000, 10).test(numberOfRepetition, additionalData);
					*/
					
					new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getRealScoreDistribution(),de.getRealTimeDistribution(),true).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(),de.getPolicyTimeDistribution(),true).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(20),de.getPolicyTimeDistribution(20),true).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(10),de.getPolicyTimeDistribution(10),true).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(5),de.getPolicyTimeDistribution(5),true).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(1),de.getPolicyTimeDistribution(1),true).test(numberOfRepetition, additionalData);

					
					/* here1
					
					new AlgorithmTesterScoreGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getRealScoreDistribution(),de.getRealTimeDistribution(),true).test(numberOfRepetition, additionalData);
					new AlgorithmTesterScoreGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getRealScoreDistribution(),de.getRealTimeDistribution(),false).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getRealScoreDistribution(),de.getRealTimeDistribution(),true).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getRealScoreDistribution(),de.getRealTimeDistribution(),false).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRoundRobinGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getRealScoreDistribution(),de.getRealTimeDistribution()).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRandom(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getRealScoreDistribution(),de.getRealTimeDistribution()).test(numberOfRepetition, additionalData);
					for (String agent : problem.agents){
						new AlgorithmTesterRoundRobinSingleAgent(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getRealScoreDistribution(),de.getRealTimeDistribution(),agent).test(numberOfRepetition, additionalData);
					}
					
					*/
					
					//			System.out.println("levels: " + String.join(",", problem.levels) + ". average score = " + score + ". " + additionalData);
					
					//withLearning
					/*
					if (levelNumber <4 || timeConstraint < 600){
						new AlgorithmTesterDynamicProgramming(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(),de.getPolicyTimeDistribution()).test(numberOfRepetition, additionalData);
						new AlgorithmTesterDynamicProgrammingBinned(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(),de.getPolicyTimeDistribution(), 1000, 1).test(numberOfRepetition, additionalData);
						new AlgorithmTesterDynamicProgrammingBinned(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(),de.getPolicyTimeDistribution(), 1, 10).test(numberOfRepetition, additionalData);
						new AlgorithmTesterDynamicProgrammingBinned(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(),de.getPolicyTimeDistribution(), 1, 25).test(numberOfRepetition, additionalData);
						new AlgorithmTesterDynamicProgrammingBinned(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(),de.getPolicyTimeDistribution(), 10000, 1).test(numberOfRepetition, additionalData);
						new AlgorithmTesterDynamicProgrammingBinned(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(),de.getPolicyTimeDistribution(), 1000, 10).test(numberOfRepetition, additionalData);
					}
					
					new AlgorithmTesterDynamicProgrammingBinned(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(),de.getPolicyTimeDistribution(), 10000, 10).test(numberOfRepetition, additionalData);
					*/
					
					/* here 2
					new AlgorithmTesterScoreGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(),de.getPolicyTimeDistribution(),true).test(numberOfRepetition, additionalData);
					new AlgorithmTesterScoreGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(),de.getPolicyTimeDistribution(),false).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(),de.getPolicyTimeDistribution(),true).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(),de.getPolicyTimeDistribution(),false).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRoundRobinGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(),de.getPolicyTimeDistribution()).test(numberOfRepetition, additionalData);
					
					*/
					//new AlgorithmTesterRandom(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(),de.getPolicyTimeDistribution()).test(numberOfRepetition, additionalData);
					//for (String agent : problem.agents){
					//	new AlgorithmTesterRoundRobinSingleAgent(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(),de.getPolicyTimeDistribution(),agent).test(numberOfRepetition, additionalData);
					//}
				}
			}
		}
		
	}
}


