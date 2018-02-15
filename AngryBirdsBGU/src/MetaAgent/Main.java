package MetaAgent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import AlgorithmTester.AlgorithmTesterDynamicProgramming;
import AlgorithmTester.AlgorithmTesterDynamicProgrammingBinned;
import AlgorithmTester.AlgorithmTesterGreedy;
import AlgorithmTester.AlgorithmTesterGreedy2;
import AlgorithmTester.AlgorithmTesterRandom;
import DB.DBHandler;
import DB.Data;
import DB.Queries;
import DB.ValueExtractor.ValueExtractorScore;
import DB.ValueExtractor.ValueExtractorTimeTaken;

public class Main {

	public static void main(String[] args) {
		try {

			
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
	
	static void compareAlgorithms() throws Exception {
		File folder = new File(Constants.levelsDir);
		File[] listOfFiles = folder.listFiles();
		
		String[] levelsBank = new String[20];
		for (int i=0; i<20; i++) {
			levelsBank[i] = listOfFiles[i*20].toPath().getFileName().toString().replace(".json", "");;
		}
		
		while(true) {
			Problem problem = new Problem();
			problem.agents = new ArrayList<>(Arrays.asList("planA,naive,AngryBER,ihsev".split(",")));
			problem.timeConstraint = 300;
			
			HashSet<String> levels = new HashSet<>();
			while(levels.size() < 3) {
				levels.add(levelsBank[(int) (Math.random() * levelsBank.length)]);
			}
			
			problem.levels =new ArrayList<>(levels);  //new ArrayList<>(Arrays.asList("Level8-1,Levelcherryblossom-4,Level159,Level5-2".split(",")));
			String[] additionalData = {null};
			new AlgorithmTesterDynamicProgramming(problem).test(100, additionalData);
			new AlgorithmTesterDynamicProgrammingBinned(problem, 1, 10).test(100, additionalData);
			new AlgorithmTesterDynamicProgrammingBinned(problem, 1, 25).test(100, additionalData);
			new AlgorithmTesterDynamicProgrammingBinned(problem, 1000, 1).test(100, additionalData);
			new AlgorithmTesterDynamicProgrammingBinned(problem, 1000, 10).test(100, additionalData);
			new AlgorithmTesterGreedy(problem).test(100, additionalData);
			new AlgorithmTesterGreedy2(problem).test(100, additionalData);
			new AlgorithmTesterRandom(problem).test(100, additionalData);
//			System.out.println("levels: " + String.join(",", problem.levels) + ". average score = " + score + ". " + additionalData);
		}		
	}
}
