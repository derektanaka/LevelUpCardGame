package levelup;

public class Main {
	public static void main(String[] args)  {
		Thread serverThread = new Thread(){
			public void run(){
				int[] ports = {
						3000, 3001, 3002, 3003, 3004
				};
				Server server = new Server(ports); // lab computer 128.252.20.177 
				server.run();
			}
		};
		serverThread.start();
		int numAIClients = 5;
		for(int i = 0; i < numAIClients; i++){
			final int q = i;
			Thread t = new Thread(){
				public void run(){
					try {
						//Thread.sleep(500 * q);
						ClientAI clientServer = new ClientAI("AI Client", "localhost", (3000 + q));
						clientServer.run();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			t.start();
		}
	}
}
