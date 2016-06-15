package levelup;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPfinder {

	public static void main(String[] args){
		try {
			System.out.println(InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			// FIXME Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
//Louderman 458 Derek/172.27.115.183