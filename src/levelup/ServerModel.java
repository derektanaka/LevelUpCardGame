package levelup;

import java.util.ArrayList;
import java.util.Collections;


public class ServerModel {

	private ArrayList<Integer> cardList;
	private ArrayList<Integer>[] playerHands;
	private ArrayList<Integer>[] currentPlays;
	public final int[][] gameTypes = {
			{48, 6, 12, 4, 40, 20 ,1},
			{100, 8, 20, 5, 80, 40, 2},
			{154, 8, 22, 7, 120, 60, 3}};
	public final int gameType;
	private int[] bottom;
	private int[] levels;
	private int champion;
	private int cardFlipped;
	private int championFlipOrOutFlip;
	private int trumpSuit;
	private int round;
	private int positionInDeal;
	private int currentWinner;
	private int trickItterator;
	private int typeOfPlay;
	private ArrayList<Integer> highestTemp;
	private ArrayList<Integer> cardsToBeatForHighest;
	private int[] consecutivePairLengths;
	private boolean[] playersReady;
	private int currentSuit;
	private boolean dealFinished;
	private boolean bottomSet;
	
	@SuppressWarnings("unchecked")
	public ServerModel(int numPlayers){
		int temp = 0;
		for(int i = 0; i < 3; i++){
			if(gameTypes[i][3] == numPlayers){
				temp = i;
			}
		}
		gameType = temp;
		cardList = new ArrayList<Integer>();
		playerHands = new ArrayList[gameTypes[gameType][3]];
		currentPlays = new ArrayList[gameTypes[gameType][3]];
		playersReady = new boolean[gameTypes[gameType][3]];
		cardsToBeatForHighest = new ArrayList<Integer>();
		for(int i = 0; i < playerHands.length; i++){
			playerHands[i] = new ArrayList<Integer>();
			currentPlays[i] = new ArrayList<Integer>();
			playersReady[i] = false;
			
		}
		bottom = new int[gameTypes[gameType][1]];
		levels = new int[numPlayers];
		for(int i = 0; i < levels.length; i++){
			levels[i] = 2;
		}
		typeOfPlay= 0;
		champion = 0;
		round = 1;
		championFlipOrOutFlip = 0;
		trumpSuit = -1;
		cardFlipped = 0;
		positionInDeal = 0;
		dealFinished = false;
		bottomSet = false;
		deal();
	}
	
	public synchronized void startNextRound(){
		round++;
		for(int i = 0; i < gameTypes[gameType][3]; i++){
			playerHands[i].clear();
			playersReady[i] = false;
			bottom = new int[gameTypes[gameType][1]];
			championFlipOrOutFlip = 0;
			trumpSuit = -1;
			cardFlipped = 0;
			positionInDeal = 0;
			dealFinished = false;
			bottomSet = false;
		}
	}
	
	public void deal(){
		cardList.clear();
		for(int i = 0; i < gameTypes[gameType][0] + gameTypes[gameType][1]; i++){
			cardList.add(i);
			//System.out.println("ServerModel " + i + " " + getCardName(cardList.get(i)));
		}
		Collections.shuffle(cardList);
		for(int i = 0; i < cardList.size(); i++){
			//System.out.println("ServerModel " + cardList.get(i) + " " + getCardName(cardList.get(i)));
		}
		for(int i = 0; i < gameTypes[gameType][0]; i++){
			playerHands[i%gameTypes[gameType][3]].add(cardList.get(i));
		}
		for(int i = 0; i < gameTypes[gameType][3]; i++){
			for(int j = 0; j < gameTypes[gameType][2]; j++){
				//System.out.println("ServerModel " + "player " + i + ", " + getCardName(playerHands[i].get(j)));
			}
		}
		for(int i = 0; i < gameTypes[gameType][1]; i++){
			bottom[i] = cardList.get(i + gameTypes[gameType][0]);
			//System.out.println("ServerModel " + "bottom " + gameTypes[gameType][1] + ", " + getCardName(bottom[i]));
		}
	}
	
	public void printChampionHand(){
		printPlayerHand(champion);
	}
	
	public void printPlayerHand(int player){
		this.playerHands[player] = sortedHand(playerHands[player]);
		for(int j = 0; j < gameTypes[gameType][2]; j++){
			System.out.println("ServerModel " + "player " + player + ", " + getCardName(playerHands[player].get(j)) + " " + playerHands[player].get(j));
		}
	}
	
	public int[] getBottom(){
		return bottom;
	}
	
	public void setBottom(int card, int position){
		bottom[position] = card;
	}
	
	public boolean getBottomSet(){
		return bottomSet;
	}
	
	public void setBottomSet(boolean set){
		bottomSet = set;
	}
	
