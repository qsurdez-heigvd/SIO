package ch.heig.sio.lab2.tsp;

/**
 * Exception thrown when parsing a TSP file fails.
 */
public final class TspParsingException extends RuntimeException {
	/**
	 * Constructs a new exception with the specified detail message.
	 *
	 * @param message the detail message
	 */
	public TspParsingException(String message) {
		super(message);
	}
}
