package chasemh.steg;

import java.io.IOException;

/**
 * Example Driver class for the Steg Project
 * 
 * @author Chase Hennion <chase_hennion@outlook.com>
 * @version 1.0
 *
 */
public class Driver {

	public static void main(String[] args) throws IOException {
		
		Steg s = new Steg();
		s.encrypt( "Hello there!", true );
		String result = s.decrypt();
		System.out.println( "Decrypted Message: " + result );
		

	}

}
