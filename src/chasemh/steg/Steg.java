package chasemh.steg;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.concurrent.ThreadLocalRandom;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

/**
 * Steg Class. Implements a Stegonography algorithm for encrypting and decrypting secret messages in images.
 * 
 * @author Chase Hennion <chase_hennion@outlook.com>
 * @version 1.0
 *
 */
public class Steg {

	/**
	 * The key image used for encrytion and decryption of messages.
	 */
	private BufferedImage keyImg;
	
	/**
	 * CipherPixel array representation of the Key image.
	 */
	private CipherPixel[] pixels;
	
	/**
	 * Creates a new Steg object
	 * 
	 * @param keyFilePath Path to the image file that will be read in and used as the encryption/decryption key
	 * 
	 * @since 1.0 
	 */
	public Steg( String keyFilePath ) throws IOException {
		
		this.keyImg = this.readImageFromFile( keyFilePath );
		this.pixels = this.toCipherPixelArray( this.keyImg );

	}
	
	/**
	 * Creates a new Step object. User selects the key file using a dialog.
	 * 
	 * @throws IOException Thrown if the user doesn't choose a key file from the dialog.
	 * 
	 * @since 1.0
	 */
	public Steg() throws IOException {
		
		this.keyImg = this.readImageFromFile( this.chooseFile( false ) );
		this.pixels = this.toCipherPixelArray( this.keyImg );

	}
	
	/**
	 * Allows a user to choose or save an image file graphically
	 * 
	 * @param savingFile If true, opens the save file dialog rather than open file
	 * 
	 * @return The absolute path to the chosen file
	 * @throws IOException Thrown if the user does not choose a file.
	 * 
	 * @since 1.0
	 */
	private String chooseFile( boolean savingFile ) throws IOException {
		
		JFileChooser jfc = new JFileChooser();
		jfc.setCurrentDirectory( new File(System.getProperty( "user.dir" ) ) );
		
		int returnValue;
		if( savingFile ) {
			returnValue =jfc.showSaveDialog( null );
		}
		else {
			
			returnValue = jfc.showOpenDialog( null );
		}
		

		if( returnValue == JFileChooser.APPROVE_OPTION ) {
			
			return jfc.getSelectedFile().getAbsolutePath();
		
		}
		else {
			throw new IOException( "No image file chosen!" );
		}
		
	}
	
