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
		//long seed = 754682329;
		long seed = 953808075;
		//long seed = 934208575;
		Random generator = new Random(seed);
		DistributionExtraction de = new DistributionExtraction(new ArrayList<>(Arrays.asList("planA,naive,AngryBER,ihsev".split(","))));
		//DistributionExtraction de = new DistributionExtraction(new ArrayList<>(Arrays.asList("planA,naive,AngryBER,ihsev".split(","))),
		//		new ArrayList<>(Arrays.asList("Level1-3,Level1-4,Level5-13,Level1-16,Level5-1,Level5-14,Level3-15,Level6-10,Level4-4,Level6-4,Level5-4,Level5-21,Level5-7,Level8-14,Level5-3,Level2-9,Level4-9,Level6-1,Level8-1,Level3-10".split(","))));
		//de.sumKolmogorovDistanceForAllLevels();
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
					

					
					
				
					
					//new AlgorithmTesterScoreGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getRealScoreDistribution(),de.getRealTimeDistribution(),true).test(numberOfRepetition, additionalData);
					//new AlgorithmTesterScoreGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getRealScoreDistribution(),de.getRealTimeDistribution(),false).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getRealScoreDistribution(),de.getRealTimeDistribution(),true).test(numberOfRepetition, additionalData);
					//new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getRealScoreDistribution(),de.getRealTimeDistribution(),false).test(numberOfRepetition, additionalData);
					//new AlgorithmTesterRoundRobinGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getRealScoreDistribution(),de.getRealTimeDistribution()).test(numberOfRepetition, additionalData);
					//new AlgorithmTesterRandom(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getRealScoreDistribution(),de.getRealTimeDistribution()).test(numberOfRepetition, additionalData);
					//for (String agent : problem.agents){
					//	new AlgorithmTesterRoundRobinSingleAgent(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getRealScoreDistribution(),de.getRealTimeDistribution(),agent).test(numberOfRepetition, additionalData);
					//}
					
					
					
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
					
					
					//new AlgorithmTesterScoreGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(),de.getPolicyTimeDistribution(),true).test(numberOfRepetition, additionalData);
					//new AlgorithmTesterScoreGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(),de.getPolicyTimeDistribution(),false).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(de.getLevels().size(),0),de.getPolicyTimeDistribution(de.getLevels().size(),0),true).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(de.getLevels().size(),0.2),de.getPolicyTimeDistribution(de.getLevels().size(),0.2),true).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(de.getLevels().size(),0.4),de.getPolicyTimeDistribution(de.getLevels().size(),0.4),true).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(de.getLevels().size(),0.6),de.getPolicyTimeDistribution(de.getLevels().size(),0.6),true).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(de.getLevels().size(),0.8),de.getPolicyTimeDistribution(de.getLevels().size(),0.8),true).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(de.getLevels().size(),1),de.getPolicyTimeDistribution(de.getLevels().size(),1),true).test(numberOfRepetition, additionalData);

					//new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(),de.getPolicyTimeDistribution(),false).test(numberOfRepetition, additionalData);
					//new AlgorithmTesterRoundRobinGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(),de.getPolicyTimeDistribution()).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getBinnedPolicyScoreDistribution(de.getLevels().size(),0),de.getBinnedPolicyTimeDistribution(de.getLevels().size(),0),true).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getBinnedPolicyScoreDistribution(de.getLevels().size(),0.2),de.getBinnedPolicyTimeDistribution(de.getLevels().size(),0.2),true).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getBinnedPolicyScoreDistribution(de.getLevels().size(),0.4),de.getBinnedPolicyTimeDistribution(de.getLevels().size(),0.4),true).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getBinnedPolicyScoreDistribution(de.getLevels().size(),0.6),de.getBinnedPolicyTimeDistribution(de.getLevels().size(),0.6),true).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getBinnedPolicyScoreDistribution(de.getLevels().size(),0.8),de.getBinnedPolicyTimeDistribution(de.getLevels().size(),0.8),true).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getBinnedPolicyScoreDistribution(de.getLevels().size(),1),de.getBinnedPolicyTimeDistribution(de.getLevels().size(),1),true).test(numberOfRepetition, additionalData);

					new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(1,0),de.getPolicyTimeDistribution(1,0),true).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(5,0),de.getPolicyTimeDistribution(5,0),true).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(10,0),de.getPolicyTimeDistribution(10,0),true).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRateGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(20,0),de.getPolicyTimeDistribution(20,0),true).test(numberOfRepetition, additionalData);

					//new AlgorithmTesterScoreGreedy(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getBinnedPolicyScoreDistribution(),de.getBinnedPolicyTimeDistribution(),true).test(numberOfRepetition, additionalData);
					
					//new AlgorithmTesterRandom(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(),de.getPolicyTimeDistribution()).test(numberOfRepetition, additionalData);
					//for (String agent : problem.agents){
					//	new AlgorithmTesterRoundRobinSingleAgent(problem,de.getRealScoreDistribution(),de.getRealTimeDistribution(),de.getPolicyScoreDistribution(),de.getPolicyTimeDistribution(),agent).test(numberOfRepetition, additionalData);
					//}
				}
			}
		}
		
	}
}


