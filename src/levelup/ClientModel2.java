package levelup;

import java.util.ArrayList;

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
	
	private ArrayList<Integer> play;
	private ArrayList<Integer> pairs;
	private ArrayList<Integer> cPairs;
	
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
	
	public void pullCPs(){
		cPairs = new ArrayList<Integer>();//reset it everytime
		int i = 0;
		while(i < pairs.size()){
			
		}
	}
	
	public void pullPs(){
		pairs = new ArrayList<Integer>(); //reset it everytime
		int i = 0;
		while(i < play.size()){
			if(play.get(i)%2 == 0){
				try{
					if(play.get(i)+1 == play.get(i+1)){
						pairs.add(play.remove(i));
						pairs.add(play.remove(i));
					}
					else{
						i++;
					}
				}catch(IndexOutOfBoundsException e){
					i++;
				}
			}
		}
	}
	
	public void parsePlay(String play){
		ArrayList<Integer> cards = new ArrayList<Integer>();
		ArrayList<Integer> pairs = new ArrayList<Integer>();
		ArrayList<Integer> cPairs = new ArrayList<Integer>();
		int length = play.length();
		for(int i = 0; i < length/3; i++){
			cards.add(Integer.parseInt(play.substring(0, 3)));
			play = play.substring(3);
		}
		//look for pairs
		int i = 0;
		while(i < cards.size()){
			if(cards.get(i)%2 == 0){
				try{
					if(cards.get(i)+1 == cards.get(i+1)){
						pairs.add(cards.remove(i));
						pairs.add(cards.remove(i));
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
		for(Integer num : cards){
			System.out.println("single " + num);
		}
		for(Integer num : pairs){
			System.out.println("pairs " + num);
		}
		for(Integer num : cPairs){
			System.out.println("cPairs " + num);
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
 * 006 = 3 of clubs
 * 007 = 3 of clubs
 * 008 = 4 of clubs
 * 009 = 4 of clubs
 * 010 = 5 of clubs
 * 011 = 5 of clubs
 * 
 * Move up cards when the prime # is higher to fill in the gap
 * if playing 4's
 * 006 = 2 of clubs
 * 007 = 2 of clubs
 * 008 = 3 of clubs
 * 009 = 3 of clubs
 * 010 = 5 of clubs
 * 011 = 5 of clubs
 * 
 * 106 = 3 of diamonds
 * 206 = 3 of spades
 * 306 = 3 of hearts
 * 406 = 3 of primes
 * 407 = 3 of primes
 * 426 = K of primes
 * 427 = K of primes
 * 428 = A of primes
 * 429 = A of primes
 * 430 = # of primes, clubs
 * 431 = # of primes, clubs
 * 432 = # of primes, diamonds
 * 433 = # of primes, diamonds
 * 434 = # of primes, spades
 * 435 = # of primes, spades
 * 436 = # of primes, hearts
 * 437 = # of primes, hearts
 * 438 = prime #
 * 439 = prime #
 * 440 = sj
 * 441 = sj
 * 442 = bj
 * 443 = bj
 * 
 * just send cards and have it figured out locally yolo lmao
 * so when I send info, if there is a pair, the numbers will be next to each other hopefully
 * pair checker
 * 
 * 
 * 
 */