	/**
	 * Reads an image from file and stores it as a BufferedImage with 8-bit RGBA color components packed into integer pixels.
	 * 
	 * @param fileName Path to the image file to read
	 * @return A BufferedImage representing the image read from file
	 * @throws IOException Thrown when fileName is not a path to a valid image
	 * 
	 * @since 1.0
	 */
	private BufferedImage readImageFromFile( String fileName ) throws IOException {
		
		// Read in the raw image 
		BufferedImage in = ImageIO.read( new File( fileName ) );

		// Convert the image into a consistent type
		BufferedImage out = new BufferedImage( in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = out.createGraphics();
		g.drawImage( in, 0, 0, null );
		
		return out;
		
	}
	
	/**
	 * Writes a BufferedImage to file
	 * 
	 * @param fileName The absolute path to the file to save the BufferedImage to
	 * @param img The BufferedImage to save
	 * @throws IOException Thrown if the user does not type in or select a file to save to
	 * 
	 * @since 1.0
	 */
	private void writeImageToFile( String fileName, BufferedImage img ) throws IOException {
		
		File outputfile = new File( fileName );
		
		String extension = "";
		int i = fileName.lastIndexOf( '.' );
		if (i > 0) {
		    extension = fileName.substring( i+1 );
		}
		
	    ImageIO.write( img, extension, outputfile );
	    
	}
	
	/**
	 * Turns a BufferedImage into a CipherPixel Array representation.
	 * 
	 * @param img The BufferedImage to convert.
	 * @return The CipherPixel Array representation of the given image
	 * 
	 * @since 1.0
	 */
	private CipherPixel[] toCipherPixelArray( BufferedImage img) {
		// Convert the BufferedImage into a CipherPixel Array
		int height = img.getHeight();
		int width = img.getWidth();
		
		CipherPixel[] cipherPixels = new CipherPixel[ height * width ];
		int pixelIndex = 0;
		
		for( int y = 0; y < height; ++y ) {
			for( int x = 0; x < width; ++x ) {
				cipherPixels[ pixelIndex ] = new CipherPixel( x, y, img.getRGB( x, y ) );
				pixelIndex++;
			}
		}
		
		return cipherPixels;
	}
	
	/**
	 * Turns a CipherPixel Array into a BufferedImage
	 * 
	 * @param img The BufferedImage to convert.
	 * @return The CipherPixel Array representation of the given image
	 * 
	 * @since 1.0
	 */
	private BufferedImage toBufferedImage( CipherPixel[] cipherPixels ) {
		// Convert the CiperPixel array to a buffered image
		
		int height = this.keyImg.getHeight();
		int width = this.keyImg.getWidth();
		BufferedImage outImg = new BufferedImage( width, height, this.keyImg.getType() );
		
		int pixelIndex = 0;
		for( int y = 0; y < height; ++y ) {
			for( int x = 0; x < width; ++x ) {
				outImg.setRGB( x, y, cipherPixels[ pixelIndex ].getRGB() );
				pixelIndex++;
			}
		}
		
		return outImg;
	}
	
	/**
	 * Calculates a random pixel distribution for encrypting a given message
	 * 
	 * @param message The message to encrypt
	 * @return An array of integers. Each index represents the corresponding index of the letter in the message, each value represents the index of the pixel
	 * 		   to change in the CipherPixel array representation of the key.
	 * @throws InvalidParameterException Thrown when the message is longer than the number of pixels in the key image
	 * 
	 * @since 1.0
	 */
	private int[] calculateMessageDistribution( String message ) throws InvalidParameterException {
		// Given a message, calculate the distribution of characters in the picture
		// Characters should still appear in order going from left to right, top to bottom but should be distributed as randomly as possible.
		// This method should return an array that contains the indices in the pixels array where characters should be enciphered.
		
		
		int[] encipherIndices = new int[ message.length() ];
		int spacing = Math.floorDiv( this.pixels.length, message.length() );
		
		if( spacing < 0 ) {
			// Message is too big for this image
			throw new InvalidParameterException( message + " is too large for provided key image!" );
		}
		
		int searchStart = 0;
		int searchEnd = searchStart + spacing;
		boolean validPixelFound = false;
		int badPixelCount = 0;
		
		for( int messageIndex = 0; messageIndex < message.length(); ++messageIndex ) {
			// Encipher every letter in the message
			char currentChar = message.charAt( messageIndex );
			while( !validPixelFound ) {
				// Generate a random index between searchStart and searchEnd
				// See if the pixel in the slot can accommodate the current characters
				int randomIndex = ThreadLocalRandom.current().nextInt( searchStart, searchEnd );
				if( this.pixels[ randomIndex ].canFitCharacter( currentChar ) ) {
					encipherIndices[ messageIndex ] = randomIndex;
					validPixelFound = true;
				}
				else {
					badPixelCount++;
				}
			}
			
			searchStart = searchEnd;
			searchEnd = searchStart + spacing;
			validPixelFound = false;
			
		}
		
		return encipherIndices;
	}
	
	/**
	 * Encrypts a given message.
	 * 
	 * @param message The message to encrypt
	 * @param saveEncrypted If true, the encrypted image will be saved to file via a dialog
	 * @return A BufferedImage representing the key image with the message encrypted in it
	 * @throws IOException 
	 * 
	 * @since 1.0
	 */
	public BufferedImage encrypt( String message, boolean saveEncrypted ) throws IOException {
		
		// Sanitize the input message. Uppercase everything and remove punctuation. Replaces spaces with @ so everything is close together on the ASCII table
		message = message.toUpperCase().replaceAll( "\\p{P}", "" ).replaceAll(" ", "@" );
		
		int[] encipherIndices = this.calculateMessageDistribution( message );
		int messageIndex = 0;
		
		// Encrypt all of the characters
		for( int i = 0; i < encipherIndices.length; ++i ) {
			int pixelIndex = encipherIndices[ i ];
			this.pixels[ pixelIndex ].encipherCharacter( message.charAt( messageIndex ) );
			messageIndex++;
		}
		
		BufferedImage encrypted = this.toBufferedImage( this.pixels );
		
		if( saveEncrypted ) {
			String fileName = this.chooseFile( true );
			this.writeImageToFile( fileName, encrypted );
		}
		
		// Reset Key Pixels for further encryption or decryption
		this.pixels = this.toCipherPixelArray( this.keyImg );
		
		return encrypted;
	
		
	}
	
	/**
	 * Decrypts a message from an encrypted image.
	 * 
	 * @param encryptedFileName The path to the file containing the encrypted image to decrypt.
	 * @return The message string decrypted from the given file.
	 * @throws InvalidParameterException Thrown when the image read doesn't have the same dimensions as the key.
	 * @throws IOException Thrown when encryptedFileName does not point to a valid image file
	 * 
	 * @since 1.0
	 */
	public String decrypt( String encryptedFileName ) throws InvalidParameterException, IOException {
		// Convert the bufferedImage to a CipherPixel Array
		// Iterate through the array
		// Compare each pixel to the pixel in the key image cipher pixel array
		// If the pixels are the same, no encrypted character
		// If they are not, a character has been encrypted. 
			// Decipher the character
			// If it is an '@', append a space to the output message
			// Otherwise, append the character representation of the pixel difference + the offset to the message
		
		BufferedImage encryptedImg = this.readImageFromFile( encryptedFileName );
		
		if( encryptedImg.getWidth() != this.keyImg.getWidth() || encryptedImg.getHeight() != this.keyImg.getHeight() ) {
			// The images are different sizes. The key must not have been used to encrypt the given image
			throw new InvalidParameterException( "The dimensions of the encrypted image differ from the key image. The images must be the same size." );
		}
		
		CipherPixel[] encryptedPixels = this.toCipherPixelArray( encryptedImg );
		StringBuilder sb = new StringBuilder();
		
		for( int i = 0; i < encryptedPixels.length; ++i ) {
			CipherPixel encrypted = encryptedPixels[ i ];
			CipherPixel key = this.pixels[ i ];
			if( encrypted.compareTo( key ) != 0 ) {
				// Pixels are not the same
				// A character must be encrypted in this pixel!
				char c = encrypted.decipherCharacter( key );
				if( c == '@' ) {
					// Turn @ back into spaces in the output
					c = ' ';
				}
				sb.append( c );
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Decrypts a message from an image chosen graphically
	 * 
	 * @return  The message string decrypted from the given file.
	 * @throws IOException Thrown if the user does not select a file.
	 * 
	 * @since 1.0
	 */
	public String decrypt() throws IOException {
		String encryptedFileName = this.chooseFile( false );
		return this.decrypt( encryptedFileName );
	}
	
	
}
