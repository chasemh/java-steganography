package chasemh.steg;

import java.awt.Color;

/**
 * CipherPixel Class. Used with the Steg class to handle encryption of message characters into pixel data.
 * 
 * @author Chase Hennion <chase_hennion@outlook.com>
 * @version 1.0
 *
 */
public class CipherPixel implements Comparable<CipherPixel> {
	
	/**
	 * The maximum allowed value for R, G, and B.
	 */
	private static final int COLOR_MAX = 255;
	
	/**
	 * The ASCII offset used during encryption.
	 * This is used to make the encrypted characters smaller so pixels do not appear drastically changed after encryption.
	 */
	private static final int ASCII_OFFSET = 63;

	/**
	 * The X coordinate for the pixel in an image.
	 */
	private int x;
	
	/**
	 * The Y coordinate for the pixel in an image.
	 */
	private int y;
	
	/**
	 * A Color object representing the color of the pixel.
	 */
	Color color;
	
	/**
	 * Creates a new CipherPixel with the given x and y coordinates.
	 * 
	 * @param x The X coordinate of the pixel
	 * @param y The Y coordinate of the pixel
	 * 
	 * @since 1.0
	 */
	public CipherPixel( int x, int y ) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Creates a new CipherPixel with the given x and y coordinates and the given rgba color value.
	 * 
	 * @param x The X coordinate of the pixel
	 * @param y The Y coordinate of the pixel
	 * @param rgba The integer representation of the combined r,g,b,a values.
	 * 
	 * @since 1.0
	 */
	public CipherPixel( int x, int y, int rgba ) {
		// rbga is a 4 byte integer
		// 00000000 00000000 00000000 11111111
		// ^ Alpha  ^Red     ^Green   ^Blue
		
		this.x = x;
		this.y = y;
		
		this.color = new Color( rgba );
	}
	
	/**
	 * Creates a new CipherPixel with the given x and y coordinates and the given r,g,b and a color values.
	 * 
	 * @param x The X coordinate of the pixel
	 * @param y The Y coordinate of the pixel
	 * @param r The Red color value of the pixel
	 * @param g The Green color value of the pixel 
	 * @param b The Blue color value of the pixel
	 * 
	 * @since 1.0
	 */
	public CipherPixel( int x, int y, int r, int g ,int b ) {
		this.x = x;
		this.y = y;
		this.color = new Color( r, g, b  );
	}
	
	/**
	 * Gets the X coordinate of the pixel.
	 * 
	 * @return The X coordinate of the pixel.
	 * 
	 * @since 1.0
	 */
	public int getX() {
		return this.x;
	}
	
	/**
	 * Gets the Y coordinate of the pixel.
	 * 
	 * @return The Y coordinate of the pixel,
	 * 
	 * @since 1.0
	 */
	public int getY() {
		return this.y;
	}
	
	/**
	 * Gets the Red color value of the pixel.
	 * 
	 * @return The Red color value of the pixel,
	 * 
	 * @since 1.0
	 */
	public int getR() {
		return this.color.getRed();
	}
	
	/**
	 * Gets the Green color value of the pixel.
	 * 
	 * @return The Green color value of the pixel,
	 * 
	 * @since 1.0
	 */
	public int getG() {
		return this.color.getGreen();
	}
	
	/**
	 * Gets the Blue color value of the pixel.
	 * 
	 * @return The Blue color value of the pixel,
	 * 
	 * @since 1.0
	 */
	public int getB() {
		return this.color.getBlue();
	}
	
	/**
	 * Gets the combined RGB color value of the pixel.
	 * 
	 * @return The combined RGB color value of the pixel.
	 * 
	 * @since 1.0
	 */
	public int getRGB() {
		return this.color.getRGB();
	}
	
	/**
	 * Enciphers a given character into this pixel.
	 * 
	 * @param c The character to encipher.
	 * 
	 * @since 1.0
	 */
	public void encipherCharacter( char c ) {
		// Convert the character to an ascii value
		// Evenly split it into three parts ( floor of divide by 3, then modulo three to get any remainder )
		// Add split chunk to each r, g and b value
		
		int charVal = (int)c - ASCII_OFFSET;
		int splitVal = Math.floorDiv( charVal, 3 );
		int remainder = charVal % 3;
	
		int newRed = this.color.getRed() + splitVal;
		int newGreen = this.color.getGreen() + splitVal;
		int newBlue = this.color.getBlue() + splitVal;
		
		// Handle the possible remainder value
		if( remainder != 0 ) {
			// Add the remainder to the current lowest value
			int minVal = Math.min( Math.min( newRed, newGreen ), newBlue );
			if( minVal == newRed ) {
				newRed += remainder;
			}
			else if( minVal == newGreen ) {
				newGreen += remainder;
			}
			else {
				newBlue += remainder;
			}
		}
		
		this.color = new Color( newRed, newGreen, newBlue );
		
	}
	
	/**
	 * Deciphers the character enciphered into this pixel given the original, key pixel.
	 * 
	 * @param key The original, key pixel.
	 * @return The deciphered character.
	 * 
	 * @since 1.0
	 */
	public char decipherCharacter( CipherPixel key ) {
		int redDiff = this.getR() - key.getR();
		int greenDiff = this.getG() - key.getG();
		int blueDiff = this.getB() - key.getB();
		
		return (char) ( redDiff + greenDiff + blueDiff + ASCII_OFFSET );
	}
	
	/**
	 * Returns true if there is enough room to encipher the given character into this pixel.
	 * A pixel has enough "room" if the integer representation of the character can be added to the 
	 * r, g and b values evenly without going over the maximum allowed color value.
	 * 
	 * @param c The character that is to be tested for encipherment
	 * @return True if the character can be enciphered into this pixel. False otherwise.
	 * 
	 * @since 1.0
	 */
	public boolean canFitCharacter( char c ) {
		// Check if this pixels R G B values can fit the given c
		// That is, if there is enough room between the current RGB values and the max value, 255 to evenly fit the split value of the character
		
		int charVal = (int)c - ASCII_OFFSET;
		int splitVal = Math.floorDiv( charVal, 3 );
		int remainder = charVal % 3;
		int minVal = Math.min( Math.min( this.color.getRed(), this.color.getGreen() ), this.color.getBlue() );
		
		if( remainder == 0 ) {
			if( this.color.getRed() + splitVal > COLOR_MAX || this.color.getGreen() + splitVal > COLOR_MAX || this.color.getBlue() + splitVal > COLOR_MAX ) {
				return false;
			}
		}
		else {
			if( this.color.getRed() + splitVal > COLOR_MAX || this.color.getGreen() + splitVal > COLOR_MAX || this.color.getBlue() + splitVal > COLOR_MAX || minVal + splitVal + remainder > COLOR_MAX ) {
				return false;
			}
		}
		
		return true;
	}

	
	/**
	 * Implementation of compareTo from the Comparable interface
	 * 
	 * @param cp The CipherPixel to compare to this pixel
	 * @return 0 if the CipherPixels have the same color. Nonzero if the colors differ.
	 * 
	 * @since 1.0
	 */
	public int compareTo( CipherPixel cp ) {		
		return this.color.getRGB() - cp.getRGB();
	}

}
