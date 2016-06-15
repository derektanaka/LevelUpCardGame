package levelup;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JPanel;
import javax.swing.JButton;

import java.awt.CardLayout;

public class LevelUpVisualizer extends JFrame implements KeyListener, MouseListener, ActionListener{
	
	private ClientModel clientModel;
	private PropertyChangeSupport pcs;
	private JFrame frame;
	private JPanel panel;
	private int width;
	private int height;
	private int numPlayers;
	private int playerNum;
	private String playerNames[];
	private int[] lvls;
	private int roundsPlayed;
	private int pointsWonByOpposition;
	private ArrayList<Integer> hand;
	private int[] pointsWon;
	private int trumpNumber;
	private int trumpSuit;
	private Thread pcsListener;
	private Thread runJobs;
	private List<String> queueIn;
	private JLabel[] levelsUI;
	private boolean running;
	private JButton button1;
	private JButton button2;
	private JPanel cards;
	private JPanel playRestrictions;
	private JPanel[] otherPlays;
	private JTextField messageBox;
	private ArrayList<String> allMessages = new ArrayList<String>();
	private JList<String> messagesReceived;
	private JScrollPane messageScrollPane;
	private JPanel pointTotal;
	private ArrayList<JToggleButton> cardList = new ArrayList<JToggleButton>();
	private ArrayList<JToggleButton> currentPlays = new ArrayList<JToggleButton>();
	private int cardWidth = 71;
	private int cardHeight = 96;
	private JPanel trumpNumAndSuit;
	private JPanel currentWinner;
	private ArrayList<JToggleButton> winningCards = new ArrayList<JToggleButton>();
	
	/**
	 * @wbp.parser.constructor
	 */
	public LevelUpVisualizer(ClientModel clientModel){
		this.clientModel = clientModel;
		pcs = clientModel.getPCS();
		queueIn = new LinkedList<String>();
		levelsUI = new JLabel[clientModel.getNumPlayer()];
		running = true;
		pcsListener = new Thread(){
			public void run(){
				pcs.addPropertyChangeListener(new PropertyChangeListener(){
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						synchronized(queueIn){
							queueIn.add(evt.getPropertyName());
							queueIn.notifyAll();
						}
					}
				});
			}
		};
		pcsListener.start();
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
		frame = new JFrame();
		
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		width = gd.getDisplayMode().getWidth();
		height = gd.getDisplayMode().getHeight();
		
		panel = new JPanel();
		frame.getContentPane().add(panel);
		
		//ArrayList<JToggleButton> cardList = new ArrayList<JToggleButton>();
		otherPlays = new JPanel[clientModel.getNumPlayer()];
		ArrayList<JPanel> listOfStuff = new ArrayList<JPanel>();
		
		trumpNumAndSuit = new JPanel();
		listOfStuff.add(trumpNumAndSuit);
		trumpNumAndSuit.setName("trumpNumAndSuit");
		trumpNumAndSuit.setBounds(0, 0, 100, 100);
		
		JPanel cardPointsWon = new JPanel();
		cardPointsWon.setName("cardPointsWon");
		listOfStuff.add(cardPointsWon);
		cardPointsWon.setBounds(100, 0, 500, 100);
		
		pointTotal = new JPanel();
		pointTotal.setName("pointTotal");
		listOfStuff.add(pointTotal);
		pointTotal.setBounds(600, 0, 100, 100);
		
		JPanel roundNum = new JPanel();
		roundNum.setName("roundNum");
		listOfStuff.add(roundNum);
		roundNum.setBounds(700, 0, 100, 100);
		
		JPanel lvls = new JPanel(new GridLayout(5,2));
		
		for(int i = 0; i < 5; i++){
			JLabel playerScore = new JLabel("Player " + (i+1));
			playerScore.setBorder(BorderFactory.createLineBorder(Color.black));
			playerScore.setHorizontalAlignment(SwingConstants.CENTER);
			JLabel label = new JLabel("" + clientModel.getLevel(i));
			label.setBorder(BorderFactory.createLineBorder(Color.black));
			label.setHorizontalAlignment(SwingConstants.CENTER);
			levelsUI[i] = label;
			lvls.add(playerScore);
			lvls.add(label);
		}
		
