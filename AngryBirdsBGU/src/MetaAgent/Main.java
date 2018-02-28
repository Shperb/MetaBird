package MetaAgent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import com.google.gson.JsonSyntaxException;

import learningDistributions.DistributionExtraction;
import AlgorithmTester.AlgorithmTesterDynamicProgramming;
import AlgorithmTester.AlgorithmTesterDynamicProgrammingBinned;
import AlgorithmTester.AlgorithmTesterRoundRobinGreedy;
import AlgorithmTester.AlgorithmTesterRoundRobinSingleAgent;
import AlgorithmTester.AlgorithmTesterScoreGreedy;
import AlgorithmTester.AlgorithmTesterRateGreedy;
import AlgorithmTester.AlgorithmTesterRandom;
import DB.DBHandler;
import DB.Data;
import DB.Queries;
import DB.ValueExtractor.ValueExtractorScore;
import DB.ValueExtractor.ValueExtractorTimeTaken;

public class Main {

	public static void main(String[] args) {
		try {
			//compareIndependentAlgorithms();
			
			compateDistributions();


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

	private static void compateDistributions() throws JsonSyntaxException, IOException {
		DistributionExtraction de = new DistributionExtraction(new ArrayList<>(Arrays.asList("planA,naive,AngryBER,ihsev".split(","))));
		List<Double> test = new ArrayList();
		test.add((double) 5);
		 test.add((double) 376);
		 test.add((double) 219);
		 test.add((double) 396);
		 test.add((double) 738);
		 test.add(3.64325E-05);
		 test.add((double) 89);
		 test.add((double) 12);
		 test.add((double) 13);
		 test.add((double) 27);
		 test.add((double) 3);
		 test.add((double) 2);
		 test.add((double) 1);
		 test.add((double) 5);
		 test.add((double) 1);
		 test.add((double) 3);
		 test.add((double) 0);
		 test.add((double) 0);
		 test.add((double) 1);
		de.computeDistanceFromEachLevel(test);
	}

	static void compareIndependentAlgorithms() throws Exception {
		File folder = new File(Constants.levelsDir);
		File[] listOfFiles = folder.listFiles();
		long seed = 953808075;
		Random generator = new Random(seed);
		String[] levelsBank = new String[20];
		for (int i=0; i<20; i++) {
			levelsBank[i] = listOfFiles[i*20].toPath().getFileName().toString().replace(".json", "");
		}
		
		for (int levelNumber = 2; levelNumber<= 4; levelNumber++){
			for (int timeConstraint = 200; timeConstraint <= 1000; timeConstraint = timeConstraint+200){
				for (int j = 1; j <= 50; j++){
					Problem problem = new Problem();
					problem.agents = new ArrayList<>(Arrays.asList("planA,naive,AngryBER,ihsev".split(",")));
					problem.timeConstraint = timeConstraint;

					HashSet<String> levels = new HashSet<>();
					while(levels.size() < levelNumber) {
						levels.add(levelsBank[(int) (generator.nextDouble() * levelsBank.length)]);
					}

					problem.levels =new ArrayList<>(levels);  //new ArrayList<>(Arrays.asList("Level8-1,Levelcherryblossom-4,Level159,Level5-2".split(",")));
					String[] additionalData = {null};
					int numberOfRepetition = 10000;
					if (levelNumber <4 || timeConstraint < 600){
						new AlgorithmTesterDynamicProgramming(problem).test(numberOfRepetition, additionalData);
						new AlgorithmTesterDynamicProgrammingBinned(problem, 1000, 1).test(numberOfRepetition, additionalData);
						new AlgorithmTesterDynamicProgrammingBinned(problem, 1, 10).test(numberOfRepetition, additionalData);
						new AlgorithmTesterDynamicProgrammingBinned(problem, 1, 25).test(numberOfRepetition, additionalData);
						new AlgorithmTesterDynamicProgrammingBinned(problem, 10000, 1).test(numberOfRepetition, additionalData);
						new AlgorithmTesterDynamicProgrammingBinned(problem, 1000, 10).test(numberOfRepetition, additionalData);
					}
					new AlgorithmTesterDynamicProgrammingBinned(problem, 10000, 10).test(numberOfRepetition, additionalData);
					new AlgorithmTesterScoreGreedy(problem,true).test(numberOfRepetition, additionalData);
					new AlgorithmTesterScoreGreedy(problem,false).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRateGreedy(problem,true).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRateGreedy(problem,false).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRoundRobinGreedy(problem).test(numberOfRepetition, additionalData);
					new AlgorithmTesterRandom(problem).test(numberOfRepetition, additionalData);
					for (String agent : problem.agents){
						new AlgorithmTesterRoundRobinSingleAgent(problem,agent).test(numberOfRepetition, additionalData);
					}
					//			System.out.println("levels: " + String.join(",", problem.levels) + ". average score = " + score + ". " + additionalData);
				}
			}
		}
		
	}
}


