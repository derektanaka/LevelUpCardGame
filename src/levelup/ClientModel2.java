package levelup;

import java.awt.List;
import java.util.ArrayList;
import java.util.LinkedList;

public class ClientModel2{

	public int numPlayers;
	public int myPlayerNum;
	public int[] levels = {2, 2, 2, 2, 2};
	public int round;
	public int champion;
	public int championPartner;
	public int calledCard;
	public int primeSuit;
	public int sj = 22;
	public int bj = 23;
	public LinkedList<String> toGUI = new LinkedList<String>();
	public LinkedList<String> fromGUI = new LinkedList<String>();
	private ArrayList<Integer> cards;
	private ArrayList<Integer> singles;
	private ArrayList<Integer> pairs;
	private ArrayList<Integer> cPairs;
	private boolean startTrick;
	private int[] startTrickInfo;
	/*
	 * number of players
my number
everyones levels
round number
champion
trump suit
called card
champion partner(s)

	 */
	
	public ClientModel2(int numPlayers, int myPlayerNum){
		
	}
	//single, pair, consec, highest
	//1, 2, 3, 4
	//consec, number of pairs
	//highest
	
	public void Played(String s){
		Trick t = new Trick(s);
		//tell gui how many cards to play
		// should be given a list of 3 digit numbers indicating which cards are being played
	}
	
	public boolean isLegalPlay(String play){
		parsePlay(play, startTrick);
		if(startTrick){
			return sameSuit();
		}
		else{
			return false;
		}
	}
	
	public boolean sameSuit(){
		int suit = getSuit(cards.get(0));
		boolean sameSuit = true;
		for(Integer card : cards){
			sameSuit = sameSuit && getSuit(card)==suit;
		}
		return sameSuit;
	}
	
	public void parsePlay(String play){
		parsePlay(play, false);
	}
	
	public void parsePlay(String play, boolean startTrick){
		cards = new ArrayList<Integer>();
		singles = new ArrayList<Integer>();
		pairs = new ArrayList<Integer>();
		cPairs = new ArrayList<Integer>();
		int length = play.length();
		for(int i = 0; i < length/3; i++){
			cards.add(Integer.parseInt(play.substring(0, 3)));
			singles.add(cards.get(i));
			play = play.substring(3);
		}
		int numCards = cards.size();
		//look for pairs
		int i = 0;
		while(i < singles.size()){
			if(singles.get(i)%2 == 0){
				try{
					if(singles.get(i)+1 == singles.get(i+1)){
						pairs.add(singles.remove(i));
						pairs.add(singles.remove(i));
						i--;
					}
				}catch(IndexOutOfBoundsException e){
				}
			}
			i++;
		}
		i = 0;
		while(i < pairs.size() && i >= 0){
			if(pairs.get(i)%2 == 0){
				int x = -2;
				try{
					while(adjacentPower(pairs.get(i), pairs.get(i+2))){
						cPairs.add(pairs.remove(i));
						cPairs.add(pairs.remove(i));
						x++;
					}
					if(x!=-2){
						cPairs.add(pairs.remove(i));
						cPairs.add(pairs.remove(i));
						i -= x;
					}
				}catch(IndexOutOfBoundsException e){
					if(x!=-2){
						cPairs.add(pairs.remove(i));
						cPairs.add(pairs.remove(i));
						i -= x;
					}
				}
			}
			i++;
		}
		for(Integer num : singles){
			System.out.println("single " + num);
		}
		for(Integer num : pairs){
			System.out.println("pairs " + num);
		}
		for(Integer num : cPairs){
			System.out.println("cPairs " + num);
		}
		int numPlays = 0;
		if(!singles.isEmpty()){
			numPlays++;
		}
		if(!pairs.isEmpty()){
			numPlays++;
		}
		
		if(!cPairs.isEmpty()){
			numPlays++;
		}
		if(numPlays != 1){
			System.out.println("highest");
		}
		else{
			System.out.println("not highest");
		}
		if(startTrick){
			startTrickInfo[0] = getSuit(cards.get(0));
			if(!cPairs.isEmpty()){
				startTrickInfo[2] = 1;
			}
			else{
				startTrickInfo[2] = 0;
			}
			if(!pairs.isEmpty() || !cPairs.isEmpty()){
				startTrickInfo[1] = 1;
			}
			else{
				startTrickInfo[1] = 0;
			}
		}
	}
	
	public boolean adjacentPower(int one, int two){
		if(getSuit(one) != getSuit(two)){
			return false;
		}
		if(getSuit(one) != 5){
			int temp = one;
			int temp2 = two;
			if((temp%100)/2 < levels[champion]){
				temp += 2;
			}
			if((temp2%100)/2 < levels[champion]){
				temp2 += 2;
			}
			return temp/2 == (temp2/2) + 1 || temp/2 == (temp2/2) - 1;
		}
		//must be prime at this point
		//if either are a two
		if(getNum(one) == levels[champion] || getNum(two) == levels[champion]){
			//ace and non prime two
			if(getNum(one) == 14 && getNum(two) == levels[champion] && two/100 != levels[champion]){
				return true;
			}
			//any 2 two's
			if(getNum(one) == levels[champion] && getNum(two) == levels[champion]){
				return true;
			}
			//prime two and sj
			if(getNum(one) == levels[champion] && one/100 == levels[champion] && getNum(two) == sj){
				return true;
			}
			return false;
		}
		int temp = one;
		int temp2 = two;
		if((temp%100)/2 < levels[champion]){
			temp += 2;
		}
		if((temp2%100)/2 < levels[champion]){
			temp2 += 2;
		}
		return temp/2 == (temp2/2) + 1 || temp/2 == (temp2/2) - 1;
		
	}
	
	public int getSuit(int num){
		return num/100==primeSuit || (num%100)/2 == levels[champion] ? 5 : num/100;
	}
	
	public int getNum(int num){
		return (num%100)/2;
	}
	
	public static void main(String[] args)  {
		ClientModel2 cm2 = new ClientModel2(0, 0);
		cm2.champion = 0;
		cm2.levels[cm2.champion] = 3;
		cm2.primeSuit = 2;
		//as long as the string comes in as expected we gucci with reading
		//lowest to highest, non prime two's , prime two's, sj bj
		cm2.parsePlay("104105108109110111112113116117120121122123124125204205212213216217206207208209406407306307544545546");
		cm2.parsePlay("108");
		cm2.parsePlay("108109");
		cm2.parsePlay("108109110");
	}
}

/*
 * Send in converted format
 * Convert back into GUI format locally
 * Conversion back into GUI format should occur when getting images, otherwise the converted format is superior
 * 
 * Define the converted format
 * Prime suit starts with 4?
 * Reg suits start with 0 1 2 3
 * Clubs Diamonds Spades Hearts
 * Switch it up so that its black red black red
 * Will make it easier for the GUI later on
 * 106 = 3 of clubs
 * 107 = 3 of clubs
 * 108 = 4 of clubs
 * 109 = 4 of clubs
 * 110 = 5 of clubs
 * 111 = 5 of clubs
 * 
 * 206 = 3 of diamonds
 * 306 = 3 of spades
 * 406 = 3 of hearts
 * 500 = sj
 * 501 = sj
 * 503 = bj
 * 504 = bj
 * 
 * just send cards and have it figured out locally yolo lmao
 * so when I send info, if there is a pair, the numbers will be next to each other hopefully
 * pair checker
 * 
 * 
 * 
 */