	public int getCard(int player, int cardNum){
		return playerHands[player].get(cardNum);
	}
	
	public String getCardAsString(int player, int cardNum){
		return "" + playerHands[player].get(cardNum);
	}
	
	public int getHandSize(){
		return gameTypes[gameType][2];
	}
	
	public int getNumPlayer(){
		return gameTypes[gameType][3];
	}
	
	public int getPositionInDeal(){
		return positionInDeal;
	}
	
	public void setPositionInDeal(int positionInDeal){
		this.positionInDeal = positionInDeal;
	}
	
	public synchronized void incrementPositionInDeal(){
		this.positionInDeal++;
	}
	
	public boolean getDealFinished(){
		return dealFinished;
	}
	
	public void setDealFinished(boolean dealFinished){
		this.dealFinished = dealFinished;
	}
	
	public synchronized void setHand(int player, int[] cards){
		for(int i = 0; i < cards.length; i++){
			setHand(player, cards[i]);
		}
	}
	
	public synchronized void setHand(int player, int card){
		playerHands[player].add(card);
	}
	
	public synchronized void resetCurrentPlays(){
		for(int i = 0; i < currentPlays.length; i++){
			currentPlays[i].clear();
		}
	}
	
	public synchronized void setCurrentPlays(int player, int card){
		currentPlays[player].add(card);
		removeCard(player, card);
		
	}
	
	public synchronized void setCurrentPlays(int player, int[] cards){
		for(int i = 0; i < cards.length; i++){
			currentPlays[player].add(cards[i]);
			removeCard(player, cards[i]);
		}
	}
	
	public synchronized void setCurrentPlays(int player, ArrayList<Integer> cards){
		for(int i = 0; i < cards.size(); i++){
			currentPlays[player].add(cards.get(i));
			removeCard(player, cards.get(i));
		}
	}
	
	public synchronized void removeCard(int player, int card){
		//System.out.println("ServerModel " + "removing card: " + getCardName(card) + " from " + player + " " + totalCardsLeft());
		playerHands[player].remove(new Integer(card));
	}
	
	public synchronized void removeCard(int player, int[] cards){
		for(int i = 0; i < cards.length; i++){
			removeCard(player, cards[i]);
		}
	}
	
