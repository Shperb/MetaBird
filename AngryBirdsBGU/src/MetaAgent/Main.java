package MetaAgent;

import AlgorithmTester.AlgorithmTesterDynamicProgramming;
import DB.DBHandler;
import DB.Data;
import DB.Queries;

public class Main {

	public static void main(String[] args) {
		try {
            String ip;
            if (args.length == 1){
                ip = args[0];
            }
            else{
                ip = Constants.serverIp;
            }
			
			new ExtensionLevelsDistributionAgent(6000, Constants.LIST_OF_AGENTS.split(",")).start(Constants.serverPort,ip);
			
//			new AlgorithmTesterDynamicProgramming(600).test(100);
			
			
//			Data data = DBHandler.loadData();
//			new Queries().getLevelResults(data );
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
