package levelup;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Server {
	
	private int[] port;
	private Socket[] s;
	private DataOutputStream[] out;
	private DataInputStream[] in;
	/*
	private final PrintStreamPanel fromServer, toServer, transcript;
	private final PrintStream ps;
	*/
	private ServerModel serverModel;
	private String[] newestMessage;
	private Object[] newestMessageLock;
	private List<String>[] queueIn;
	private List<String>[] queueOut;
	private List<String> queueSelf;
	private boolean running;
	private int needInputFromThisPlayer;
	private Object needInputFromThisPlayerLock;
	private Object queueOutAllLock;
	
	@SuppressWarnings("unchecked")
	public Server(int[] port){
		Random r = new Random();
		needInputFromThisPlayer = r.nextInt(5);
		needInputFromThisPlayerLock = new Object();
		queueOutAllLock = new Object();
		this.port = port;
		this.s = new Socket[port.length];
		this.out = new DataOutputStream[port.length];
		this.in = new DataInputStream[port.length];
		/*
		this.fromServer = new PrintStreamPanel(Color.WHITE, "From Server",170);
		this.toServer   = new PrintStreamPanel(Color.YELLOW, "To Server",170);
		this.transcript = new PrintStreamPanel(Color.GREEN, "Transcript",380);
		this.ps = transcript.getPrintStream();
		StreamsAndTranscriptViz viz = new StreamsAndTranscriptViz(
				"Dirk server", fromServer, toServer, transcript);
		viz.setVisible(true);
		*/
		running = true;
		this.queueIn = new LinkedList[port.length];
		this.queueOut = new LinkedList[port.length];
		this.queueSelf = new LinkedList<String>();
	}
	
	public void run(){
		Thread[] acceptServer = new Thread[port.length];
		try{
			serverModel = new ServerModel(port.length);
			newestMessage = new String[s.length];
			newestMessageLock = new Object[s.length];
			for(int i = 0; i < s.length; i++){
				final int q = i;
				acceptServer[i] = new Thread(){
					public void run(){
						try {
							ServerSocket serverSocket = new ServerSocket(port[q]);
							s[q] = serverSocket.accept();
							serverSocket.close();
							in[q] = new DataInputStream(s[q].getInputStream());
							out[q] = new DataOutputStream(s[q].getOutputStream());
							out[q].writeByte(serverModel.getNumPlayer());
							out[q].writeByte(q);
							newestMessage[q] = "";
							newestMessageLock[q] = new Object();
						} catch (IOException e) {
							// FIXME Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
				acceptServer[i].start();
			}
			for(int i = 0; i < s.length; i++){
				acceptServer[i].join();
			}
			
			Thread listenToSelf = new Thread(){
				public void run(){
					while(running){
						String temp = "";
						synchronized(queueSelf){
							while(queueSelf.size() < 1){
								try {
									queueSelf.wait();
								} catch (InterruptedException e) {
								}
							}
							temp = queueSelf.get(0);
							queueSelf.remove(0);
						}
						runJob(temp, -1);
					}
				}
			};
			listenToSelf.start();
			
			for(int i = 0; i < s.length; i++){
				final int q = i;
				queueIn[q] = new LinkedList<String>();
				queueOut[q] = new LinkedList<String>();
				
				Thread runJobs = new Thread(){
					public void run(){
						while(running){
							String temp = "";
							synchronized(queueIn[q]){
								while(queueIn[q].size() < 1){
									try {
										queueIn[q].wait();
									} catch (InterruptedException e) {
									}
								}
								temp = queueIn[q].get(0);
								queueIn[q].remove(0);
								runJob(temp, q);
							}
						}
					}
				};
				runJobs.start();
				Thread sendToClient = new Thread(){
					public void run(){
						try {
							while(running){
								synchronized(queueOut[q]){
									while(queueOut[q].size() < 1){
										queueOut[q].wait();
									}
									out[q].writeUTF(queueOut[q].get(0));
									queueOut[q].remove(0);
								}
							}
						} catch (InterruptedException | IOException e) {
							e.printStackTrace();
						}
					}
				};
				sendToClient.start();
				Thread listenToClient = new Thread(){
					public void run(){
						try {
							while(running){
								queueIn[q].add(in[q].readUTF());
								synchronized(queueIn[q]){
									queueIn[q].notifyAll();	
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				};
				listenToClient.start();
			}
			
			//deal
			//serverModel.shuffle();
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void runJob(String jobName, int client) {
		//dealing
		//System.out.println("Server: runJob " + jobName + " from client: " + client);
		if(client == -1){ //from queueSelf
			if(jobName.equals("Deal is over") || jobName.equals("Check everyone ready")){
				synchronized(queueOut[serverModel.getChampion()]){
					if(!serverModel.getBottomSet()){
						serverModel.setBottomSet(true);
						serverModel.setHand(serverModel.getChampion(), serverModel.getBottom()); // put the bottom in the champion's hand in serverModel
						for(int i = 0; i < serverModel.getBottom().length; i++){
							queueOut[serverModel.getChampion()].add(i + "Bottom:" +serverModel.getBottom()[i]); //giving the champion client the bottom
						}
						queueOut[serverModel.getChampion()].add("Finished dealing bottom");
						queueOut[serverModel.getChampion()].notifyAll();
					}
				}
				if(serverModel.getAllPlayersReady()){
					synchronized(queueSelf){
						queueSelf.add("trick over");
						queueSelf.notifyAll();
					}
				}
			}
			if(jobName.equals("trick over")){
				if(serverModel.totalCardsLeft() < 1){
					//next round
					synchronized(queueOutAllLock){
						for(int i = 0; i < queueOut.length; i++){
							synchronized(queueOut[i]){
								queueOut[i].add("Round over");
								queueOut[i].notifyAll();
							}
						}
					}
					System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					serverModel.startNextRound();
				}
				else{
					synchronized(needInputFromThisPlayerLock){
						synchronized(queueOut[needInputFromThisPlayer]){
							queueOut[needInputFromThisPlayer].add("start trick");
							serverModel.setTrickItterator(0);
							queueOut[needInputFromThisPlayer].notifyAll();
						}
					}
				}
				
			}
		}
		
		synchronized(needInputFromThisPlayerLock){//for everything that requires sequenced input or from a specific player (drawing, playing)
			if(client == needInputFromThisPlayer){
				if(jobName.equals("Ready for draw") && !serverModel.getDealFinished()){
					needInputFromThisPlayer = (needInputFromThisPlayer+1)%5;
					synchronized(queueOut[client]){
						queueOut[client].add("Deal:" + serverModel.getCardAsString(client, serverModel.getPositionInDeal()/port.length));
						queueOut[client].notifyAll();
					}
					synchronized(serverModel){
						serverModel.incrementPositionInDeal();
					}
					if(serverModel.getPositionInDeal() < serverModel.gameTypes[serverModel.gameType][0]){
						queueOut[needInputFromThisPlayer].add("Your turn to draw");
						synchronized(queueOut[needInputFromThisPlayer]){
							queueOut[needInputFromThisPlayer].notifyAll();
						}
					}
					else{
						synchronized(queueSelf){
							queueSelf.add("Deal is over");
							serverModel.setDealFinished(true);
							queueSelf.notifyAll();
							needInputFromThisPlayer = serverModel.getChampion();
						}
					}
				}
				else if(jobName.contains("Play:")){
					try{
						//Thread.sleep(1000);
					}
					catch(Exception e){
					}
					if(serverModel.setHand(client, jobName)){
						if(serverModel.getCurrentPlays(client).get(0) == -1){ //need to check highest
							String cardsToBeat = serverModel.getCardsToBeat();
							for(int i = 0; i < queueOut.length; i++){
								//Check:length: + l1 + card + "/"
								//pair: + card + "/"
								//single: + card + "/"
								synchronized(queueOut[i]){
									queueOut[i].add("Check:" + cardsToBeat);
									queueOut[i].notifyAll();
								}
								serverModel.setTrickItterator(serverModel.getTrickItterator() + 1);
								needInputFromThisPlayer = (needInputFromThisPlayer+1)%5;
							}
						}
						else{
							synchronized(queueOutAllLock){
								for(int i = 0; i < queueOut.length; i++){
									synchronized(queueOut[i]){
										queueOut[i].add(client + "Played:" + jobName.substring(jobName.indexOf(':')+1));
										queueOut[i].notifyAll();
									}
								}
							}
							serverModel.setTrickItterator(serverModel.getTrickItterator() + 1);
							needInputFromThisPlayer = (needInputFromThisPlayer+1)%5;
							if(serverModel.getTrickItterator() == 5){
								serverModel.setTrickItterator(0);
								needInputFromThisPlayer = serverModel.trickWinner();
								synchronized(queueSelf){
									queueSelf.add("trick over");
									queueSelf.notifyAll();
								}
							}
							else{
								synchronized(queueOut[needInputFromThisPlayer]){
									queueOut[needInputFromThisPlayer].add("Your turn to play" + serverModel.getCurrentSuit());
									queueOut[needInputFromThisPlayer].notifyAll();
								}
							}
						}
					}
					else{
						System.out.println("Server:IllegalPlay:" + jobName + " client: " + client);
						synchronized(queueOut[needInputFromThisPlayer]){
							queueOut[needInputFromThisPlayer].add("Illegal :Your turn to play" + serverModel.getCurrentSuit());
							queueOut[needInputFromThisPlayer].notifyAll();
						}
					}
				}
			}
		}
		if(client== serverModel.getChampion()){//input from champion client
			/*
			if(jobName.equals("Ready to receive bottom")){ //input from champion client
				synchronized(queueOut[serverModel.getChampion()]){
					serverModel.setHand(serverModel.getChampion(), serverModel.getBottom()); // put the bottom in the champion's hand in serverModel
					for(int i = 0; i < serverModel.getBottom().length; i++){
						queueOut[serverModel.getChampion()].add(i + "Bottom:" +serverModel.getBottom()[i]); //giving the champion client the bottom
					}
					queueOut[serverModel.getChampion()].add("Finished dealing bottom");
					queueOut[serverModel.getChampion()].notifyAll();
				}
			}
			*/
			if(jobName.contains("SetBottom:")){ //getting the bottom from the champion client
				int card = Integer.parseInt(jobName.substring(jobName.indexOf(':') + 1));
				if(serverModel.cardChecker(serverModel.getChampion(), card)){ // check to make sure the champion has the card
					serverModel.setBottom(card, Integer.parseInt(jobName.substring(0, jobName.indexOf('S')))); // set the bottom
					serverModel.removeCard(serverModel.getChampion(), card);  // remove the bottomed card from the champion's hand
				}
			}
			if(jobName.contains("Finished setting bottom")){
				//serverModel.printChampionHand();
			}
			if(jobName.contains("Calling:")){
				System.out.println("Server:Calling:" + jobName);
				jobName = jobName.substring(jobName.indexOf(':') + 1);
				serverModel.setCardCall(Integer.parseInt(jobName.substring(jobName.indexOf(':') + 1)));
				synchronized(queueOutAllLock){
					for(int i = 0; i < serverModel.getNumPlayer(); i++){
						 //giving the champion client the bottom
						synchronized(queueOut[i]){
							queueOut[i].add(jobName);
							queueOut[i].notifyAll();	
						}
					}
				}
			}
		}
		if(jobName.contains("OutFlip:")){
			System.out.println("OutFlip!!!!!!!!!!!!!!!!!");
			String card = jobName.substring(0, jobName.indexOf('O'));
			jobName = jobName.substring(jobName.indexOf(':') + 1);
			int card1 = Integer.parseInt(card);
			int card2 = Integer.parseInt(jobName);
			synchronized(serverModel){
				if(serverModel.isFlipable(card1) && serverModel.cardChecker(client, card1) && card1 > serverModel.getFlipStrength()){
					if(serverModel.isFlipable(card2) && serverModel.cardChecker(client, card2)){
						serverModel.setTrumpSuit(card1);
						if(serverModel.getRound() == 1){
							serverModel.setChampion(client);
						}
						synchronized(queueOutAllLock){
							for(int i = 0; i < s.length; i++){
								synchronized(queueOut[i]){
									queueOut[i].add(card1 + "Player" + client + "outFlip" + card2);
									queueOut[i].notifyAll();
								}
							}
						}
						System.out.println("Player " + client + " " + " outflipped with " + serverModel.getCardName(card1) + " " + serverModel.getCardName(card2));
					}
				}
			}
		}
		//regular flip
		else if(jobName.contains("Flip:") && serverModel.getChampionFlip()== 0){
			int card = Integer.parseInt(jobName.substring(5));
			synchronized(serverModel){
				if(serverModel.isFlipable(card) && serverModel.cardChecker(client, card)){
					serverModel.setTrumpSuit(card);
					serverModel.setChampionFlip(1);
					if(serverModel.getRound() == 1){
						serverModel.setChampion(client);
					}
					synchronized(queueOutAllLock){
						for(int i = 0; i < s.length; i++){
							synchronized(queueOut[i]){
								queueOut[i].add("Player" + client + "flipped" + card);
								queueOut[i].notifyAll();
							}
						}
					}
					System.out.println("Player " + client + " flipped " + serverModel.getCardName(card));
				}
			}
		}
		//outflip
		else if(jobName.contains("Ready to play")){
			serverModel.setPlayerReady(Integer.parseInt(jobName.substring(0, jobName.indexOf('R'))), true);
			synchronized(queueSelf){
				queueSelf.add("Check everyone ready");
				queueSelf.notifyAll();
			}
		}
		else if(jobName.contains("Check:")){ //highest check
			if(jobName.contains("Ok")){
				serverModel.highestOk();
			}
			serverModel.beatHighest(jobName);
		}
		else if(jobName.contains("Message")){
			String s = "Message from player " + client + ":" + jobName.substring(jobName.indexOf('g') + 2);
			for(int i = 0; i < serverModel.getNumPlayer(); i++){
				if(i != client){
					synchronized(queueOut[i]){
						queueOut[i].add(s);
						queueOut[i].notifyAll();
					}
				}
			}
		}
	}
}
//lab computer 
//using InetAddress.getLocalHost() : URB218-19/172.16.2.99
//using my IP Address : 128.252.20.177

//at home
//using InetAddress.getLocalHost() : Derek/172.17.16.106
//using my IP Address : localhost/127.0.0.1

/*
try {
System.out.println(InetAddress.getLocalHost());
System.out.println(InetAddress.getLoopbackAddress());
} catch (UnknownHostException e) {
e.printStackTrace();
}
*/