	public synchronized int totalCardsLeft(){
		int ans = 0;
		for(int i = 0; i < playerHands.length; i++){
			for(int j = 0; j < playerHands[i].size(); j++){
				ans++;
			}
		}
		return ans;
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
	
	public int getSuit(int card){
		return (card/gameTypes[gameType][6])/13;
	}
	
	public int getCurrentSuit(){
		return currentSuit;
	}
	
	public void setCurrentSuit(int currentSuit){
		this.currentSuit = currentSuit;
		System.out.println("ServerModel:setCurrentSuit:" + currentSuit);
	}
	
	public boolean greaterThan(int card1, int card2){
		return getRank(card1) > getRank(card2);
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
		else if(52*gameTypes[gameType][6] < card && card < 53*gameTypes[gameType][6] - 1){
			return 28; //small joker
		}
		else if(53*gameTypes[gameType][6] < card && card < 54*gameTypes[gameType][6] - 1){
			return 29; // big joker
		}
		return (card/gameTypes[gameType][6])%13; // everything else
	}
	
	public int getTrickItterator(){
		return trickItterator;
	}
	
	public void setTrickItterator(int trickItterator){
		this.trickItterator = trickItterator;
		resetCurrentPlays();
	}
	
	public int getChampionLevel(){
		return levels[champion];
	}
	
	public int getChampion(){
		return champion;
	}
	
	public void setChampion(int champion){
		trickItterator = champion;
		this.champion = champion;
	}
	
	public int getChampionFlip(){
		return championFlipOrOutFlip;
	}
	
	public int getRound(){
		return round;
	}
	
	public void setRound(int round){
		this.round = round;
	}
	
	public boolean getAllPlayersReady(){
		boolean ans = true;
		for(int i = 0; i < playersReady.length; i++){
			ans = ans && playersReady[i]; 
		}
		return ans;
	}
	
	public boolean getPlayerReady(int player){
		return playersReady[player];
	}
	
	public void setPlayerReady(int player, boolean ready){
		playersReady[player] = ready;
	}
	
	public void setTrumpSuit(int card){
		trumpSuit = this.getSuit(card);
		cardFlipped = card;
		championFlipOrOutFlip++;
	}
	
	public void setChampionFlip(int championFlip){
		this.championFlipOrOutFlip = championFlip;
	}
	
	public int getFlipStrength(){
		return cardFlipped;
	}
	
	public int getTrumpSuit(){
		return trumpSuit;
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
	
	public boolean isFlipable(int card){
		if(card == -1){
			return false;
		}
		return ((card/gameTypes[gameType][6])%13)+2 == levels[champion];
	}
	
	public boolean cardChecker(int player, int[] cards){
		boolean ans = true;
		for(int i = 0; i < cards.length; i++){
			ans = ans && cardChecker(player, cards[i]);
		}
		return ans;
	}
	
	public boolean cardChecker(int player, ArrayList<Integer> cards){
		boolean ans = true;
		for(int i = 0; i < cards.size(); i++){
			ans = ans && cardChecker(player, cards.get(i));
		}
		return ans;
	}
	
	public boolean cardChecker(int player, int card){
		return playerHands[player].contains(card);
	}
	
	public ArrayList<Integer> getCurrentPlays(int player){
		return currentPlays[player];
	}
	
	public int trickWinner(){
		System.out.println("ServerModel:trickWinner" + currentWinner);
		return currentWinner;
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
	
	public void setHighestOk(){
		
	}
	
	public void getHighestOk(){
		
	}
	
	public void highestOk(){
		setCurrentPlays(currentWinner, highestTemp);
	}
	
	public void beatHighest(String jobName){
		ArrayList<Integer> cardsPlayed = new ArrayList<Integer>();
		jobName.substring(jobName.indexOf(':') + 1);
		while(jobName.indexOf('/') != -1){ //as long as we have more cards
			cardsPlayed.add(Integer.parseInt(jobName.substring(0, jobName.indexOf('/') - 1)));
			jobName = jobName.substring(jobName.indexOf('/') + 1);
		}
		beatHighest(cardsPlayed);
	}
	
	public void beatHighest(ArrayList<Integer> cardsForced){
		setCurrentPlays(currentWinner, cardsForced);
	}
	
	public ArrayList<Integer> sortedHand(ArrayList<Integer> hand){
		//dualPivotQuicksort(hand, 0, gameTypes[gameType][2], 3);
		Collections.sort(hand);
		return hand;
	}
	
	public synchronized boolean setHand(int player, String jobName){ //Playing:card/card/card/card
		ArrayList<Integer> cardsPlayed = new ArrayList<Integer>();
		jobName = jobName.substring(jobName.indexOf(':') + 1);
		while(jobName.indexOf('/') != -1){ //as long as we have more cards
			cardsPlayed.add(Integer.parseInt(jobName.substring(0, jobName.indexOf('/'))));
			jobName = jobName.substring(jobName.indexOf('/') + 1);
		}
		return setHand(player, cardsPlayed);
	}
	
	@SuppressWarnings("unchecked")
	public boolean setHand(int player, ArrayList<Integer> cardsPlayed){//check legality
		if(cardChecker(player, cardsPlayed)){//(cardsPlayed.size() == currentPlays[currentWinner].size() || currentPlays[currentWinner].size() == 0)){
			if(currentPlays[currentWinner].size() != 0 && cardsPlayed.size() == currentPlays[currentWinner].size() && cardsPlayed.size() > 0){
				setCurrentPlays(player, cardsPlayed);
				if(oneSuitAsTrump(cardsPlayed) && getSuitAsTrump(cardsPlayed.get(0)) == getSuitAsTrump(currentPlays[currentWinner].get(0))){
					if(typeOfPlay < 3){ //not highest
						if(cardsPlayed.size() == 1){// single
							if(greaterThan(cardsPlayed.get(0), currentPlays[currentWinner].get(0))){//if we're higher set us as the current winner
								currentWinner = player;
							}
						}
						else if(cardsPlayed.size() == 2){ // pair
							if(cardsPlayed.get(0)/gameTypes[gameType][6] == cardsPlayed.get(1)/gameTypes[gameType][6] && greaterThan(cardsPlayed.get(0), currentPlays[currentWinner].get(0))){
								currentWinner = player;
							}
						}
						else{ // consecutive pair
							if(cardsPlayed.get(0)/gameTypes[gameType][6] == cardsPlayed.get(1)/gameTypes[gameType][6] && cardsPlayed.get(2)/gameTypes[gameType][6] == cardsPlayed.get(3)/gameTypes[gameType][6] && isConsecutivePair(cardsPlayed.get(0), cardsPlayed.get(2)) && greaterThan(cardsPlayed.get(0), currentPlays[currentWinner].get(0))){
								currentWinner = player;
							}
						}
					}
					else{//gg highest
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
						
						ArrayList<Integer> ourPlay = new ArrayList<Integer>();
						for(int i = 0; i < pairs.size(); i+= 2){
							ArrayList<Integer> temp = consecutivePairsToBeat(i, consecutivePairs);
							if(!temp.equals(null)){
								ourPlay.add(temp.get(temp.size() - 1));
								pairs.removeAll(temp);
								cardsPlayed.removeAll(temp);
							}
						}
						cardsPlayed.removeAll(pairs);
						ourPlay.add(pairs.get(pairs.size()-1));
						ourPlay.add(cardsPlayed.get(cardsPlayed.size()-1));
						boolean ans = true;
						for(int i = 0; i < cardsToBeatForHighest.size(); i++){
							ans = ans && greaterThan(ourPlay.get(i), cardsToBeatForHighest.get(i));
						}
						if(ans){
							currentWinner = player;
							cardsToBeatForHighest = ourPlay;
						}
					}
				}
			}
			else if(currentPlays[currentWinner].size() == 0){ //we're starting the trick
				if(cardsPlayed.size() == 1){
					setCurrentPlays(player, cardsPlayed.get(0));
					currentWinner = player;
					typeOfPlay = 0;
					setCurrentSuit(getSuitAsTrump(cardsPlayed.get(0)));
				}
				else if(oneSuitAsTrump(cardsPlayed)){
					ArrayList<Integer> pairs = new ArrayList<Integer>();
					for(int i = 0; i < cardsPlayed.size() - 1; i++){
						if(cardsPlayed.get(i)/gameTypes[gameType][6] ==  cardsPlayed.get(i + 1)/gameTypes[gameType][6]){
							pairs.add(cardsPlayed.get(i));
							pairs.add(cardsPlayed.get(i + 1));
							i++;
						}
					}
					if(pairs.size() == 2 && cardsPlayed.size() == 2){
						setCurrentPlays(player, cardsPlayed);
						typeOfPlay = 1;
						setCurrentSuit(getSuitAsTrump(cardsPlayed.get(0)));
						currentWinner = player;
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
							setCurrentPlays(player, cardsPlayed);
							setCurrentSuit(getSuitAsTrump(cardsPlayed.get(0)));
							typeOfPlay = 2;
							currentWinner = player;
						}
						else{
							throw new NullPointerException();
						}
					}catch(NullPointerException | ArrayIndexOutOfBoundsException e){ //mean's we're highest
						consecutivePairLengths = new int[consecutivePairsItterator + 1];
						int cheapItterator = 0;
						currentPlays[player].clear();
						currentPlays[player].add(-1);
						highestTemp = cardsPlayed;
						typeOfPlay = 3;
						setCurrentSuit(getSuitAsTrump(cardsPlayed.get(0)));
						currentWinner = player;
						cardsToBeatForHighest.clear();
						for(int i = 0; i < pairs.size(); i+= 2){
							ArrayList<Integer> temp = consecutivePairsToBeat(i, consecutivePairs);
							try{
								if(!temp.equals(null)){
									consecutivePairLengths[cheapItterator] = temp.size();
									cheapItterator++;
									cardsToBeatForHighest.add(temp.get(temp.size()-1));
									pairs.removeAll(temp);
									cardsPlayed.removeAll(temp);
								}
							}catch(NullPointerException e2){
							}
						}
						if(cardsToBeatForHighest.isEmpty()){
							consecutivePairLengths[0] = -1;
							cardsToBeatForHighest.add(-1);
						}
						cardsPlayed.removeAll(pairs);
						if(pairs.isEmpty()){
							pairs.add(-1);
						}
						if(cardsPlayed.isEmpty()){
							cardsPlayed.add(-1);
						}
						cardsToBeatForHighest.add(pairs.get(pairs.size()-1));
						cardsToBeatForHighest.add(cardsPlayed.get(cardsPlayed.size()-1));
						for(int i = 0; i < playersReady.length; i++){ // make ready for all players false
							playersReady[i] = false;
						}
					}
				}
				else{
					return false;
				}
			}
			return true;
		}
		return false; //means you don't have all the cards that you tried to play || you aren't playing the right amount
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
	
	public ArrayList<Integer> consecutivePairsToBeat(int length, ArrayList<Integer>[] consecutivePairs){
		int thisArray = 0;
		boolean found = false;
		try{
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
		}catch(NullPointerException e){
			return null;
		}
		return null;
	}
	
	
	public static void main(String[] args){
		//ServerModel m = new ServerModel(5);
	}

	public String getCardsToBeat() {
		String s = "";
		int cheapItterator = 0;
		while(cheapItterator < consecutivePairLengths.length){
			 s += "length:" + consecutivePairLengths[cheapItterator] + "card:" + cardsToBeatForHighest.get(cheapItterator) + "/";
		}
		for(int i = cheapItterator; i < cardsToBeatForHighest.size(); i++){
			if(cheapItterator == i){
				s += "pair:" + cardsToBeatForHighest.get(i) + "/";
			}
			else{
				s += "single:" + cardsToBeatForHighest.get(i) + "/";
			}
		}
		return s;
	}

	public void setCardCall(int card) {
	}
	
}
