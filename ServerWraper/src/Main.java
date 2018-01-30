import java.net.UnknownHostException;

public class Main {
	public static void main(String[] args) throws Exception {
//		new JsonLevelsHandler().copy(new String[]{"C:\\Temp\\Ai-birds\\Levels\\Level1-1.json","C:\\Temp\\Ai-birds\\Levels\\Level1-1.json","C:\\Temp\\Ai-birds\\Levels\\Level1-1.json","C:\\Temp\\Ai-birds\\Levels\\Level1-1.json","C:\\Temp\\Ai-birds\\Levels\\Level1-1.json","C:\\Temp\\Ai-birds\\Levels\\Level1-1.json","C:\\Temp\\Ai-birds\\Levels\\Level1-1.json","C:\\Temp\\Ai-birds\\Levels\\Level1-1.json"});

		new ServerWrap().start();	
//		new JsonLevelsHandler().copy();
//		Proxy proxy = new Proxy(9001);
//		proxy.start();
//		proxy.waitForClients(1);
//		proxy.send(new ProxyReloadMessage());		
//		proxy.waitForClients(1);
//		proxy.send(new ProxyReloadMessage());		
//		proxy.waitForClients(1);
//		proxy.send(new ProxyReloadMessage());		

	}
	
	
	static void reload() throws UnknownHostException {

	}
}
