package levelup;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class ClientAI {

	private ArtificialIntelligence AI;
	private String clientName;
	private String serverIP;
	private int serverPort;
	private Socket s;
	/*
	private final PrintStreamPanel fromServer, toServer, transcript;
	private final PrintStream ps;
	*/
	private DataInputStream in;
	private DataOutputStream out;
	private Thread listenToServer;
	private Thread sendToServer;
	private Thread runJobs;
	private ClientModel clientModel;
	private PropertyChangeSupport pcs;
	private List<String> queueIn;
	private List<String> queueOut;
	private boolean running;
	
	public ClientAI(String clientName, String serverIP, int serverPort) {
		running = true;
		this.clientName = clientName + " " + serverPort;
		this.serverIP = serverIP;
		this.serverPort = serverPort;
		/*
		this.fromServer = new PrintStreamPanel(Color.WHITE, "From Server",170);
		this.toServer   = new PrintStreamPanel(Color.YELLOW, "To Server",170);
		this.transcript = new PrintStreamPanel(Color.GREEN, "Transcript",380);
		this.ps       = transcript.getPrintStream();
		StreamsAndTranscriptViz viz = new StreamsAndTranscriptViz(
				clientName,
				fromServer, toServer, transcript);
		viz.setVisible(false);
		*/
		this.queueIn = new LinkedList<String>();
		this.queueOut = new LinkedList<String>();
	}

	// TODO:  implementation
	public void run()  {
		try {
			Socket socket = new Socket(serverIP, serverPort);
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			clientModel = new ClientModel(in.readByte(), in.readByte());
			AI = new ArtificialIntelligence(clientModel, clientModel.getMyPlayerNum());
			pcs = clientModel.getPCS();
			runJobs = new Thread(){
				public void run(){
					while(running){
						String temp = "";
						synchronized(queueIn){
							while(queueIn.size() < 1){
								try {
									queueIn.wait();
								} catch (InterruptedException e) {
								}
							}
							temp = queueIn.get(0);
							queueIn.remove(0);
							//queueIn.notifyAll();
						}
						runJob(temp);
					}
				}
			};
			runJobs.start();
			sendToServer = new Thread(){
				public void run(){
					try {
						while(running){
							synchronized(queueOut){
								while(queueOut.size() < 1){
									queueOut.wait();
								}
								out.writeUTF(queueOut.get(0));
								queueOut.remove(0);
							}
						}
					} catch (InterruptedException | IOException e) {
						e.printStackTrace();
					}
				}
			};
			sendToServer.start();
			queueOut.add("Ready for draw");
			listenToServer = new Thread(){
				public void run(){
					try {
						while(running){
							queueIn.add(in.readUTF());
							synchronized(queueIn){
								queueIn.notifyAll();	
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			listenToServer.start();
			Thread pcsListener = new Thread(){
				public void run(){
					pcs.addPropertyChangeListener(new PropertyChangeListener(){
						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							queueIn.add(evt.getPropertyName());
							synchronized(queueIn){
								queueIn.notifyAll();	
							}
						}
						
					});
				}
			};
			pcsListener.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void runJob(String jobName){
		//System.out.println("clientAI:runJob:" + clientName + " jobName "+ jobName);
		if(jobName.contains("Played:")){
			if(!clientModel.setHand(Integer.parseInt(jobName.substring(0, 1)), jobName)){
				//System.out.println("ERRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRROOOOOOOOOOOOOORRRRRRRRRRRRRRRRRRRRRRRRRRRRR");
			}
		}
		if(jobName.contains("Check:")){
			clientModel.checkHighest(jobName);
		}
		
		if(jobName.contains("start trick")){//
			//System.out.println("clientAI:starttrick"  + clientName + " " + jobName);
			try{
				Thread.sleep(2000);
			}catch(InterruptedException e){
			}
			String temp = AI.getPlay();
			queueOut.add(temp);
			synchronized(queueOut){
				queueOut.notifyAll();	
			}
		}
		else if(jobName.equals("Round over")){
			clientModel.nextRound();
		}
		else if(jobName.contains("Your turn to play")){
			try{
				Thread.sleep(1250);
			}catch(InterruptedException e){
			}
			String temp = AI.getPlay(Integer.parseInt(jobName.substring(jobName.indexOf('y') + 1)));
			System.out.println("ClientAI:your turn to play" + temp);
			queueOut.add(temp);
			synchronized(queueOut){
				queueOut.notifyAll();	
			}
		}
		else if(jobName.equals("Your turn to draw")){
			queueOut.add("Ready for draw");
			synchronized(queueOut){
				queueOut.notifyAll();	
			}
		}
		else if(jobName.contains("Deal:")){
			clientModel.setHand(Integer.parseInt(jobName.substring(5)));
			/*
			if(clientModel.getChampion() == clientModel.getMyPlayerNum() && clientModel.getHandSize() == clientModel.cardsInHand()){//we're champion
				queueOut.add("Ready to receive bottom");
				synchronized(queueOut){
					queueOut.notifyAll();	
				}
			}
			*/
		}
		else if(jobName.contains("Bottom:")){
			clientModel.setHand(Integer.parseInt(jobName.substring(jobName.indexOf(':') + 1)));
		}
		else if(jobName.equals("Finished dealing bottom")){
			int[] bottom = AI.calculateBottom();
			for(int i = 0 ; i < bottom.length; i++){
				queueOut.add(i + "SetBottom:" + bottom[i]);
			}
			queueOut.add(AI.getCardCall());
			queueOut.add(clientModel.getMyPlayerNum() + "Ready to play");
			for(int i = 0; i < clientModel.getHandSize(); i++){
				//System.out.println((serverPort - 3000) + "card " + i + " = " + clientModel.getCardName(clientModel.getHand(i)) + " " + clientModel.getHand(i));
			}
			for(int i = 0; i < clientModel.getBottom().length; i++){
				//System.out.println((serverPort - 3000) + "bottom " + i + " = " + clientModel.getCardName(clientModel.getBottom(i)));
			}
			synchronized(queueOut){
				queueOut.notifyAll();	
			}
		}
		else if(jobName.contains("card added") ){
			if(clientModel.cardsInHand() == clientModel.gameTypes[clientModel.gameType][2]){
				clientModel.setDealFinished(true);
					clientModel.sortHand();
					for(int i = 0; i < clientModel.getHandSize(); i++){
						//System.out.println((serverPort - 3000) + "card " + i + " = " + clientModel.getCardName(clientModel.getHand(i)) + " " + clientModel.getHand(i));
					}
					queueOut.add(clientModel.getMyPlayerNum() + "Ready to play");
					synchronized(queueOut){
						queueOut.notifyAll();	
					}
			}
			int card = Integer.parseInt(jobName.substring(10));
			if(!clientModel.getDealFinished()){
				if(clientModel.canOutFlipSuit(clientModel.getSuit(card)) && clientModel.getChampionFlip() > 0){//outflip
					if(AI.shouldOutFlip(clientModel.getSuit(clientModel.getTrumpSuit()), clientModel.getSuit(card))){
						//System.out.println(clientName + " is outflipping with " + clientModel.getFlippableCard(clientModel.getSuit(card))[0] + " and " + clientModel.getFlippableCard(clientModel.getSuit(card))[1]);
						//System.out.println("flipping because we drew a " + card + " and this makes " + clientModel.getNumPerSuit(clientModel.getSuit(card)) + " " + clientModel.getSuitString(clientModel.getSuit(card)));
						//System.out.println("We would have had " + clientModel.getNumPerSuit(clientModel.getTrumpSuit()));
						queueOut.add(clientModel.getFlippableCard(clientModel.getSuit(card))[0] + "OutFlip:" + clientModel.getFlippableCard(clientModel.getSuit(card))[1]);
						synchronized(queueOut){
							queueOut.notifyAll();	
						}
					}
				}
				else if(clientModel.canFlipSuit(clientModel.getSuit(card))){//flip
					if(AI.shouldFlip(clientModel.getSuit(card))){
						//AI Client + port, the card that caused us to flip, suit, number per suit, 
						//System.out.println(clientName + " flipped because we drew " + card + " which is the " + clientModel.getSuit(card) +  " suit ");
						//System.out.println("current = " + clientModel.getNumPerSuit(clientModel.getSuit(card)) + ", cards in hand = " + clientModel.cardsInHand() + ", expect " + (clientModel.getHandSize() - clientModel.cardsInHand())/4.5 + " more");
						//System.out.println("Overall we expect " + (clientModel.getNumPerSuit(clientModel.getSuit(card)) + (clientModel.getHandSize() - clientModel.cardsInHand())/4.5));
						queueOut.add("Flip:" + clientModel.getFlippableCard(clientModel.getSuit(card))[0]);
						synchronized(queueOut){
							queueOut.notifyAll();	
						}
						//System.out.println(clientName + " Flip:" + clientModel.getFlippableCard(clientModel.getSuit(card))[0] + " " + clientModel.getChampionFlip());
					}
				}
			}
		}
		//listening for a regular flip
		else if(jobName.contains("Player") && jobName.contains("flipped")){ // "Player" + playerNum + "flipped" + card
			if(clientModel.getChampionFlip() < 1){
				int playerWhoLastFlipped = Integer.parseInt(jobName.substring(6, 7));
				int card = Integer.parseInt(jobName.substring(jobName.indexOf('d') + 1));
				if(clientModel.getRound() == 1){
					clientModel.setChampion(playerWhoLastFlipped);
				}
				clientModel.eliminateAllOthers(playerWhoLastFlipped, card);
				clientModel.setTrumpSuit(card, playerWhoLastFlipped);
				clientModel.setChampionFlip(1);
			}
		}
		//listening for an outflip
		else if(jobName.contains("Player") && jobName.contains("outFlip")){// card1 + "Player" + playerNum + "outFlip"+ card2
			int card1 = Integer.parseInt(jobName.substring(0, jobName.indexOf('P')));
			int card2 = Integer.parseInt(jobName.substring(jobName.indexOf('p') + 1));
			int playerWhoLastFlipped = Integer.parseInt(jobName.substring(jobName.indexOf('r')+1, jobName.indexOf('r') + 2));
			synchronized(clientModel){
				if(clientModel.getRound() == 1){
					clientModel.setChampion(playerWhoLastFlipped);
				}
				clientModel.eliminateAllOthers(playerWhoLastFlipped, card1);
				clientModel.eliminateAllOthers(playerWhoLastFlipped, card2);
				clientModel.setTrumpSuit(card1, playerWhoLastFlipped);
				clientModel.setChampionFlip(2);
			}
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws Exception 
	 */
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
		final int numAIClients = 4;
		for(int i = 0; i < 5; i++){
			final int q = i;
			Thread t = new Thread(){
				public void run(){
					try {
						//Thread.sleep(500 * q);
						if(q < numAIClients){
							ClientAI clientServer = new ClientAI("AI Client", "localhost", (3000 + q));
							clientServer.run();
						}
						else{
							Client clientServer = new Client("Client", "localhost", (3000 + q));
							clientServer.run();
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			t.start();
		}
	}
	
}