		cards = new JPanel();
		cards.setBorder(BorderFactory.createLineBorder(Color.black));
		panel.add(cards);
		cards.setBounds(0, 500, 700, 200);
		cards.setLayout(null);
		
		lvls.setBounds(0, 100, 200, 400);
		
		panel.add(lvls);
		
		playRestrictions = new JPanel();
		playRestrictions.setName("playRestrictions");
		listOfStuff.add(playRestrictions);
		playRestrictions.setBounds(200, 400, 400, 100);
		
		JPanel messages = new JPanel();
		listOfStuff.add(messages);
		messages.setBounds(600, 400, 200, 100);
		
		messageBox = new JTextField();
		messageBox.setName("messageBox");
		messageBox.addKeyListener(new KeyListener(){

			@Override
			public void keyPressed(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_ENTER){
					pcs.firePropertyChange("Message:" + messageBox.getText(), false, true); // read in the client, send to the server, get server to receive and send to all players
					messageBox.setText("");
				}
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				// FIXME Auto-generated method stub
				
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				// FIXME Auto-generated method stub
				
			}
		});
		panel.add(messageBox);
		messageBox.setBounds(600, 400, 142, 20);
		
		messagesReceived = new JList<String>();
		messageScrollPane = new JScrollPane(messagesReceived,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		messageScrollPane.setSize(200, 80);
		messageScrollPane.setLocation(600, 420);
		
		//messageScrollPane.setBounds(x, y, width, height);
		panel.add(messageScrollPane);
		
		JButton sendMessage = new JButton("Send");
		sendMessage.setBounds(737, 400, 63, 19);
		sendMessage.addActionListener(this);
		panel.add(sendMessage);
		
		currentWinner = new JPanel();
		currentWinner.setName("currentWinner");
		listOfStuff.add(currentWinner);
		currentWinner.setBounds(700, 100, 100, 300);
		
		button1 = new JButton();
		button1.setName("button1");
		button1.setText("Draw");
		button1.addActionListener(this);
		panel.add(button1);
		button1.setBounds(700, 500, 100, 50);
		
		button2 = new JButton();
		button2.setName("button2");
		button2.setText("Flip");
		button2.addActionListener(this);
		panel.add(button2);
		button2.setBounds(700, 550, 100, 50);
		
		for(int i = 0; i < 5; i++){
			JPanel player = new JPanel();
			player.setName("Player " + (i+1));
			listOfStuff.add(player);
			otherPlays[i] = player;
			player.setBounds(200+(i*100), 100, 100, 300);
		}
		
		for(JPanel stuff : listOfStuff){
			stuff.add(new JLabel(stuff.getName()));
			stuff.setBorder(BorderFactory.createLineBorder(Color.black));
			panel.add(stuff);
		}
		
		panel.setLayout(null);
		
		/*
		panel.add(trumpNumAndSuit);
		panel.add(cardPointsWon);
		panel.add(pointTotal);
		panel.add(roundNum);
		panel.add(cards);
		panel.add(lvls);
		panel.add(playRestrictions);
		*/
		
		//frame.setExtendedState(MAXIMIZED_BOTH); //max size
		frame.setLocation(200, 0);
		frame.setUndecorated(true); //no borders, fullscreen effect
		//frame.setSize(800, 600);
		frame.setSize(800, 700);
		frame.addKeyListener(this);
		frame.addMouseListener(this);
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	/*
	 * need to listen for clicks on cards
	 * 
	 */
	@Override
	public void mouseClicked(MouseEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// FIXME Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// FIXME Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// FIXME Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// FIXME Auto-generated method stub
		
	}

	/*
	 * do i need this?
	 */
	@Override
	public void keyPressed(KeyEvent arg0) {
		switch(arg0.getKeyCode()){
		case KeyEvent.VK_ESCAPE: frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)); break;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// FIXME Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// FIXME Auto-generated method stub
		
	}
	
	/*
	 * plays
	 * flips
	 * bottom
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		//System.out.println("SJV:actionPerformed: " + arg0.getActionCommand());
		if(arg0.getActionCommand() == "Draw"){
			pcs.firePropertyChange("Ready for draw", false, true);
		}
		else if(arg0.getActionCommand() == "Play"){
			ArrayList<Integer> playing = new ArrayList<Integer>();
			for(int i = 0; i < cardList.size(); i++){
				if(cardList.get(i).isSelected()){
					playing.add(Integer.parseInt(cardList.get(i).getToolTipText()));
					cardList.get(i).setSelected(false);
					cards.remove(cardList.get(i));
					cardList.remove(cardList.get(i));
					i--;
				}
			}
			String s = "Play:";
			for(Integer card : playing){
				s += card + "/";
			}
			panel.repaint();
			if(!clientModel.setHand(clientModel.getMyPlayerNum(), playing)){
				for(int i = 0; i < playing.size(); i++){
					JToggleButton jtb = new JToggleButton();
					jtb.setIcon(clientModel.getIcon(playing.get(i)));
					cards.add(jtb);
					cardList.add(jtb);
				}
			}
			reorganizeHand();
			panel.repaint();
			pcs.firePropertyChange(s, false, true);
			clientModel.checkHandsAreFull();
		}
		else if(arg0.getActionCommand() == "CallCard"){
			pcs.firePropertyChange("Calling:" + messageBox.getText(), false, true);
		}
		else if(arg0.getActionCommand() == "Flip"){
			ArrayList<Integer> cardsToFlip = new ArrayList<Integer>();
			int flippingType = 0;
			for(JToggleButton card : cardList){
				if(card.isSelected()){
					flippingType++;
					cardsToFlip.add(Integer.parseInt(card.getToolTipText()));
					card.setSelected(false);
				}
			}
			if(flippingType != 0){
				if(flippingType == 2 && cardsToFlip.get(0)/clientModel.gameTypes[clientModel.gameType][6] == cardsToFlip.get(1)/clientModel.gameTypes[clientModel.gameType][6]){ //outflip
					pcs.firePropertyChange(cardsToFlip.get(0) + "OutFlip:" + cardsToFlip.get(1), false, true);
				}
				else if(flippingType == 1){
					pcs.firePropertyChange("Flip:" + cardsToFlip.get(0), false, true);
				}
			}
		}
		else if(arg0.getActionCommand() == "Bottom"){
			ArrayList<Integer> bottom = new ArrayList<Integer>();
			for(int i = 0; i < cardList.size(); i++){
				if(cardList.get(i).isSelected()){
					bottom.add(Integer.parseInt(cardList.get(i).getToolTipText()));
					cardList.get(i).setSelected(false);
					cards.remove(cardList.get(i));
					cardList.remove(cardList.get(i));
					i--;
				}
			}
			for(int i = 0; i < bottom.size(); i++){
				clientModel.setBottom(bottom.get(i), i);
			}
			
			pcs.firePropertyChange("bottom is set", false, true);
			playRestrictions.setVisible(true);
			reorganizeHand();
			button1.setText("Play");
			button2.setText("Not Highest");
			for(JPanel panel : otherPlays){
				panel.setVisible(true);
			}
		}
		else if(arg0.getActionCommand() == "Send"){
			pcs.firePropertyChange("Message:" + messageBox.getText(), false, true); // read in the client, send to the server, get server to receive and send to all players
			messageBox.setText("");
		}
	}
	
	/*
	 * **"Levels" + champion + ":" + levels[champion]
	 * **"Levels" + championPartner + ":" + levels[championPartner]
	 * **"Levels" + i + ":" + levels[i]
	 * **"ChangeChampion" + champion
	 * "NextRound"
	 * **"SetCurrentPlay" + player + ":" + card
	 * "ClearHands"
	 * **"card added" + card
	 * "ChampionPartner" + championPartner
	 * "TrickStart"
	 * "addPoints" + currentWinner + ":" + pointsInPlay
	 * "ourTurnToPlay"
	 * "needToDraw"
	 * "selectBottom"
	 * "selectCardCall"
	 * "Calling:"
	 * "DealFinished"
	 */
	
	//diamonds is longest at 8 across
	public void runJob(String jobName){
		//System.out.println("SJV:runJob: " + jobName);
		if(jobName.contains("NextRound")){ // reset everything
			for(int i = 0; i < currentPlays.size(); i++){
				panel.remove(currentPlays.get(i));
			}
			button1.setText("Draw");
			button2.setText("Flip");
			currentWinner.setVisible(true);
		}
		else if(jobName.equals("selectCardCall")){
			button1.setText("CallCard");
		}
		else if(jobName.equals("CardFlipped")){
			trumpNumAndSuit.removeAll();
			JLabel icon = new JLabel();
			icon.setIcon(clientModel.getIcon(clientModel.getCardFlipped()));
			trumpNumAndSuit.add(icon);
		}
		else if(jobName.equals("ClearHands")){
			for(int i = 0; i < currentPlays.size(); i++){
				panel.remove(currentPlays.get(i));
			}
			playRestrictions.removeAll();
			playRestrictions.add(new JLabel("Play Restrictions"));
		}
		else if(jobName.contains("addPoints")){
			pointTotal.removeAll();
			pointTotal.add(new JLabel("" + clientModel.getPointsWonByOpposition()));
			pointTotal.repaint();
		}
		else if(jobName.equals("TrickStart")){
			playRestrictions.removeAll();
			for(int i = 0; i < currentPlays.size(); i++){
				panel.remove(currentPlays.get(i));
			}
			playRestrictions.add(new JLabel("Your turn to start the trick"));
		}
		else if(jobName.equals("ourTurnToPlay")){
			playRestrictions.removeAll();
			playRestrictions.add(new JLabel("Your turn to play"));
		}
		else if(jobName.contains("Levels")){
			levelsUI[Integer.parseInt(jobName.substring(jobName.indexOf('s') + 1, jobName.indexOf(':')))].setText(jobName.substring(jobName.indexOf(':') + 1));
		}
		else if(jobName.contains("ChangeChampion")){ //2 asterisks for champion
			int champion = Integer.parseInt(jobName.substring(jobName.indexOf('o') + 2));
			for(int i = 0; i < clientModel.getNumPlayer(); i++){
				levelsUI[i].setText(levelsUI[i].getText().replace("*", ""));
			}
			levelsUI[champion].setText(levelsUI[champion].getText() + "**");
		}
		else if(jobName.contains("ChampionPartner")){ //1 asterisk for champion partner
			int championPartner = Integer.parseInt(jobName.substring(jobName.indexOf('e') + 2));
			levelsUI[championPartner].setText(levelsUI[championPartner].getText() + "*");
		}
		else if(jobName.contains("DealFinished")){
			if(clientModel.getMyPlayerNum() == clientModel.getChampion()){
				button1.setText("Bottom");
			}
			else{
				button1.setText("Play");
				button2.setText("Not Highest");
				reorganizeHand();
			}
		}
		else if(jobName.contains("card added")){
			//System.out.println(cardList.size());
			if(cardList.size() < clientModel.getHandSize()){
				//System.out.println("SJV:runJob:card added: " + jobName + " " + cardList.size() + " " + (cardWidth*(cardList.size()%10))+ " " +  (500 + cardHeight*(cardList.size()/10)));
				int card = Integer.parseInt(jobName.substring(10));
				JToggleButton cardButton = new JToggleButton(clientModel.getCardName(card));
				cardButton.setIcon(clientModel.getIcon(card));
				cardButton.setToolTipText("" + card);
				//cardButton.setFont(new Font("Courier", Font.PLAIN, 6));
				//JToggleButton cardButton = new JToggleButton("" + card);
				cardButton.setBounds(cardWidth*((cardList.size())%10), cardHeight*((cardList.size())/10), cardWidth, cardHeight);// x,y, width, height
				cards.add(cardButton);
				cardList.add(cardButton);
				cards.repaint();
			}
			else{ //bottom
				System.out.println("SJV:runJob:card added:bottom " + (200 + cardWidth*((cardList.size()-20)%4)) + " " + (300 + cardHeight*((cardList.size()-20)/4)));
				int card = Integer.parseInt(jobName.substring(10));
				JToggleButton cardButton = new JToggleButton("" + card);
				cardButton.setIcon(clientModel.getIcon(card));
				cardButton.setToolTipText("" + card);
				for(JPanel panel : otherPlays){
					panel.setVisible(false);
				}
				cardButton.setBounds(200 + cardWidth*((cardList.size()-20)%4), 300 + cardHeight*((cardList.size()-20)/4), cardWidth, cardHeight);// x,y, width, height
				cardList.add(cardButton);
				panel.add(cardButton);
				cardButton.setVisible(true);
				playRestrictions.setVisible(false);
				panel.repaint();
			}
		}
		else if(jobName.contains("SetCurrentPlay")){ //"SetCurrentPlay" + player + ":" + card
			int player = Integer.parseInt(jobName.substring(jobName.indexOf('y') + 1));
			ArrayList<Integer> play = clientModel.getCurrentPlays(player);
			try{
				for(int i = 0; i < play.size(); i++){
					JToggleButton cardImage = new JToggleButton("" + play.get(i));
					cardImage.setIcon(clientModel.getIcon(play.get(i)));
					cardImage.setBounds(200 + (100 * player), 100 + (i * cardHeight), cardWidth, cardHeight);
					otherPlays[player].setVisible(false);
					currentPlays.add(cardImage);
					panel.add(cardImage);
					panel.repaint();
				}
			}catch(Exception e){
			}
		}
		else if(jobName.equals("CurrentWinnerChange")){ //pcs.firePropertyChange("CurrentWinnerChange", false, true);
			try{
				for(int i = 0; i < winningCards.size(); i++){
					panel.remove(winningCards.get(i));
				}
			}catch(ArrayIndexOutOfBoundsException e){
				System.out.println("Cant remove");
				e.printStackTrace();
			}
			winningCards.clear();
			for(int i = 0; i < clientModel.getCurrentPlays(clientModel.getCurrentWinner()).size(); i++){
				JToggleButton cardImage = new JToggleButton("" + clientModel.getCurrentPlays(clientModel.getCurrentWinner()).get(i));
				cardImage.setIcon(clientModel.getIcon(clientModel.getCurrentPlays(clientModel.getCurrentWinner()).get(i)));
				cardImage.setName("CurrentWinner");
				cardImage.setBounds(700, 100 + (i * cardHeight), cardWidth, cardHeight);
				currentWinner.setVisible(false);
				currentPlays.add(cardImage);
				winningCards.add(cardImage);
				panel.add(cardImage);
				panel.repaint();
			}
		}
		else if(jobName.contains("Message from player")){
			jobName = jobName.replace("Message from ", "");
			jobName = jobName.replace(",", ": ");
			allMessages.add(jobName);
			String[] temptemp = new String[allMessages.size()];
			for(int i = 0; i < allMessages.size(); i++){
				temptemp[i] = allMessages.get(i);
				System.out.println(temptemp[i]);
			}
			messagesReceived.setListData(temptemp);
			messagesReceived.repaint();
			JScrollBar vertical = messageScrollPane.getVerticalScrollBar();
			vertical.setValue(vertical.getMaximum());
		}
	}
	
	public void reorganizeHand(){
		cards.removeAll();
		for(int i = 0; i < cardList.size(); i++){
			cardList.get(i).setName(clientModel.getCardName(clientModel.getHand(i)));
			cardList.get(i).setIcon(clientModel.getIcon(clientModel.getHand(i)));
			cardList.get(i).setToolTipText("" + clientModel.getHand(i));
			cardList.get(i).setBounds(cardWidth*((cards.getComponentCount())%10), cardHeight*((cards.getComponentCount())/10), cardWidth, cardHeight);
			cards.add(cardList.get(i));
		}
		cards.repaint();
		panel.repaint();
	}
	
	public static void main(String args[]){
		LevelUpVisualizer sjv = new LevelUpVisualizer(new ClientModel(5, 3));
	}
}
