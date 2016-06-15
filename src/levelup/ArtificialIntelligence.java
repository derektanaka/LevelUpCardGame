package levelup;

import java.util.ArrayList;
import java.util.Random;

public class ArtificialIntelligence {

	private ClientModel clientModel;
	private int myPlayerNum;
	
	public ArtificialIntelligence(ClientModel clientModel, int myPlayerNum){
		this.clientModel = clientModel;
		this.myPlayerNum = myPlayerNum;
	}
	
	public boolean shouldFlip(int suitFlip){
		synchronized(clientModel){
			if(suitFlip == 4){ //jokers with no offsuit aces after round 1
				return false;
			}
			return (0.5*expect(suitFlip, clientModel.cardsInHand())) + clientModel.getNumPerSuit(suitFlip) > 1 + (clientModel.getHandSize()/4.5);
		}
	}
	
	public boolean shouldOutFlip(int suitInitial, int suitOutFlip){
		synchronized(clientModel){
			if(suitOutFlip == 4 && clientModel.getOffSuitAces() == 0 && clientModel.getRound() != 1){ // outflipping with jokers, no offsuit aces after round one
				//System.out.println("ArtificialIntelligence " + "Should outflip with jokers because we have no offsuit aces and this is not round 1");
				return true;
			}
			else if(clientModel.getNumPerSuit(suitInitial) <= clientModel.getNumPerSuit(suitOutFlip)){ //more primes in outflipped suit
				//System.out.println("ArtificialIntelligence " + "Should outflip because there are more primes in the outflipsuit " + clientModel.getSuitString(suitOutFlip) + " compared to the current trump suit" + clientModel.getSuitString(suitInitial));
				return true;
			}
			return false;
		}
	}
	
	/**
	 * amount of cards left in suit = amount of cards in suit - amount of cards I already have - number of cards flipped in the suit
	 * amount of cards left in deck = amount of cards total - amount of cards in my hand - number of cards flipped
	 * @param suit
	 * @param cardsInHand
	 * @return number we expect
	 */
	public double expect(int suit, int cardsInHand){
		synchronized(clientModel){
			double ans = (clientModel.getHandSize()- cardsInHand + 0.0) /(clientModel.gameTypes[clientModel.gameType][0] + clientModel.gameTypes[clientModel.gameType][1] - clientModel.cardsInHand());
			if(suit == 4 && clientModel.getChampionFlip() == 0){//jokers no flips in jokers
				return ans * ((2 * clientModel.gameTypes[clientModel.gameType][6]) - clientModel.getNumPerSuit(suit));
			}
			else if(suit == 4 && clientModel.getTrumpSuit() == 4 && clientModel.getChampionFlip() != 0){ // jokers outflipping jokers (only makes sense for first round with godly hand)
				return ans * (((2 * clientModel.gameTypes[clientModel.gameType][6]) - clientModel.getNumPerSuit(suit) - clientModel.getChampionFlip()));
			}
			//everything else
			return ans * ((13 * clientModel.gameTypes[clientModel.gameType][6]) - clientModel.getNumPerSuit(suit));
		}
	}

	public int[] calculateBottom() {
		int[] bottom = new int[clientModel.gameTypes[clientModel.gameType][1]];
		Random r = new Random();
		for(int i = 0; i < bottom.length; i++){
			bottom[i] = clientModel.getHand(i);
			clientModel.setBottom(bottom[i], i);
		}
		/*
		 * index everything as a play (highest, consecutive pair, pair, single
		 * choose a suit to call
		 * 		
		 * 
		 * dont bottom points unless x primes + y jokers (cannot be outprimed and cannot be beat)
		 * do not bottom a consecutive pair
		 * do not bottom pairs (usually)
		 * do not bottom primes (usually)
		 * attempt to short suit
		 * find a suit for the call
		 * 		call suit needs to have at least one point card (K + point), (K), (point)
		 * 		call suit (minus aces and points) should be shortable
		 * 		usually bad to have pairs in call suit
		 */
		return bottom;
	}
	
