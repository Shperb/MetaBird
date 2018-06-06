package MetaAgent;

import AlgorithmTester.AlgorithmTesterDynamicProgramming;
import DB.DBHandler;
import DB.Data;
import DB.Queries;

public class Main {

	public static void main(String[] args) {
		try {

			
			new MetaAgentDistributionSampling(600, new String[] {"planA"
//					, "naive", "AngryBER", "ihsev"
			}).start();
			
//			new AlgorithmTesterDynamicProgramming(600).test(100);
			
			
//			Data data = DBHandler.loadData();
//			new Queries().getLevelResults(data );
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
