/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.persistence;

public class BadFileFormatException extends Exception {

	private static final long serialVersionUID = 1L;

	public BadFileFormatException(String message) {
		super(message);
	}
	
	public BadFileFormatException(String message, Throwable cause) {
		super(message, cause);
	}

}