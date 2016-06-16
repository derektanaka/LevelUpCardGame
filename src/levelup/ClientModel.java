package levelup;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class ClientModel {

	private int myPlayerNum;
	public final int[][] gameTypes = {
			{48, 6, 12, 4, 40, 20 ,1},
			{100, 8, 20, 5, 80, 40, 2},
			{154, 8, 22, 7, 120, 60, 3}};
	public final int gameType;
	public int[] levels;
	private int[] bottom;
	public int champion;
	private ArrayList<Integer> myHand;
	private ArrayList<Integer>[] otherHands;
	private int[] numPerSuit;
	private int cardFlipped;
	private int championFlipOrOutFlip;
	private int trumpSuit;
	private PropertyChangeSupport pcs;
	private boolean dealFinished;
	private int round;
	private int[] consecutivePairLengths;
	private int lastPlayerWhoFlipped;
	private int championPartner;
	private Integer calledCard;
	
	private int typeOfPlay;
	private int suitOfPlay;
	private ArrayList<ArrayList<Integer>> highestTemp;
	private ArrayList<Integer>[] currentPlays;
	private int currentWinner;
	private int pointsInPlay;
	private int[] pointsWonPerPlayer;
	private int pointsWonByOpposition;
	
	public ClientModel(int numPlayers, int myPlayerNum){
		this.myPlayerNum = myPlayerNum;
		int temp = 0;
		for(int i = 0; i < 3; i++){
			if(gameTypes[i][3] == numPlayers){
				temp = i;
			}
		}
		gameType = temp;
		bottom = new int[gameTypes[gameType][1]];
		levels = new int[numPlayers];
		for(int i = 0; i < levels.length; i++){
			levels[i] = 2;
		}
		currentPlays = new ArrayList[gameTypes[gameType][3]];
		pointsWonPerPlayer = new int[gameTypes[gameType][3]];
		highestTemp = new ArrayList<ArrayList<Integer>>();
		round = 1;
		champion = 0;
		championFlipOrOutFlip = 0;
		trumpSuit = 4;
		typeOfPlay= 0;
		cardFlipped = 0;
		pointsWonByOpposition = 0;
		currentWinner = 0;
		calledCard = -1;
		championPartner = champion;
		lastPlayerWhoFlipped = -1;
		dealFinished = false;
		numPerSuit = new int[5];
		for(int i = 0; i < numPerSuit.length; i++){
			numPerSuit[i] = 0;
		}
		otherHands = new ArrayList[numPlayers];
		for(int i = 0; i < otherHands.length; i++){
			pointsWonPerPlayer[i] = 0;
			currentPlays[i] = new ArrayList<Integer>();
			otherHands[i] = new ArrayList<Integer>();
			for(int j = 0; j < gameTypes[gameType][0]+gameTypes[gameType][1]; j++){
				if(i != myPlayerNum){
					otherHands[i].add(j);
				}
			}
		}
		myHand = new ArrayList<Integer>();
		pcs = new PropertyChangeSupport(this);
	}
	
	public void nextRound(){ // should double check this later since copy pasted from serverModel
		round++;
		int levelGain = (pointsWonByOpposition- 80)/40;
		if(levelGain < 0){
			levels[champion] += Math.abs(levelGain);
			pcs.firePropertyChange("Levels" + champion + ":" + levels[champion], false, true);
			levels[championPartner] += Math.abs(levelGain);
			pcs.firePropertyChange("Levels" + championPartner + ":" + levels[championPartner], false, true);
		}
		else if(levelGain >= 0){
			for(int i = 0; i < gameTypes[gameType][3]; i++){
				if(i != champion && i != championPartner){
					levels[i] += levelGain;
					pcs.firePropertyChange("Levels" + i + ":" + levels[i], false, true);
				}
			}
			champion = (champion+1)%5;
			if(champion == championPartner){ //skip championPartner when they lose
				champion = (champion+1)%5;
			}
			championPartner = champion; //reset
			pcs.firePropertyChange("ChangeChampion" + champion, false, true);
		}
		for(int i = 0; i < gameTypes[gameType][3]; i++){
			otherHands[i].clear();
			bottom = new int[gameTypes[gameType][1]];
			championFlipOrOutFlip = 0;
			trumpSuit = -1;
			calledCard = -1;
			championPartner = champion;
			dealFinished = false;
		}
		pointsWonByOpposition = 0;
		pcs.firePropertyChange("NextRound", false, true);
	}
	
	public synchronized void setCurrentPlays(int player, int card){
		checkHandsAreFull();
		currentPlays[player].add(card);
		if((card/gameTypes[gameType][6])%13 == 11 || (card/gameTypes[gameType][6])%13 == 8){ //king or 10
			pointsInPlay += 10;
		}
		else if((card/gameTypes[gameType][6])%13 == 3){ //5
			pointsInPlay += 5;
		}
		pcs.firePropertyChange("SetCurrentPlay" + player, false, true);
		eliminateAll(card);
	}
	
	public int getPointInPlay(){
		return pointsInPlay;
	}
	
	public int getPointsWonPerPlayer(int player){
		return pointsWonPerPlayer[player];
	}
	
	public synchronized void checkHandsAreFull(){
		boolean ans = true;
		for(int i = 0; i < currentPlays.length; i++){
			ans = ans && currentPlays[i].size() > 0;
		}
		if(ans){
			//System.out.println("Clearing all hands");
			pointsWonPerPlayer[currentWinner] += pointsInPlay;
			if(currentWinner != champion && currentWinner != championPartner){
				pointsWonByOpposition += pointsInPlay;
				pcs.firePropertyChange("addPoints" + currentWinner + ":" + pointsInPlay, false, true);
			}
			pointsInPlay = 0;
			for(int i = 0; i < currentPlays.length; i++){
				currentPlays[i].clear();
			}
			pcs.firePropertyChange("ClearHands", false, true);
		}
	}
	
	public synchronized void setCurrentPlays(int player, int[] cards){
		for(int i = 0; i < cards.length; i++){
			setCurrentPlays(player, cards[i]);
		}
	}
	
	public synchronized void setCurrentPlays(int player, ArrayList<Integer> cards){
		for(int i = 0; i < cards.size(); i++){
			setCurrentPlays(player, cards.get(i));
		}
	}
	
	
	public ArrayList<Integer> getCurrentPlays(int player){
		return currentPlays[player];
	}
	
	public int getCurrentWinner(){
		return currentWinner;
	}
	
	public ArrayList<Integer> getHandToBeat(){
		return getCurrentPlays(currentWinner);
	}
	
	public int getHandSize(){
		return gameTypes[gameType][2];
	}
	
	public void setLevel(int player, int level){
		levels[player] = level;
	}
	
	public int getLevel(int player){
		return levels[player];
	}
	
	public void lvlup(int player){
		synchronized(levels){
			levels[player]++;	
		}
	}
	
	public int getPointsWonByOpposition(){
		return pointsWonByOpposition;
	}
	
	public synchronized void setHand(int card){
		myHand.add(card);
		numPerSuit[(card/gameTypes[gameType][6])/13]++;
		pcs.firePropertyChange("card added" + card, false, true);
	}
	
	public int getBottom(int position){
		return bottom[position];
	}
	
	public int[] getBottom(){
		return bottom;
	}
	
	public void setBottom(int card, int position){
		bottom[position] = card;
		eliminateSelf(card);
	}
	
	public synchronized int getHand(int card){
		return myHand.get(card);
	}
	
	public synchronized int getIndexOf(int card){
		return myHand.indexOf(card);
	}
	
	public synchronized ArrayList<Integer> sortHand(){
		Collections.sort(myHand);
		return myHand;
	}
	
	public int getNumPlayer(){
		return gameTypes[gameType][3];
	}
	
	public boolean isFlipable(int card){
		return ((card/gameTypes[gameType][6]))%13 == levels[champion]-2;
	}
	
	public synchronized boolean canFlipSuit(int suit){
		return myHand.contains(suit*13*gameTypes[gameType][6]) || myHand.contains((suit*13*gameTypes[gameType][6]) + 1);
	}
	
	public synchronized boolean canOutFlipSuit(int suit){
		return myPlayerNum != lastPlayerWhoFlipped && myHand.indexOf(suit*13*gameTypes[gameType][6]) != -1 && myHand.indexOf(suit*13*gameTypes[gameType][6] + 1) != -1;
	}
	
	public synchronized int[] getFlippableCard(int suit){
		int[] flippableCards = new int[2];
		int i = 0;
		if(myHand.indexOf(suit*13*gameTypes[gameType][6]) != -1){
			flippableCards[i] = suit*13*gameTypes[gameType][6];
			i++;
		}
		if(myHand.indexOf(suit*13*gameTypes[gameType][6] + 1) != -1){
			flippableCards[i] = suit*13*gameTypes[gameType][6] + 1;
		}
		return flippableCards;
	}
	
	public boolean greaterThan(int card1, int card2){
		return getRank(card1) > getRank(card2);
	}
	
	public int getSuit(int card){
		return (card/gameTypes[gameType][6])/13;
	}
	
	public boolean oneSuitAsTrump(ArrayList<Integer> cards){
		boolean ans = true;
		int suit = getSuitAsTrump(cards.get(0));
		for(int i = 1; i < cards.size(); i++){
			ans = ans && suit == getSuitAsTrump(cards.get(i));
		}
		return ans;
	}
	
	public int getSuitAsTrump(int card){
		if(getSuit(card) == trumpSuit || getRank(card) == 26){
			return 4;
		}
		return getSuit(card);
	}
	
	public int getRank(int card){  
		if(getSuit(card) == trumpSuit && (card/gameTypes[gameType][6])%13 != levels[champion]){
			return 13+(card/gameTypes[gameType][6])%13; //prime
		}
		else if((card/gameTypes[gameType][6])%13 == levels[champion]){
			if(getSuit(card) == trumpSuit){
				return 27; //prime + number
			}
			return 26; // number
		}
		else if(53*gameTypes[gameType][6] - gameTypes[gameType][6] < card && card < 53*gameTypes[gameType][6] - 1){
			return 28; //small joker
		}
		else if(54*gameTypes[gameType][6] - gameTypes[gameType][6] < card && card < 54*gameTypes[gameType][6] - 1){
			return 29; // big joker
		}
		return (card/gameTypes[gameType][6])%13; // everything else
	}
	
	public synchronized int getOffSuitAces(){
		int ans = 0;
		for(int i = 0; i < myHand.size(); i++){
			if(getRank(myHand.get(i)) == 12){
				ans++;
			}
		}
		return ans;
	}
	
	public int getNumPerSuit(int suit){
		return numPerSuit[suit];
	}
	
	public int getTrumpSuit(){
		return trumpSuit;
	}
	
	public int getCardFlipped(){
		return cardFlipped;
	}
	
	public synchronized void setTrumpSuit(int cardFlipped, int lastPlayerWhoFlipped){
		this.cardFlipped = cardFlipped;
		this.trumpSuit = getSuit(cardFlipped);
		this.lastPlayerWhoFlipped = lastPlayerWhoFlipped;
		pcs.firePropertyChange("CardFlipped", false, true);
	}
	
	public int cardsInHand(){
		return myHand.size();
	}
	
	public int handSize(){
		return gameTypes[gameType][2];
	}
	
	public PropertyChangeSupport getPCS(){
		return pcs;
	}
	
	public int getRound(){
		return round;
	}
	
	public void setRound(int round){
		this.round = round;
	}
	
	public boolean getDealFinished(){
		return dealFinished;
	}
	
	public void setDealFinished(boolean dealFinished){
		this.dealFinished = dealFinished;
		myHand = sortHand();
		pcs.firePropertyChange("DealFinished", false, true);
	}
	
	public int getMyPlayerNum(){
		return myPlayerNum;
	}
	
	public int getChampion(){
		return champion;
	}
	
	public void setChampion(int champion){
		this.champion = champion;
		pcs.firePropertyChange("ChangeChampion" + champion, false, true);
		this.championPartner = champion;
	}
	
	public void setChampionFlip(int championFlip){
		this.championFlipOrOutFlip = championFlip;
	}
	
	public int getChampionFlip(){
		return championFlipOrOutFlip;
	}
	
	public String getTrumpSuitString(){
		return getSuitString(trumpSuit);
	}
	
	public String getSuitString(int suit){
		String suitName = "";
		switch(suit){
		case 0: suitName += "Spades"; break;
		case 1: suitName += "Hearts"; break;
		case 2: suitName += "Diamonds"; break;
		case 3: suitName += "Clubs"; break;
		case 4: suitName += "Jokers"; break;
		default: suitName = "None"; break;
		}
		return suitName;
	}
	
	public synchronized void eliminateAll(int[] cards){
		for(int i = 0; i < cards.length; i++){
			eliminateAll(cards[i]);
		}
	}

	private synchronized void eliminateAll(int card) {
		for(int i = 0; i < gameTypes[gameType][3]; i++){
			eliminate(i, card);
		}
	}

	public synchronized void eliminateAllOthers(int player, int[] cards){
		for(int i = 0; i < gameTypes[gameType][3]; i++){
			if(i != player){
				eliminate(i, cards);
			}
		}
	}
	
	public synchronized void eliminateAllOthers(int player, int card){
		for(int i = 0; i < gameTypes[gameType][3]; i++){
			if(i != player){
				eliminate(i, card);
				//System.out.println("ClientModel" + myPlayerNum + ":eliminate(int, int): " + i + " " + card);
			}
		}
	}
	
	
	public synchronized void eliminateSelf(int card){
		eliminate(myPlayerNum, card);
		System.out.println("ClientModel" + myPlayerNum + ":eliminateSelf(int): " + myPlayerNum + " " + card);
	}
	
	public synchronized void eliminateSelf(int cards[]){
		for(int i = 0; i < cards.length; i++){
			eliminate(myPlayerNum, cards[i]);
			System.out.println("ClientModel:eliminateSelf(int[]): " + myPlayerNum + " " + cards[i]);
		}
	}
	
	public synchronized void eliminate(int player, int[] cards){
		for(int i = 0; i < cards.length; i++){
			eliminate(player, cards[i]);
		}
	}
	
	public synchronized void eliminate(int player, int card){
		if(player != myPlayerNum){
			otherHands[player].remove(new Integer(card));
		}
		else{
			myHand.remove(new Integer(card));
		}
		//System.out.println("ClientModel " + myPlayerNum + " removed " + getCardName(card) + " from player " + player + " cards left " + cardsInHand());
	}
	
	public Icon getIcon(int card){
		String s = "cards/" + getMatchingIcon(card) + ".gif";
		ImageIcon image = new ImageIcon(s);
		return image;
	}
	
	public String getMatchingIcon(int card){
		String name = "";
		card = card/gameTypes[gameType][6];
		switch(card%(13)){
		case 0: name += "Two"; break;
		case 1: name += "Three"; break;
		case 2: name += "Four"; break;
		case 3: name += "Five"; break;
		case 4: name += "Six"; break;
		case 5: name += "Seven"; break;
		case 6: name += "Eight"; break;
		case 7: name += "Nine"; break;
		case 8: name += "Ten"; break;
		case 9: name += "Jack"; break;
		case 10: name += "Queen"; break;
		case 11: name += "King"; break;
		case 12: name += "Ace"; break;
		default: name += (card%13 + 2) + "";
		}
		name += " of ";
		switch(card/(13)){
		case 0: name += "Spades"; break;
		case 1: name += "Hearts"; break;
		case 2: name += "Diamonds"; break;
		case 3: name += "Clubs"; break;
		case 4:
			if(card%(13 * 4) < 1){
				name = "Small Joker";
			}
			else{
				name = "Big Joker";
			}
			break;
		}
		return name;
	}
	
	public String getCardName(int i){
		String name = "";
		i = i/gameTypes[gameType][6];
		switch(i%(13)){
		case 9: name += "Jack"; break;
		case 10: name += "Queen"; break;
		case 11: name += "King"; break;
		case 12: name += "Ace"; break;
		default: name += (i%13 + 2) + "";
		}
		name += " of ";
		switch(i/(13)){
		case 0: name += "Spades"; break;
		case 1: name += "Hearts"; break;
		case 2: name += "Diamonds"; break;
		case 3: name += "Clubs"; break;
		case 4:
			if(i%(13 * 4) < 1){
				name = "Small Joker";
			}
			else{
				name = "Big Joker";
			}
			break;
		}
		return name;
	}
	
	public synchronized boolean setHand(int player, String jobName){ //Playing:card/card/card/card
		checkHandsAreFull();
		ArrayList<Integer> cardsPlayed = new ArrayList<Integer>();
		jobName = jobName.substring(jobName.indexOf(':') + 1);
		while(jobName.indexOf('/') != -1){ //as long as we have more cards
			cardsPlayed.add(Integer.parseInt(jobName.substring(0, jobName.indexOf('/'))));
			jobName = jobName.substring(jobName.indexOf('/') + 1);
		}
		boolean legalPlay = setHand(player, cardsPlayed);
		if(legalPlay && championPartner == champion && (cardsPlayed.contains(2*(calledCard/gameTypes[gameType][6]))|| cardsPlayed.contains((2*(calledCard/gameTypes[gameType][6])) + 1))){
			championPartner = player;
			pcs.firePropertyChange("ChampionPartner" + championPartner, false, true);
		}
		return legalPlay;
	}
	
	@SuppressWarnings("unchecked")
	public synchronized boolean setHand(int player, ArrayList<Integer> cardsPlayed){//check legality
		System.out.println("ClientModel:setHand: " + myPlayerNum + " " + getCurrentPlays(currentWinner) + " " + currentWinner + " " + player);
		if(cardsPlayed.size() == currentPlays[currentWinner].size() || currentPlays[currentWinner].size() == 0){//(cardsPlayed.size() == currentPlays[currentWinner].size() || currentPlays[currentWinner].size() == 0)){
			if(currentPlays[currentWinner].size() != 0){
				setCurrentPlays(player, cardsPlayed);
				if(typeOfPlay < 3){//not highest
					if(cardsPlayed.size() == 1){// single
						if(greaterThan(cardsPlayed.get(0), currentPlays[currentWinner].get(0))){//if we're higher set us as the current winner
							currentWinner = player;
							pcs.firePropertyChange("CurrentWinnerChange", false, true);
						}
					}
					else if(cardsPlayed.size() == 2){ // pair
						if(oneSuitAsTrump(cardsPlayed) && cardsPlayed.get(0)/gameTypes[gameType][6] == cardsPlayed.get(1)/gameTypes[gameType][6] && greaterThan(cardsPlayed.get(0), currentPlays[currentWinner].get(0))){
							currentWinner = player;
							pcs.firePropertyChange("CurrentWinnerChange", false, true);
						}
					}
					else{ // consecutive pair
						if(oneSuitAsTrump(cardsPlayed) && cardsPlayed.get(0)/gameTypes[gameType][6] == cardsPlayed.get(1)/gameTypes[gameType][6] && cardsPlayed.get(2)/gameTypes[gameType][6] == cardsPlayed.get(3)/gameTypes[gameType][6] && isConsecutivePair(cardsPlayed.get(0), cardsPlayed.get(2)) && greaterThan(cardsPlayed.get(0), currentPlays[currentWinner].get(0))){
							currentWinner = player;
							pcs.firePropertyChange("CurrentWinnerChange", false, true);
						}
					}
				}
				else{//gg highest
					if(oneSuitAsTrump(cardsPlayed) && cardsPlayed.size() == highestTemp.size()){
						
						ArrayList<Integer> pairs = new ArrayList<Integer>();
						for(int i = 0; i < cardsPlayed.size() - 1; i++){
							if(cardsPlayed.get(i)/gameTypes[gameType][6] ==  cardsPlayed.get(i + 1)/gameTypes[gameType][6]){
								pairs.add(cardsPlayed.get(i));
								pairs.add(cardsPlayed.get(i + 1));
								i++;
							}
						}
						ArrayList<Integer>[] consecutivePairs = new ArrayList[pairs.size()/2];
						int consecutivePairsItterator = 0;
						for(int i = 0; i < pairs.size() - 3; i+= 2){
							try{
								if(isConsecutivePair(pairs.get(i), pairs.get(i + 2))){
									if(consecutivePairs[consecutivePairsItterator].contains(pairs.get(i))){ // more than a double consecutive
										consecutivePairs[consecutivePairsItterator].add(pairs.get(i + 2));
										consecutivePairs[consecutivePairsItterator].add(pairs.get(i + 3));
									}
									else{ //means we already have a consecutive because we aren't getting a null pointer
										consecutivePairsItterator++;
										consecutivePairs[consecutivePairsItterator] = new ArrayList<Integer>();
										consecutivePairs[consecutivePairsItterator].add(pairs.get(i));
										consecutivePairs[consecutivePairsItterator].add(pairs.get(i + 1));
										consecutivePairs[consecutivePairsItterator].add(pairs.get(i + 2));
										consecutivePairs[consecutivePairsItterator].add(pairs.get(i + 3));
									}
								}
							}catch(NullPointerException e){ //first consecutive we're constructing
								consecutivePairs[consecutivePairsItterator] = new ArrayList<Integer>();
								consecutivePairs[consecutivePairsItterator].add(pairs.get(i));
								consecutivePairs[consecutivePairsItterator].add(pairs.get(i + 1));
								consecutivePairs[consecutivePairsItterator].add(pairs.get(i + 2));
								consecutivePairs[consecutivePairsItterator].add(pairs.get(i + 3));
							}
						}
						
						ArrayList<ArrayList<Integer>> ourPlay = new ArrayList<ArrayList<Integer>>();
						for(int i = 0; i < pairs.size(); i+= 2){
							ArrayList<Integer> temp = consecutivePairsToBeat(i, consecutivePairs);
							if(!temp.equals(null)){
								ourPlay.add(temp);
								pairs.removeAll(temp);
								cardsPlayed.removeAll(temp);
							}
						}
						cardsPlayed.removeAll(pairs);
						ourPlay.add(pairs);
						ourPlay.add(cardsPlayed);
						boolean ans = true;
						for(int i = 0; i < highestTemp.size(); i++){
							ans = ans && greaterThan(ourPlay.get(i).get(0), highestTemp.get(i).get(0));
						}
						if(ans){
							currentWinner = player;
							pcs.firePropertyChange("CurrentWinnerChange", false, true);
							highestTemp = ourPlay;
						}
					}
				}
			}
			else{ //we're starting the trick
				setCurrentPlays(player, cardsPlayed);
				if(cardsPlayed.size() == 1){
					typeOfPlay = 0;
					currentWinner = player;
					pcs.firePropertyChange("CurrentWinnerChange", false, true);
				}
				else{
					ArrayList<Integer> pairs = new ArrayList<Integer>();
					for(int i = 0; i < cardsPlayed.size() - 1; i++){
						if(cardsPlayed.get(i)/gameTypes[gameType][6] ==  cardsPlayed.get(i + 1)/gameTypes[gameType][6]){
							pairs.add(cardsPlayed.get(i));
							pairs.add(cardsPlayed.get(i + 1));
							i++;
						}
					}
					if(pairs.size() == 2 && cardsPlayed.size() == 2){
						typeOfPlay = 1;
						currentWinner = player;
						pcs.firePropertyChange("CurrentWinnerChange", false, true);
						return true;
					}
					ArrayList<Integer>[] consecutivePairs = new ArrayList[pairs.size()/2];
					int consecutivePairsItterator = 0;
					for(int i = 0; i < pairs.size() - 3; i+= 2){
						try{
							if(isConsecutivePair(pairs.get(i), pairs.get(i + 2))){
								if(consecutivePairs[consecutivePairsItterator].contains(pairs.get(i))){ // more than a double consecutive
									consecutivePairs[consecutivePairsItterator].add(pairs.get(i + 2));
									consecutivePairs[consecutivePairsItterator].add(pairs.get(i + 3));
								}
								else{ //means we already have a consecutive because we aren't getting a null pointer
									consecutivePairsItterator++;
									consecutivePairs[consecutivePairsItterator] = new ArrayList<Integer>();
									consecutivePairs[consecutivePairsItterator].add(pairs.get(i));
									consecutivePairs[consecutivePairsItterator].add(pairs.get(i + 1));
									consecutivePairs[consecutivePairsItterator].add(pairs.get(i + 2));
									consecutivePairs[consecutivePairsItterator].add(pairs.get(i + 3));
								}
							}
						}catch(NullPointerException e){ //first consecutive we're constructing
							consecutivePairs[consecutivePairsItterator] = new ArrayList<Integer>();
							consecutivePairs[consecutivePairsItterator].add(pairs.get(i));
							consecutivePairs[consecutivePairsItterator].add(pairs.get(i + 1));
							consecutivePairs[consecutivePairsItterator].add(pairs.get(i + 2));
							consecutivePairs[consecutivePairsItterator].add(pairs.get(i + 3));
						}
					}
					try{
						if(consecutivePairs[0].size() == cardsPlayed.size()){
							typeOfPlay = 2;
							currentWinner = player;
							pcs.firePropertyChange("CurrentWinnerChange", false, true);
						}
						else{
							throw new NullPointerException();
						}
					}catch(NullPointerException e){ //mean's we're highest
						typeOfPlay = 3;
						currentWinner = player;
						pcs.firePropertyChange("CurrentWinnerChange", false, true);
						highestTemp.clear();
						for(int i = 0; i < pairs.size(); i+= 2){
							ArrayList<Integer> temp = consecutivePairsToBeat(i, consecutivePairs);
							if(!temp.equals(null)){
								highestTemp.add(temp);
								pairs.removeAll(temp);
								cardsPlayed.removeAll(temp);
							}
						}
						cardsPlayed.removeAll(pairs);
						highestTemp.add(pairs);
						highestTemp.add(cardsPlayed);
						
					}
				}
			}
			return true;
		}
		return false; //means you don't have all the cards that you tried to play || you aren't playing the right amount
	}
	
	public synchronized ArrayList<Integer> consecutivePairsToBeat(int length, ArrayList<Integer>[] consecutivePairs){
		int thisArray = 0;
		boolean found = false;
		for(int i = 1; i < consecutivePairs.length; i++){
			if(length == consecutivePairs[i].size()){
				found = true;
				if(!greaterThan(consecutivePairs[thisArray].get(0), consecutivePairs[i].get(0))){//check first cards against each other
					thisArray = i;
				}
			}
		}
		if(found){
			return consecutivePairs[thisArray];	
		}
		return null;
	}

	public synchronized String checkHighest(String jobName) {
		ArrayList<Integer> cardsPlayed = new ArrayList<Integer>();

		//Check:length: + l1 + "card:" + card + "/"
		//pair: + card + "/"
		//single: + card + "/"
		jobName = jobName.substring(jobName.indexOf(':') + 1);
		while(jobName.contains("length")){
			System.out.println("ClientModel: checkHighest: " + jobName.substring(jobName.indexOf(':') + 1, jobName.indexOf('d') -1));
			System.out.println("ClientModel: checkHighest: " + jobName.substring(jobName.indexOf('d') + 2, jobName.indexOf('/') -1 ));
			cardsPlayed.add(Integer.parseInt(jobName.substring(jobName.indexOf(':') + 1, jobName.indexOf('d') -1)));
			cardsPlayed.add(Integer.parseInt(jobName.substring(jobName.indexOf('d') + 2, jobName.indexOf('/') -1 )));
			jobName = jobName.substring(jobName.indexOf('/') + 1);
		}
		consecutivePairLengths = new int[cardsPlayed.size()];
		for(int i = 0; i < cardsPlayed.size(); i++){
			consecutivePairLengths[i] = cardsPlayed.get((i/2) + 1);
			cardsPlayed.remove(i);
		}
		while(jobName.contains("/")){
			cardsPlayed.add(Integer.parseInt(jobName.substring(jobName.indexOf(':') + 1, jobName.indexOf('/') -1 )));
			jobName = jobName.substring(jobName.indexOf('/') + 1);
		}
		return checkHighest(cardsPlayed);
	}

	public synchronized String checkHighest(ArrayList<Integer> cardsPlayed){
		String s = "";
		ArrayList<Integer> possiblePlays = getCardsInSuitArrayList(myPlayerNum, cardsPlayed.get(0));
		for(int i = 0; i < consecutivePairLengths.length; i++){
			ArrayList<Integer> temp = findConsecutivePairOfLength(possiblePlays, consecutivePairLengths[i]);
			if(temp == null && consecutivePairLengths[i] > 0){
			}
			else if(greaterThan(temp.get(0), cardsPlayed.get(i) )){
				s = "";
				for(int j = 0; j < consecutivePairLengths[i]; j++){
					s += "" + (cardsPlayed.get(i) - j) + "/";
				}
			}
		}
		int pair = cardsPlayed.get(cardsPlayed.size()-2);
		int single = cardsPlayed.get(cardsPlayed.size()-1);
		for(int i = 0; i < possiblePlays.size(); i++){
			if(greaterThan(possiblePlays.get(i), single)){
				s = "" + single;
			}
		}
		for(int i = 0; i < possiblePlays.size() - 1; i++){
			if(checkIsPair(possiblePlays.get(i), possiblePlays.get(i + 1))){
				s = "" + (pair - 1) + "/" + (pair);
			}
		}
		return s;
	}
	
	public boolean checkIsPair(int card1, int card2){
		return card1/gameTypes[gameType][6] == card2/gameTypes[gameType][6];
	}
	
	public boolean isConsecutivePair(int card1, int card2){
		if((card1/gameTypes[gameType][6])%13 == levels[champion] - 2){ //lower is the trump number
			if((getRank(card2) == 28 && getRank(card1) == 26) || card1/gameTypes[gameType][6] == card2/gameTypes[gameType][6]){// higher is a small joker and we're the flipped card or both trump number non prime
				return true;
			}
			return false;
		}
		else if(getRank(card1) == 25 && getRank(card2) == 26){// higher is a trump number non prime and we're an ace
			return true;
		}
		else if(1 + (card1/gameTypes[gameType][6])%13 == (cardFlipped/gameTypes[gameType][6])%13){//next one is the trump number so we skip it
			return card1/gameTypes[gameType][6] + 2 == card2/gameTypes[gameType][6];
		}
		else if((card2/gameTypes[gameType][6])%13 == 0){ //check if the higher is a 2 after checking if its a prime and everything
			return false;
		}
		else{
			return (card1/gameTypes[gameType][6]) + 1 == card1/gameTypes[gameType][6]; // return if the next one is the next card sequentially
		}
	}
	
	public synchronized ArrayList<Integer> getCardsInSuitArrayList(int player, int suit){
		ArrayList<Integer> cardsInSuit = new ArrayList<Integer>();
		if(player == myPlayerNum){
			for(int i = 0; i < myHand.size(); i++){
				if(getSuitAsTrump(myHand.get(i)) == suit){
					cardsInSuit.add(myHand.get(i));
				}
			}
		}
		else{
			for(int i = 0; i < otherHands[player].size(); i++){
				if(getSuitAsTrump(otherHands[player].get(i)) == suit){
					cardsInSuit.add(otherHands[player].get(i));
				}
			}
		}
		return cardsInSuit;
	} 
	
	public int getCardsInSuitInt(int player, int suit){
		return getCardsInSuitArrayList(player, suit).size();
	}
	
	public ArrayList<Integer> findConsecutivePairOfLength(ArrayList<Integer> possibleCards, int length){
		ArrayList<Integer> pairs = new ArrayList<Integer>();
		for(int i = 0; i < possibleCards.size() - 1; i++){
			if(possibleCards.get(i)/gameTypes[gameType][6] ==  possibleCards.get(i + 1)/gameTypes[gameType][6]){
				pairs.add(possibleCards.get(i));
				pairs.add(possibleCards.get(i + 1));
				i++;
			}
		}
		ArrayList<Integer>[] consecutivePairs = new ArrayList[pairs.size()/2];
		int consecutivePairsItterator = 0;
		for(int i = 0; i < pairs.size() - 3; i+= 2){
			try{
				if(isConsecutivePair(pairs.get(i), pairs.get(i + 2))){
					if(consecutivePairs[consecutivePairsItterator].contains(pairs.get(i))){ // more than a double consecutive
						consecutivePairs[consecutivePairsItterator].add(pairs.get(i + 2));
						consecutivePairs[consecutivePairsItterator].add(pairs.get(i + 3));
					}
					else{ //means we already have a consecutive because we aren't getting a null pointer
						consecutivePairsItterator++;
						consecutivePairs[consecutivePairsItterator] = new ArrayList<Integer>();
						consecutivePairs[consecutivePairsItterator].add(pairs.get(i));
						consecutivePairs[consecutivePairsItterator].add(pairs.get(i + 1));
						consecutivePairs[consecutivePairsItterator].add(pairs.get(i + 2));
						consecutivePairs[consecutivePairsItterator].add(pairs.get(i + 3));
					}
				}
			}catch(NullPointerException e){ //first consecutive we're constructing
				consecutivePairs[consecutivePairsItterator] = new ArrayList<Integer>();
				consecutivePairs[consecutivePairsItterator].add(pairs.get(i));
				consecutivePairs[consecutivePairsItterator].add(pairs.get(i + 1));
				consecutivePairs[consecutivePairsItterator].add(pairs.get(i + 2));
				consecutivePairs[consecutivePairsItterator].add(pairs.get(i + 3));
			}
		}
		return consecutivePairsToBeat(length, consecutivePairs);
	}
	
}
