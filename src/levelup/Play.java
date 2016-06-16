package levelup;

public class Play {

	public int type;
	public int strength;
	public int numCards;
	
	public Play(int type, int strength){
		this.type = type;
		this.strength = strength;
		this.numCards = type*2 - 2;
		if(numCards == 0){
			numCards = 1;
		}
	}
	
	public String toString(){
		String s = "";
		switch(type){
		case 1: s += "single"; break;
		case 2: s += "pair"; break;
		default: s += (type-1) + " consec"; break;
		}
		return s + ":" + strength;
	}
}

/*
 * int type (1 = single, 2 = pair, 3+ = consec w/ length (n-1)
 * int strength
 * for consecs I need length
 * for highest I need a ton more
 * 
 * 
 * In terms of from the client, I want to be able to just throw my entire play 
 * and the computer should be able to tell what I'm doing
 * 
 * could have a linked list
 * consecs at beginning, then pairs then highest
 * check first consec
 * check second consec if first consec succeeds
 * pairs -> highest
 * 
 * 
 * 
 */