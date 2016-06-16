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

public class Client {

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
	private Thread listenToVisualizer;
	private String newestMessage;
	private Object newestMessageLock;
	private ClientModel clientModel;
	private LevelUpVisualizer sjv;
	private PropertyChangeSupport pcs;
	private List<String> queueIn;
	private List<String> queueOut;
	private boolean running;
	
	public Client(String clientName, String serverIP, int serverPort) {
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
		viz.setVisible(true);
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
			pcs = clientModel.getPCS();
			sjv = new LevelUpVisualizer(clientModel);
			newestMessage = "";
			newestMessageLock = new Object();
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
			listenToVisualizer = new Thread(){
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
			sendToServer.start();
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
		//System.out.println("client:runJob:" + clientName + " jobName "+ jobName);
		if(jobName.contains("Played:")){
			if(Integer.parseInt(jobName.substring(0, 1)) != clientModel.getMyPlayerNum()){
				if(!clientModel.setHand(Integer.parseInt(jobName.substring(0, 1)), jobName)){
					System.out.println("ERRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRROOOOOOOOOOOOOORRRRRRRRRRRRRRRRRRRRRRRRRRRRR");
				}
			}
		}
		if(jobName.contains("Check:")){
			clientModel.checkHighest(jobName);
		}
		if(jobName.contains("Play:")){ //untested
			synchronized(queueOut){
				queueOut.add(jobName);
				queueOut.notifyAll();
			}
		}
		if(jobName.contains("start trick")){//
			pcs.firePropertyChange("TrickStart", false, true);
			System.out.println("clientAI:starttrick"  + clientName + " " + jobName);
		}
		else if(jobName.equals("Round over")){
			clientModel.nextRound();
		}
		else if(jobName.contains("Your turn to play")){
			pcs.firePropertyChange("ourTurnToPlay", false, true);
		}
		else if(jobName.equals("Your turn to draw")){
			pcs.firePropertyChange("needToDraw", false, true);
		}
		else if(jobName.equals("Ready for draw")){
			synchronized(queueOut){
				queueOut.add("Ready for draw");
				queueOut.notifyAll();	
			}
		}
		else if(jobName.contains("Deal:")){
			clientModel.setHand(Integer.parseInt(jobName.substring(5)));
		}
		else if(jobName.contains("Bottom:")){
			clientModel.setHand(Integer.parseInt(jobName.substring(jobName.indexOf(':') + 1)));
		}
		else if(jobName.equals("bottom is set")){
			pcs.firePropertyChange("selectCardCall", false, true);
			for(int i = 0; i < clientModel.gameTypes[clientModel.gameType][1]; i++){
				queueOut.add(i + "SetBottom:" + clientModel.getBottom(i));
			}
			for(int i = 0; i < clientModel.getHandSize(); i++){
				System.out.println((serverPort - 3000) + "card " + i + " = " + clientModel.getCardName(clientModel.getHand(i)) + " " + clientModel.getHand(i));
			}
			for(int i = 0; i < clientModel.getBottom().length; i++){
				System.out.println((serverPort - 3000) + "bottom " + i + " = " + clientModel.getCardName(clientModel.getBottom(i)));
			}
			synchronized(queueOut){
				queueOut.add(clientModel.getMyPlayerNum() + "Ready to play");
				queueOut.notifyAll();	
			}
		}
		else if(jobName.equals("Finished dealing bottom")){
			pcs.firePropertyChange("selectBottom", false, true);
		}
		else if(jobName.contains("card added") ){
			if(clientModel.cardsInHand() == clientModel.gameTypes[clientModel.gameType][2]){
				clientModel.setDealFinished(true);
				if(clientModel.getChampion() == clientModel.getMyPlayerNum()){//we're champion
					queueOut.add("Ready to receive bottom");
					synchronized(queueOut){
						queueOut.notifyAll();	
					}
				}
				else{
					clientModel.sortHand();
					for(int i = 0; i < clientModel.getHandSize(); i++){
						System.out.println((serverPort - 3000) + "card " + i + " = " + clientModel.getCardName(clientModel.getHand(i)) + " " + clientModel.getHand(i));
					}
					queueOut.add(clientModel.getMyPlayerNum() + "Ready to play");
					synchronized(queueOut){
						queueOut.notifyAll();	
					}
				}
			}
		}
		else if(jobName.contains("Flip:")){ //handle both flip and outflip
			synchronized(queueOut){
				queueOut.add(jobName);
				queueOut.notifyAll();	
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
		else if(jobName.contains("Message:")){
			synchronized(queueOut){
				queueOut.add(jobName);
				queueOut.notifyAll();
			}
		}
		else if(jobName.contains("Message from player") && jobName.contains(":")){
			jobName = jobName.replace("::", ",");
			pcs.firePropertyChange(jobName, false, true);
		}
	}
}

