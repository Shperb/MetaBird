package MetaAgent;

public class Main {

	public static void main(String[] args) {
		try {
//			new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(new Date().getTime() + 10*60*60*1000));

//			System.out.println(new SimpleDateFormat().format(new Date()));
			
			new MetaAgentDistributionSampling(600).start();
			
//			HashMap<Integer, Long> m1= new HashMap<>();
//			m1.put(3, (long)4);
//			m1.put(3, (long)4);
//			HashMap<Integer, Long> m2= new HashMap<>();
//			m2.put(3, (long)3);
//			m2.put(3, (long)4);
//			
//			System.out.println(m1.equals(m2));
//			for (int h=100; ;h+=20) {
//				long start = System.currentTimeMillis();
//				MetaAgentDynamicProgramming metaAgentDynamicProgramming = new MetaAgentDynamicProgramming(null);
//				Object[] refChoice = new Object[2];
//				Object[] refData = new Object[2];
//				HashMap<Integer, Long> scores = new HashMap<>();
//				for (int i=1; i<=21; i++) {
//					scores.put(i, (long)0);
//				}
//				long val = metaAgentDynamicProgramming.getValue(scores, h, refChoice, refData);
//				System.out.println("horizon(seconds): " + h);
//				System.out.println("val: " + val);
//				System.out.println("choice: " + refChoice[0] + " " + refChoice[1]);
//				long end = System.currentTimeMillis();
//				System.out.println("time: " + (end-start) / 1000 + " seconds");
//				System.out.println("cache entries: " + refData[0]);
//				System.out.println("hit ratio: " + refData[1]);
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
