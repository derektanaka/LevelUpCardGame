package levelup;

import java.util.LinkedList;

public class Trick {

	public Play[] plays;
	private LinkedList<Play> playsLL;
	public int numCards = 0;
	private String s;
	//
	//
	//
	//
	//
	
	// 0 = highest, 1 = single, 2 = pair, 3+ = consec
	// first char is playType
	// if not highest, next 3 chars are the strength of the play
	// if highest, next char is number of plays
	// next n*4 chars are play types and strengths (playtype + playStrenght)
	// 0 indicating highest, n indicating number of plays, n sets of 4 bytes indicating plays
	public Trick(String temp){
		this.s = temp;
		playsLL = new LinkedList<Play>();
		if(Integer.parseInt(s.substring(0, 1)) > 0){
			playsLL.add(parsePlay());
		}
		else{
			s = s.substring(1);
			int numPlays = parse(1);
			for(int i = 0; i < numPlays; i++){
				playsLL.add(parsePlay());
			}
		}
		plays = new Play[numCards];
		Play tempP = null;
		int i = 0;
		while((tempP = playsLL.peek()) != null){
			playsLL.pop();
			plays[i] = tempP;
			i++;
		}
	}
	
	private int parse(int num){
		try{
			int ret = Integer.parseInt(s.substring(0, num));
			s = s.substring(num);
			return ret;
		}catch(IndexOutOfBoundsException e){
			System.out.println("Trick::parse(int num) error 1");
			return -1;
		}
	}
	
	private Play parsePlay(){
		Play p = new Play(parse(1), parse(3));
		this.numCards += p.numCards;
		return p;
	}
	
	public String toString(){
		String ret = "";
		Play temp =  null;
		Play[] p = plays;
		int counter = 0;
		for(int i = 0; i < plays.length; i++){
			ret += plays[i] + "\n";
			counter++;
		}
		if(counter > 1){
			ret = "highest\n" + ret;
		}
		return ret;
	}
	
	public static void main(String[] args){
		System.out.println("hi");
		Trick t = new Trick("03411121121123");
		System.out.println(t.toString());
	}
	
}