	public boolean isShortable(int suit){// minus points and aces on left hand side
		return clientModel.getNumPerSuit(suit) <= clientModel.gameTypes[clientModel.gameType][1];
	}
	
	public String getCardCall(){
		String s = "Calling:";
		if(clientModel.getOffSuitAces() < 6){ //i have no clue whats going on
			for(int i = 0; i < 4; i++){
				if(i == clientModel.getTrumpSuit()){
				}
				else{ //possible to call an ace// check whether i have the other ace in the suit???
					int acesInSuit = 0;
					int ace = 13*clientModel.gameTypes[clientModel.gameType][6] - 1;
					ArrayList<Integer> cardsInSuit = clientModel.getCardsInSuitArrayList(myPlayerNum, i);
					for(int j = 0; j < cardsInSuit.size(); j++){
						if((cardsInSuit.get(j)/clientModel.gameTypes[clientModel.gameType][6])%13 == 12){
							acesInSuit++;
						}
					}
					if(acesInSuit == 0){
						return s += "First:" + ace; 
					}
					else{
						return s += "Other:" + ace;
					}
				}
			}
		}
		return "Calling:1v4";
	}

	public String getPlay() {
		//"Single" + card;
		//card1 + "Pair" card2;
		// #length of consecutive pair + "consecutivepair" + card1 + card2 + card3 + card4 + (card5) + (card6);
		// #single + "S" + #pair + "p" + #consecutivepair + "c" + length of CPs + "l" + "Highest";
		Random r = new Random();
		//System.out.print("ArtificialIntelligence:getPlay(): cardsInHand() = " + clientModel.cardsInHand() + " player: " + myPlayerNum);
		int card = clientModel.getHand(r.nextInt(clientModel.cardsInHand()));
		//int card = getPlay(clientModel.);
		//System.out.println(" " + card);
		clientModel.eliminateSelf(card);
		String s = "Play:" + card + "/";
		//System.out.println("ArtificialIntelligence " + s);
		return s;
	}
	
	public String getPlay(int suit){ //response
		Random r = new Random();
		String s = "Play:";
		ArrayList<Integer> cardsInSuit = clientModel.getCardsInSuitArrayList(myPlayerNum, suit);
		int numberOfCards = clientModel.getHandToBeat().size();
		if(cardsInSuit.size() == 0){ // we're short of this suit
			if(suit == 4){ //means the suit is trump suit
				for(int i = 0; i < numberOfCards; i++){ //throw boys
					s += getLowestCardInHand() + "/";
				}
			}
			else{
				if(clientModel.getPointInPlay() > 0){// #heuristics
					s += clientModel.getHand(r.nextInt(clientModel.cardsInHand())) + "/";
				}
			}
		}
		else{
			s += "" + clientModel.getCardsInSuitArrayList(myPlayerNum, suit).get(r.nextInt(clientModel.getCardsInSuitArrayList(myPlayerNum, suit).size())) + "/";
		}
		System.out.println(s);
		return s;
	}
	
	public int getLowestCardInHand(){
		int lowestCard = clientModel.getHand(0); //lowest = big joker
		for(int i = 0; i < clientModel.cardsInHand(); i++){
			//System.out.println(clientModel.getHand(i));
			if(clientModel.greaterThan(lowestCard, clientModel.getHand(i))){ //waiting for clientModel.getHand(i) to be lower than the lowest card
				//System.out.println("ArtificialIntelligence: getLowestCardInHand(): " + " throwing" + clientModel.cardsInHand() + " " + clientModel.getCardName(clientModel.getHand(i)) + " " + clientModel.getHand(i));
				lowestCard = clientModel.getHand(i);
			}
		}
		clientModel.eliminateSelf(lowestCard);
		return lowestCard;
	}
	
}
